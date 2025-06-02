package com.rxjava.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import skillfactory.api.Observable;
import skillfactory.api.Observer;
import skillfactory.sheduler.ComputationScheduler;
import skillfactory.sheduler.IOThreadScheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class SchedulersTest {

    @Test
    @DisplayName("Проверка параллельной обработки в ComputationScheduler")
    void testParallelComputationScheduler() throws InterruptedException {
        List<Integer> receivedItems = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger threadCount = new AtomicInteger(0);
        AtomicReference<Thread> mainThread = new AtomicReference<>(Thread.currentThread());

        Observable<Integer> source = Observable.create(observer -> {
            for (int i = 0; i < 5; i++) {
                observer.onNext(i);
            }
            observer.onComplete();
        });

        Observable<Integer> parallel = source
            .subscribeOn(new ComputationScheduler())
            .observeOn(new ComputationScheduler());

        parallel.subscribe(new Observer<>() {
            @Override
            public void onNext(Integer item) {
                Thread currentThread = Thread.currentThread();
                if (currentThread != mainThread.get()) {
                    threadCount.incrementAndGet();
                }
                receivedItems.add(item);
            }

            @Override
            public void onError(Throwable t) {
                fail("Unexpected error: " + t.getMessage());
            }

            @Override
            public void onComplete() {
                latch.countDown();
            }
        });

        assertTrue(latch.await(1, TimeUnit.SECONDS));
        assertEquals(List.of(0, 1, 2, 3, 4), receivedItems);
        assertTrue(threadCount.get() > 0, "Должна быть использована как минимум одна дополнительная нить");
    }

    @Test
    @DisplayName("Проверка IO операций в IOThreadScheduler")
    void testIOThreadScheduler() throws InterruptedException {
        List<String> receivedItems = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger threadCount = new AtomicInteger(0);
        AtomicReference<Thread> mainThread = new AtomicReference<>(Thread.currentThread());

        Observable<String> source = Observable.create(observer -> {
            // Имитация IO операций
            Thread.sleep(100);
            observer.onNext("IO Operation 1");
            Thread.sleep(100);
            observer.onNext("IO Operation 2");
            observer.onComplete();
        });

        Observable<String> io = source.subscribeOn(new IOThreadScheduler());

        io.subscribe(new Observer<>() {
            @Override
            public void onNext(String item) {
                Thread currentThread = Thread.currentThread();
                if (currentThread != mainThread.get()) {
                    threadCount.incrementAndGet();
                }
                receivedItems.add(item);
            }

            @Override
            public void onError(Throwable t) {
                fail("Unexpected error: " + t.getMessage());
            }

            @Override
            public void onComplete() {
                latch.countDown();
            }
        });

        assertTrue(latch.await(1, TimeUnit.SECONDS));
        assertEquals(List.of("IO Operation 1", "IO Operation 2"), receivedItems);
        assertTrue(threadCount.get() > 0, "Должна быть использована как минимум одна дополнительная нить");
    }

    @Test
    @DisplayName("Проверка обработки ошибок в многопоточной среде")
    void testErrorHandlingInMultiThreadedEnvironment() throws InterruptedException {
        List<Integer> receivedItems = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger errorCount = new AtomicInteger(0);
        RuntimeException testException = new RuntimeException("Test error");

        Observable<Integer> source = Observable.create(observer -> {
            observer.onNext(1);
            observer.onNext(2);
            throw testException;
        });

        Observable<Integer> parallel = source
            .subscribeOn(new ComputationScheduler())
            .observeOn(new ComputationScheduler());

        parallel.subscribe(new Observer<>() {
            boolean errorOccurred = false;

            @Override
            public void onNext(Integer item) {
                if (errorOccurred) fail("Should not receive items after error");
                receivedItems.add(item);
            }

            @Override
            public void onError(Throwable t) {
                assertEquals(testException, t);
                errorOccurred = true;
                errorCount.incrementAndGet();
                latch.countDown();
            }

            @Override
            public void onComplete() {
                fail("Should not complete after error");
            }
        });

        assertTrue(latch.await(1, TimeUnit.SECONDS));
        assertTrue(receivedItems.size() <= 2, "Должно быть получено не более 2 элементов до ошибки");
        assertEquals(1, errorCount.get());
    }
} 