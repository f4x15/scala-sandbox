public class ThreadYield {
    public static void main(String[] args) {
        Runnable r = () -> {
            int counter = 0;
            while (counter < 2) {
                System.out.println(Thread.currentThread()
                    .getName());
                counter++;

		// see: https://www.baeldung.com/java-thread-yield
                Thread.yield();  // inform sheduller that yield CPU ("уступать" in russian) in other threads
            }
        };
        new Thread(r).start();
        new Thread(r).start();
    }
}

