package com.my.project;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class ThreadedArrayElementCounter {
    public static class Counter implements Callable<Integer> {
        private boolean[] array;
        private int start;
        private int end;

        public Counter(boolean[] array, int start, int size) {
            this.array = array;
            this.start = start;
            this.end = this.start + size;
            if (this.end > this.array.length) {
                this.end = this.array.length;
            }
        }

        public Integer call() throws Exception {
            int count = 0;
            for (int i = start; i < end; ++i) {
                if (array[i]) {
                    ++count;
                }
            }
            return count;
        }
    }

    private final static int ARRAY_SIZE = 1000000;
    private boolean[] target = new boolean[ARRAY_SIZE];
    private int chunkSize;

    public ThreadedArrayElementCounter(int chunkSize) {
        this.chunkSize = chunkSize;
        target[945673] = true;
        target[211111] = true;
        //int population = (int) (Math.random() * (ARRAY_SIZE / 2 + 1));
    }

    public int count() {
        int chunks = (int) Math.ceil(ARRAY_SIZE / chunkSize);
        List<Future<Integer>> results = new ArrayList<Future<Integer>>(chunks);
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(chunks);
        int count = 0;
        try {
            for (int i = 0; i < ARRAY_SIZE; i += chunkSize) {
                results.add(executor.submit(new Counter(target, i, chunkSize)));
            }
            while (executor.getActiveCount() > 0) {
                try {
                    Thread.currentThread().sleep(100);
                } catch (InterruptedException e) {
                    // Do nothing
                }
            }
            for (int i = 0; i < chunks; ++i) {
                try {
                    count += results.get(i).get();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } catch (ExecutionException e) {
                    e.printStackTrace(System.err);
                    throw new RuntimeException(e);
                }
            }
        } finally {
            executor.shutdownNow();
        }
        return count;
    }

    public static void main( String[] args ) {
        ThreadedArrayElementCounter counter = new ThreadedArrayElementCounter(100000);
        System.out.println("Count of trues: " + counter.count());
    }
}
