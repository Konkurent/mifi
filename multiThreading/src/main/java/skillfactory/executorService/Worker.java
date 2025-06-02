package skillfactory.executorService;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class Worker extends Thread {
    private static final Logger log = LoggerFactory.getLogger(Worker.class);
    
    private final Consumer<Worker> onClose;
    @Getter
    private final BlockingQueue<Runnable> workQueue;
    private final long keepAliveTime;
    private final TimeUnit timeUnit;
    public final int workerId;
    private volatile boolean running;

    public Worker(String name, Consumer<Worker> onClose, BlockingQueue<Runnable> workQueue, long keepAliveTime, TimeUnit timeUnit, int workerId) {
        super(name);
        this.onClose = onClose;
        this.workQueue = workQueue;
        this.keepAliveTime = keepAliveTime;
        this.timeUnit = timeUnit;
        this.workerId = workerId;
        this.running = true;
    }

    @Override
    public void run() {
        try {
            while (running) {
                Runnable task = workQueue.poll(keepAliveTime, timeUnit);
                if (task != null) {
                    try {
                        log.info("{} is running {}", getName(), task.getClass().getSimpleName());
                        task.run();
                    } catch (Exception e) {
                        log.error("The {} was stopped due to an error that occurred", getName(), e);
                        onClose.accept(this);
                    }
                } else {
                    log.info("The {} died of boredom", getName());
                    running = false;
                    onClose.accept(this);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("{} was interrupted!", getName());
        }
    }

    @Override
    public void interrupt() {
        this.running = false;
        onClose.accept(this);
        super.interrupt();
    }

    public BlockingQueue<Runnable> getWorkQueue() {
        return workQueue;
    }
}
