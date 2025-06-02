package skillfactory.sheduler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SingleThreadScheduler implements Scheduler {

    private final static ExecutorService EXEC = Executors.newSingleThreadExecutor();

    @Override
    public void execute(Runnable task) {
        EXEC.submit(task);
    }
}
