class ThreadSleep {

public static void main(String []args) {
        Runnable task = () -> {
            try {
                int secToWait = 1000 * 60;
		// the same
		// TimeUnit.SECONDS.sleep(60);
                Thread.currentThread().sleep(secToWait);
                System.out.println("Waked up");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };
        Thread thread = new Thread(task);
        thread.start();

}
