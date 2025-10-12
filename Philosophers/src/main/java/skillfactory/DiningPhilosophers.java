package skillfactory;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.*;
import java.util.stream.IntStream;

public class DiningPhilosophers {

    private AtomicBoolean[] plateStatuses;
    private final Lock lock = new ReentrantLock();
    private final int meals;

    public DiningPhilosophers(int philosophers, int meals) {
        plateStatuses = new AtomicBoolean[philosophers];
        this.meals = meals;
        for (int i = 0; i < philosophers; i++) {
            plateStatuses[i] = new AtomicBoolean(true);
        }
    }

    public int[] execute(long t) throws InterruptedException {
        Philosopher[] philosophers = new Philosopher[plateStatuses.length];
        for (int i = 0; i < philosophers.length; i++) {
            philosophers[i] = new Philosopher(i, meals, t);
            philosophers[i].start();
        }
        for (int i = 0; i < philosophers.length; i++) {
            philosophers[i].join();
        }
        return IntStream.range(0, philosophers.length).map(i -> philosophers[i].count).toArray();
    }


    private class Philosopher extends Thread {

        private final int meals;
        private final long time;

        public int count;

        private volatile AtomicBoolean left;
        private volatile AtomicBoolean right;

        public Philosopher(int id, int meals, long time) {
            super("Философ " + (id + 1));
            this.meals = meals;
            this.time = time;
            left = plateStatuses[id];
            right = plateStatuses[plateStatuses.length == id + 1 ? 0 : id + 1];
        }

        @Override
        public void run() {
            boolean isLock = false;
            try {
                while (count != meals) {
                    isLock = lock.tryLock(time, TimeUnit.MILLISECONDS);
                    try {
                        if (left.get() && right.get()) {
                            left.set(false);
                            right.set(false);
                            Thread.sleep(time);
                            left.set(true);
                            right.set(true);
                            count++;
                        }
                    } finally {
                        if (isLock) {
                            lock.unlock();
                        }
                    }
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

}