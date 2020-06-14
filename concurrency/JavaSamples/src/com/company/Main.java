package com.company;


/**
 * Samples based on @see: <a href="https://javarush.ru/groups/posts/2078-threadom-java-ne-isportishjh--chastjh-v---executor-threadpool-fork-join-pool"/s>
 */
public class Main {

    /*
    public static void main(String[] args) {
        System.out.println("hello world");
    }

     */

    public static void main(String []args) throws Exception {
        // -> it is lambda, (parameters of function) -> {the body}
        Runnable task = () -> {
            System.out.println("Task executed");
        };

        Thread thread = new Thread(task);
        thread.start();
    }
}
