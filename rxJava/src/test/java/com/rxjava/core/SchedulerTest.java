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

import static org.junit.jupiter.api.Assertions.*;

class SchedulerTest {

    @Test
    @DisplayName("Проверка subscribeOn")
    void testSubscribeOn() throws InterruptedException {
        List<Integer> receivedItems = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger completeCount = new AtomicInteger(0);
        Thread mainThread = Thread.currentThread();

        Observable<Integer> observable = Observable.<Integer>create(observer -> {
            assertNotEquals(mainThread, Thread.currentThread());
            observer.onNext(1);
            observer.onNext(2);
            observer.onComplete();
        }).subscribeOn(new IOThreadScheduler());

        observable.subscribe(new Observer<>() {
            @Override
            public void onNext(Integer item) {
                receivedItems.add(item);
            }

            @Override
            public void onError(Throwable t) {
                fail("Unexpected error: " + t.getMessage());
            }

            @Override
            public void onComplete() {
                completeCount.incrementAndGet();
                latch.countDown();
            }
        });

        assertTrue(latch.await(1, TimeUnit.SECONDS));
        assertEquals(List.of(1, 2), receivedItems);
        assertEquals(1, completeCount.get());
    }

    @Test
    @DisplayName("Проверка observeOn")
    void testObserveOn() throws InterruptedException {
        List<Integer> receivedItems = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger completeCount = new AtomicInteger(0);
        Thread mainThread = Thread.currentThread();

        Observable<Integer> observable = Observable.just(1, 2, 3)
                .observeOn(new ComputationScheduler());

        observable.subscribe(new Observer<>() {
            @Override
            public void onNext(Integer item) {
                assertNotEquals(mainThread, Thread.currentThread());
                receivedItems.add(item);
            }

            @Override
            public void onError(Throwable t) {
                fail("Unexpected error: " + t.getMessage());
            }

            @Override
            public void onComplete() {
                assertNotEquals(mainThread, Thread.currentThread());
                completeCount.incrementAndGet();
                latch.countDown();
            }
        });

        assertTrue(latch.await(1, TimeUnit.SECONDS));
        receivedItems.sort(Integer::compareTo);
        assertEquals(List.of(1, 2, 3), receivedItems);
        assertEquals(1, completeCount.get());
    }

    @Test
    @DisplayName("Проверка комбинации subscribeOn и observeOn")
    void testSubscribeOnAndObserveOn() throws InterruptedException {
        List<Integer> receivedItems = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger completeCount = new AtomicInteger(0);
        Thread mainThread = Thread.currentThread();

        Observable<Integer> observable = Observable.<Integer>create(observer -> {
            assertNotEquals(mainThread, Thread.currentThread());
            observer.onNext(1);
            observer.onNext(2);
            observer.onComplete();
        })
        .subscribeOn(new IOThreadScheduler())
        .observeOn(new ComputationScheduler());

        observable.subscribe(new Observer<>() {
            @Override
            public void onNext(Integer item) {
                assertNotEquals(mainThread, Thread.currentThread());
                receivedItems.add(item);
            }

            @Override
            public void onError(Throwable t) {
                fail("Unexpected error: " + t.getMessage());
            }

            @Override
            public void onComplete() {
                assertNotEquals(mainThread, Thread.currentThread());
                completeCount.incrementAndGet();
                latch.countDown();
            }
        });

        assertTrue(latch.await(1, TimeUnit.SECONDS));
        receivedItems.sort(Integer::compareTo);
        assertEquals(List.of(1, 2), receivedItems);
        assertEquals(1, completeCount.get());
    }
} 