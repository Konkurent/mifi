package skillfactory.executorService;

import org.junit.jupiter.api.Test;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class CustomThreadPoolTest {

    @Test
    void testBasicExecution() throws Exception {
        CustomThreadPool pool = CustomThreadPool.builder()
                .corePoolSize(2)
                .maxPoolSize(4)
                .keepAliveTime(1)
                .timeUnit(TimeUnit.SECONDS)
                .queueSize(10)
                .minSpareThreads(1)
                .build();

        AtomicInteger counter = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(5);

        // Запускаем 5 задач
        for (int i = 0; i < 5; i++) {
            pool.execute(() -> {
                try {
                    Thread.sleep(100);
                    counter.incrementAndGet();
                    latch.countDown();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        // Ждем завершения всех задач
        assertTrue(latch.await(2, TimeUnit.SECONDS));
        assertEquals(5, counter.get());
    }

    @Test
    void testSubmitWithFuture() throws Exception {
        CustomThreadPool pool = CustomThreadPool.builder()
                .corePoolSize(2)
                .maxPoolSize(4)
                .keepAliveTime(1)
                .timeUnit(TimeUnit.SECONDS)
                .queueSize(10)
                .minSpareThreads(1)
                .build();

        Future<Integer> future = pool.submit(() -> {
            Thread.sleep(100);
            return 42;
        });

        assertEquals(42, future.get(1, TimeUnit.SECONDS));
    }

    @Test
    void testShutdown() {
        CustomThreadPool pool = CustomThreadPool.builder()
                .corePoolSize(2)
                .maxPoolSize(4)
                .keepAliveTime(1)
                .timeUnit(TimeUnit.SECONDS)
                .queueSize(10)
                .minSpareThreads(1)
                .build();

        pool.shutdown();
        assertThrows(IllegalStateException.class, () -> pool.execute(() -> {}));
    }

    @Test
    void testMinSpareThreads() throws Exception {
        CustomThreadPool pool = CustomThreadPool.builder()
                .corePoolSize(2)
                .maxPoolSize(4)
                .keepAliveTime(1)
                .timeUnit(TimeUnit.SECONDS)
                .queueSize(10)
                .minSpareThreads(2)
                .build();

        CountDownLatch latch = new CountDownLatch(1);
        
        // Запускаем задачу, которая завершится через 2 секунды
        pool.execute(() -> {
            try {
                Thread.sleep(2000);
                latch.countDown();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        // Ждем завершения задачи
        assertTrue(latch.await(3, TimeUnit.SECONDS));
        
        // Проверяем, что количество воркеров не упало ниже minSpareThreads
        Thread.sleep(1000); // Даем время на остановку воркеров
        assertTrue(pool.getContext().get() >= pool.getMinSpareThreads());
    }

    @Test
    void testMaxPoolSize() throws Exception {
        CustomThreadPool pool = CustomThreadPool.builder()
                .corePoolSize(2)
                .maxPoolSize(3)
                .keepAliveTime(1)
                .timeUnit(TimeUnit.SECONDS)
                .queueSize(10)
                .minSpareThreads(1)
                .build();

        CountDownLatch latch = new CountDownLatch(5);
        
        // Запускаем 5 длительных задач
        for (int i = 0; i < 5; i++) {
            pool.execute(() -> {
                try {
                    Thread.sleep(2000);
                    latch.countDown();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        // Проверяем, что количество воркеров не превышает maxPoolSize
        Thread.sleep(1000); // Даем время на создание воркеров
        assertTrue(pool.getContext().get() <= pool.getMaxPoolSize());
    }

    @Test
    void testQueueOverflow() throws Exception {
        CustomThreadPool pool = CustomThreadPool.builder()
                .corePoolSize(1)
                .maxPoolSize(2)
                .keepAliveTime(1)
                .timeUnit(TimeUnit.SECONDS)
                .queueSize(2)
                .minSpareThreads(1)
                .build();

        CountDownLatch latch = new CountDownLatch(4);
        AtomicInteger completedTasks = new AtomicInteger(0);

        // Запускаем 4 длительные задачи
        for (int i = 0; i < 4; i++) {
            pool.execute(() -> {
                try {
                    Thread.sleep(1000);
                    completedTasks.incrementAndGet();
                    latch.countDown();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        // Ждем завершения всех задач
        assertTrue(latch.await(5, TimeUnit.SECONDS));
        assertEquals(4, completedTasks.get());
        
        // Проверяем, что количество воркеров не превышает maxPoolSize
        assertTrue(pool.getContext().get() <= pool.getMaxPoolSize());
    }
} 