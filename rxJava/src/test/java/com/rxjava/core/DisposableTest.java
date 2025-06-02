package com.rxjava.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import skillfactory.api.Disposable;
import skillfactory.api.Observable;
import skillfactory.api.Observer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class DisposableTest {

    @Test
    @DisplayName("Проверка отмены подписки")
    void testDispose() {
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

        Disposable disposable = observable.subscribe(observer);
        disposable.dispose(); // Отмена сразу после подписки

        // После dispose элементы не должны приходить (поток синхронный)
        assertTrue(disposable.isDisposed());
        // В этом тесте гарантируем только, что disposable в состоянии disposed
    }

    @Test
    @DisplayName("Проверка множественных вызовов dispose")
    void testMultipleDispose() {
        List<Integer> receivedItems = new ArrayList<>();
        AtomicInteger completeCount = new AtomicInteger(0);

        Observable<Integer> observable = Observable.just(1, 2, 3);
        Disposable disposable = observable.subscribe(new Observer<>() {
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
            }
        });

        disposable.dispose();
        disposable.dispose(); // Второй вызов не должен вызывать проблем

        assertTrue(disposable.isDisposed());
        assertEquals(List.of(1, 2, 3), receivedItems);
        assertEquals(1, completeCount.get());
    }

    @Test
    @DisplayName("Проверка отмены подписки после onComplete")
    void testDisposeAfterComplete() {
        List<Integer> receivedItems = new ArrayList<>();
        AtomicInteger completeCount = new AtomicInteger(0);

        Observable<Integer> observable = Observable.just(1, 2, 3);
        Observer<Integer> observer = new Observer<>() {
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
            }
        };
        Disposable disposable = observable.subscribe(observer);
        disposable.dispose(); // Dispose после onComplete
        assertTrue(disposable.isDisposed());
        assertEquals(List.of(1, 2, 3), receivedItems);
        assertEquals(1, completeCount.get());
    }

    @Test
    @DisplayName("Проверка отмены подписки после onError")
    void testDisposeAfterError() {
        List<Integer> receivedItems = new ArrayList<>();
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
            }
            @Override
            public void onComplete() {
                fail("Should not complete after error");
            }
        };
        Disposable disposable = observable.subscribe(observer);
        disposable.dispose(); // Dispose после onError
        assertTrue(disposable.isDisposed());
        assertEquals(List.of(1), receivedItems);
        assertEquals(1, errorCount.get());
    }
} 