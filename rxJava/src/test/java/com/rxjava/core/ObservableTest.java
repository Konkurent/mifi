package com.rxjava.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import skillfactory.api.Observable;
import skillfactory.api.Observer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class ObservableTest {

    @Test
    @DisplayName("Проверка успешной эмиссии элементов")
    void testSuccessfulEmission() {
        List<Integer> receivedItems = new ArrayList<>();
        AtomicInteger completeCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);

        Observable<Integer> observable = Observable.create(observer -> {
            observer.onNext(1);
            observer.onNext(2);
            observer.onNext(3);
            observer.onComplete();
        });

        Observer<Integer> observer = new Observer<>() {
            @Override
            public void onNext(Integer item) {
                receivedItems.add(item);
            }

            @Override
            public void onError(Throwable t) {
                errorCount.incrementAndGet();
            }

            @Override
            public void onComplete() {
                completeCount.incrementAndGet();
            }
        };

        observable.subscribe(observer);

        assertEquals(List.of(1, 2, 3), receivedItems);
        assertEquals(1, completeCount.get());
        assertEquals(0, errorCount.get());
    }

    @Test
    @DisplayName("Проверка обработки ошибок")
    void testErrorHandling() {
        List<Integer> receivedItems = new ArrayList<>();
        AtomicInteger completeCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        RuntimeException testException = new RuntimeException("Test error");

        Observable<Integer> observable = Observable.create(observer -> {
            observer.onNext(1);
            throw testException;
        });

        Observer<Integer> observer = new Observer<>() {
            @Override
            public void onNext(Integer item) {
                receivedItems.add(item);
            }

            @Override
            public void onError(Throwable t) {
                errorCount.incrementAndGet();
                assertEquals(testException, t);
            }

            @Override
            public void onComplete() {
                completeCount.incrementAndGet();
            }
        };

        observable.subscribe(observer);

        assertEquals(List.of(1), receivedItems);
        assertEquals(0, completeCount.get());
        assertEquals(1, errorCount.get());
    }

    @Test
    @DisplayName("Проверка пустого потока")
    void testEmptyStream() {
        List<Integer> receivedItems = new ArrayList<>();
        AtomicInteger completeCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);

        Observable<Integer> observable = Observable.create(Observer::onComplete);

        Observer<Integer> observer = new Observer<>() {
            @Override
            public void onNext(Integer item) {
                receivedItems.add(item);
            }

            @Override
            public void onError(Throwable t) {
                errorCount.incrementAndGet();
            }

            @Override
            public void onComplete() {
                completeCount.incrementAndGet();
            }
        };

        observable.subscribe(observer);

        assertTrue(receivedItems.isEmpty());
        assertEquals(1, completeCount.get());
        assertEquals(0, errorCount.get());
    }

    @Test
    @DisplayName("Проверка множественных подписок")
    void testMultipleSubscriptions() {
        List<Integer> firstSubscriberItems = new ArrayList<>();
        List<Integer> secondSubscriberItems = new ArrayList<>();

        Observable<Integer> observable = Observable.create(observer -> {
            observer.onNext(1);
            observer.onNext(2);
            observer.onComplete();
        });

        Observer<Integer> firstObserver = new Observer<>() {
            @Override
            public void onNext(Integer item) {
                firstSubscriberItems.add(item);
            }

            @Override
            public void onError(Throwable t) {}

            @Override
            public void onComplete() {}
        };

        Observer<Integer> secondObserver = new Observer<>() {
            @Override
            public void onNext(Integer item) {
                secondSubscriberItems.add(item);
            }

            @Override
            public void onError(Throwable t) {}

            @Override
            public void onComplete() {}
        };

        observable.subscribe(firstObserver);
        observable.subscribe(secondObserver);

        assertEquals(List.of(1, 2), firstSubscriberItems);
        assertEquals(List.of(1, 2), secondSubscriberItems);
    }

    @Test
    @DisplayName("Проверка метода just")
    void testJust() {
        List<String> receivedItems = new ArrayList<>();
        AtomicInteger completeCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);

        Observable<String> observable = Observable.just("Hello");

        Observer<String> observer = new Observer<>() {
            @Override
            public void onNext(String item) {
                receivedItems.add(item);
            }

            @Override
            public void onError(Throwable t) {
                errorCount.incrementAndGet();
            }

            @Override
            public void onComplete() {
                completeCount.incrementAndGet();
            }
        };

        observable.subscribe(observer);

        assertEquals(List.of("Hello"), receivedItems);
        assertEquals(1, completeCount.get());
        assertEquals(0, errorCount.get());
    }
} 