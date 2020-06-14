// sample based on: https://www.lightbend.com/blog/reactive-streams-java
import akka.NotUsed;
import akka.http.javadsl.model.ws.Message;
import akka.http.javadsl.model.ws.TextMessage;
import akka.http.javadsl.server.HttpApp;
import akka.http.javadsl.server.Route;
import akka.japi.Pair;
import akka.stream.FlowShape;
import akka.stream.Graph;
import akka.stream.SinkShape;
import akka.stream.ThrottleMode;
import akka.stream.UniformFanOutShape;
import akka.stream.alpakka.csv.javadsl.CsvParsing;
import akka.stream.alpakka.file.DirectoryChange;
import akka.stream.alpakka.file.javadsl.DirectoryChangesSource;
import akka.stream.javadsl.Broadcast;
import akka.stream.javadsl.FileIO;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.GraphDSL;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import akka.util.ByteString;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;


import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

import static akka.event.Logging.InfoLevel;
import static akka.stream.Attributes.createLogLevels;
import static java.util.stream.Collectors.averagingDouble;
import static java.util.stream.Collectors.toList;



public class CsvProcessor {


    //<editor-fold desc="Load config">
    private static final Config config = ConfigFactory.load();
    private static final Path DATA_DIR = Paths.get(config.getString("csv-processor.data-dir"));
    private static final Duration DATA_DIR_POLL_INTERVAL =
            (config.getDuration("csv-processor.data-dir-poll-interval"));
    private static final double AVERAGE_THRESHOLD = config.getDouble("csv-processor.average-threshold");
    private static final int EMAIL_THRESHOLD = config.getInt("csv-processor.email-threshold");
    //</editor-fold>

    private static final Logger logger = Logger.getLogger("CsvProcessor");

    // create `source` for your stream with out pair of `Pair<Path, DirectoryChange>`
    // TODO: make for FIniteDuration by scala
    private final Source<Pair<Path, DirectoryChange>, NotUsed> newFiles =
            DirectoryChangesSource.create(DATA_DIR, DATA_DIR_POLL_INTERVAL, 128);

    // create Flow from `Pair<Path, DirectoryChange>` to `Path`
    private final Flow<Pair<Path, DirectoryChange>, Path, NotUsed> csvPaths =
            Flow.<Pair<Path, DirectoryChange>>create()  // `Flow.create` used when we know flow type
                    .filter(this::isCsvFileCreationEvent)   // if success this predicate
                    .map(Pair::first);  // modify list by function and return some values
    // semantically: return new create `.csv` file-names

    // new created ".csv" files
    private boolean isCsvFileCreationEvent(Pair<Path, DirectoryChange> p) {
        return p.first().toString().endsWith(".csv") && p.second().equals(DirectoryChange.Creation);
    }

    // next we need translate 'path-names' into strings that get from concrete file.
    //  We need transform one source to another by `flatMap*`
    private final Flow<Path, ByteString, NotUsed> fileBytes =
            Flow.of(Path.class).flatMapConcat(FileIO::fromPath);
    // when we don't know concrete `Flow` type we use `of` method
    // `ByteString` - it is representing Byte stream accepted in Akka Stream
    // `flatMapConcat` - concatenate with ordering some strings
    // `FileIO::fromPath` - read from file by chunks of 8129 bytes

    // take bytes-sequence as csv-file
    private final Flow<ByteString, Collection<ByteString>, NotUsed> csvFields =
            Flow.of(ByteString.class).via(CsvParsing.lineScanner());
    // `via` attach some one `flow` to other flow or source
    // result: flow of csv elements


    // next we need transform it mo our domain model - one-by-one element
    private final Flow<Collection<ByteString>, Reading, NotUsed> readings =
            Flow.<Collection<ByteString>>create().map(Reading::create);

    // put values in pair, computaion average, collect to threshold then move to next
    private final Flow<Reading, Double, NotUsed> averageReadings =
            Flow.of(Reading.class)
                    .grouped(2)
                    .mapAsyncUnordered(10, readings ->      // computation in parallel
                            CompletableFuture.supplyAsync(() ->       // CF is chain of futures
                                    readings.stream()
                                            .map(Reading::getValue)
                                            .collect(averagingDouble(v -> v)))
                    )
                    .filter(v -> v > AVERAGE_THRESHOLD);

    // add notify to mailbox
    //  create custom step `broadcast` in your pipe for add some one to mailbox
    //                                  get custom of builder
    private final Graph<FlowShape<Double, Double>, NotUsed> notifier = GraphDSL.create(builder -> {
        // create sink
        Sink<Double, NotUsed> mailerSink = Flow.of(Double.class)
                .grouped(EMAIL_THRESHOLD)       // group input el into groups by size 5 el
                .to(Sink.foreach(ds ->          // add mail choke
                        logger.info("Sending e-mail")
                ));
        // create broadcast with two-outputs
        UniformFanOutShape<Double, Double> broadcast = builder.add(Broadcast.create(2));
        SinkShape<Double> mailer = builder.add(mailerSink);

        // add our sink to broadcast to `1` output
        builder.from(broadcast.out(1)).toInlet(mailer.in());

        // add out to `0` output
        return FlowShape.of(broadcast.in(), broadcast.out(0));
    });


    // collect all fields in your pipe
    private final Source<Double, NotUsed> liveReadings =
            newFiles
                    .via(csvPaths)
                    .via(fileBytes)
                    .via(csvFields)
                    .via(readings)
                    .via(averageReadings)
                    .via(notifier)
                    .withAttributes(createLogLevels(InfoLevel(), InfoLevel(), InfoLevel()));


    public static void main(String[] args) throws ExecutionException, InterruptedException {
        CsvProcessor csvProcessor = new CsvProcessor();
        Server server = new Server(csvProcessor.liveReadings);
        server.startServer(config.getString("server.host"), config.getInt("server.port"));
    }
}


/**
 * Our domain model
 */
class Reading {
    private final int id;
    private final double value;

    private Reading(int id, double value) {
        this.id = id;
        this.value = value;
    }

    double getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.format("Reading(%d, %f)", id, value);
    }

    static Reading create(Collection<ByteString> fields) {
        List<String> fieldList = fields.stream().map(ByteString::utf8String).collect(toList());
        int id = Integer.parseInt(fieldList.get(0));
        double value = Double.parseDouble(fieldList.get(1));
        return new Reading(id, value);
    }
}

/**
 * Server
 */
class Server extends HttpApp {

    private final Source<Double, NotUsed> readings;

    Server(Source<Double, NotUsed> readings) {
        this.readings = readings;
    }

    @Override
    protected Route routes() {
        return route(
                path("data", () -> {
                            Source<Message, NotUsed> messages = readings.map(String::valueOf).map(TextMessage::create);
                            return handleWebSocketMessages(Flow.fromSinkAndSourceCoupled(Sink.ignore(), messages));
                        }
                ),
                get(() ->
                        pathSingleSlash(() ->
                                getFromResource("index.html")
                        )
                )
        );
    }
}