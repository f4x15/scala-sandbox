/**
 * base on @see <a href="https://javarush.ru/groups/posts/2047-threadom-java-ne-isportishjh--chastjh-i---potoki"></a>
 */
public class HelloJavaThread {
   // First variant of create Thread
   public static class MyThread1 extends Thread {
        @Override
        public void run() {
            System.out.println("Hello, World! - 1");
        }
    }

    public static void main(String []args){
        // First variant
	// Classical way, but cons is: 
	// we need extend our class by Thread
	// it is break SOLID
	Thread thread1 = new MyThread1();
        thread1.start();

        // Second variant, more shorter
	// using Runnable Interface 
	Runnable task = new Runnable() {
            public void run() {
                System.out.println("Hello, World! - 2");
            }
        };
        Thread thread2 = new Thread(task);
        thread2.start();

	// Thrid variant
	// more shorter-way, with anonymus function
	Runnable task2 = () -> {
		System.out.println("Hello, World! - 3");
	};
	Thread thread3 = new Thread(task2);
	thread3.start();
}	
}
