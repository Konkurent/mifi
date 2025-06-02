package skillfactory.executorService;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Slf4j
public class Worker extends Thread {

    private final Consumer<Worker> onClose;
    @Getter
    private final BlockingQueue<Runnable> workQueue;
    private final long keepAliveTime;
    private final TimeUnit timeUnit;
    public final int workerId;
    private volatile boolean running;

    @lombok.Builder
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
}
