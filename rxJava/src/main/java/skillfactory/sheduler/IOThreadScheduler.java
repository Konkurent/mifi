package skillfactory.sheduler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class IOThreadScheduler implements Scheduler {
    private final static ExecutorService EXEC = Executors.newCachedThreadPool();

    @Override
    public void execute(Runnable task) {
        EXEC.submit(task);
    }
}
