package skillfactory.sheduler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ComputationScheduler implements Scheduler {

    private final static int CORES = Runtime.getRuntime().availableProcessors();
    private final static ExecutorService EXEC = Executors.newFixedThreadPool(CORES);

    @Override
    public void execute(Runnable task) {
        EXEC.submit(task);
    }
}
