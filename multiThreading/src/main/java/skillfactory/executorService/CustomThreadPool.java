package skillfactory.executorService;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Data
@Slf4j
public class CustomThreadPool implements CustomExecutor {

    private final Lock lock = new ReentrantLock();
    private final AtomicBoolean shutdown = new AtomicBoolean(false);
    private final AtomicInteger context = new AtomicInteger(0);
    private final AtomicInteger workerIndex = new AtomicInteger(0);

    private final int corePoolSize;
    private final int maxPoolSize;
    private final int keepAliveTime;
    private final TimeUnit timeUnit;
    private final int queueSize;
    private final int minSpareThreads;
    private final WorkerFactory threadFactory;


    private final RejectedExecutionHandler handler = new ThreadPoolExecutor.CallerRunsPolicy();

    private final List<BlockingQueue<Runnable>> taskQueues = new ArrayList<>();
    private final List<Worker> workers = new ArrayList<>();

    @Builder
    public CustomThreadPool(
            int corePoolSize,
            int maxPoolSize,
            int keepAliveTime,
            TimeUnit timeUnit,
            int queueSize,
            int minSpareThreads
    ) {
        this.corePoolSize = corePoolSize;
        this.maxPoolSize = maxPoolSize;
        if (maxPoolSize < corePoolSize) throw new IllegalArgumentException("maxPoolSize must be greater than corePoolSize");
        this.keepAliveTime = keepAliveTime;
        this.timeUnit = timeUnit;
        this.queueSize = queueSize;
        this.minSpareThreads = minSpareThreads;
        this.threadFactory = new WorkerFactory();
        createWorker(corePoolSize);
    }

    private void createWorker(int workers) {
        for (int i = 0; i < workers; i++) {
            threadFactory.newThread(null).start();
        }
    }

    public BlockingQueue<Runnable> getWorkerQueue() {
        if (!taskQueues.isEmpty()) {
            int index = workerIndex.getAndIncrement() % taskQueues.size();
            return taskQueues.get(index);
        } else {
            Worker worker = threadFactory.newThread(null);
            worker.start();
            return worker.getWorkQueue();
        }

    }

    @Override
    public <T> Future<T> submit(Callable<T> callable) {
        FutureTask<T> futureTask = new FutureTask<>(callable);
        execute(futureTask);
        return futureTask;
    }

    @Override
    public void shutdown() {
        shutdown.set(true);
    }

    @Override
    public void shutdownNow() {
        workers.forEach(Worker::interrupt);
    }

    @Override
    public void execute(Runnable command) {
        if (shutdown.get()) {
            throw new IllegalStateException("Thread pool is already shutdown");
        }
        lock.lock();
        try {
            BlockingQueue<Runnable> queue = getWorkerQueue();
            queue.offer(command);
        } finally {
            lock.unlock();
        }
    }

    private void stopWorker(Worker worker) {
        lock.lock();
        try {
            if (context.get() <= minSpareThreads && !shutdown.get()) {
                Worker newWorker = threadFactory.buildThread(null);
                taskQueues.add(newWorker.getWorkQueue());
                workers.add(newWorker);
                newWorker.start();
                context.incrementAndGet();
                log.info("Created new worker {} to maintain minimum thread count", newWorker.getName());
            }
            taskQueues.remove(worker.getWorkQueue());
            workers.remove(worker);
            context.decrementAndGet();
            log.info("Worker {} stopped. Current thread count: {}", worker.getName(), context.get());
        } finally {
            lock.unlock();
        }
    }

    private class WorkerFactory implements ThreadFactory {

        private final static String PREFIX = "MyThread-";
        private final AtomicInteger threadNumber = new AtomicInteger(0);

        @Override
        public synchronized Worker newThread(Runnable r) {
            Worker worker = buildThread(r);
            taskQueues.add(worker.getWorkQueue());
            context.incrementAndGet();
            workers.add(worker);
            return worker;
        }

        public Worker buildThread(Runnable runnable) {
            BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>();
            if (runnable != null) queue.add(runnable);
            int workerId = threadNumber.getAndIncrement();
            return Worker.builder()
                    .onClose(CustomThreadPool.this::stopWorker)
                    .workQueue(queue)
                    .workerId(workerId)
                    .keepAliveTime(keepAliveTime)
                    .timeUnit(timeUnit)
                    .name(PREFIX + (workerId + 1))
                    .build();
        }
    }
}