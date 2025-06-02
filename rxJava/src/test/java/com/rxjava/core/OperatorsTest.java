package com.rxjava.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import skillfactory.api.Observable;
import skillfactory.api.Observer;
import skillfactory.operators.FilterOperator;
import skillfactory.operators.MapOperator;
import skillfactory.operators.FlatMapOperator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class OperatorsTest {

    @Test
    @DisplayName("Проверка оператора map")
    void testMapOperator() {
        List<String> receivedItems = new ArrayList<>();
        AtomicInteger completeCount = new AtomicInteger(0);

        Observable<Integer> source = Observable.just(1, 2, 3);
        Observable<String> mapped = MapOperator.map(source, Object::toString);

        mapped.subscribe(new Observer<>() {
            @Override
            public void onNext(String item) {
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

        assertEquals(List.of("1", "2", "3"), receivedItems);
        assertEquals(1, completeCount.get());
    }

    @Test
    @DisplayName("Проверка оператора filter")
    void testFilterOperator() {
        List<Integer> receivedItems = new ArrayList<>();
        AtomicInteger completeCount = new AtomicInteger(0);

        Observable<Integer> source = Observable.just(1, 2, 3, 4, 5);
        Observable<Integer> filtered = FilterOperator.filter(source, x -> x % 2 == 0);

        filtered.subscribe(new Observer<>() {
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

        assertEquals(List.of(2, 4), receivedItems);
        assertEquals(1, completeCount.get());
    }

    @Test
    @DisplayName("Проверка оператора flatMap")
    void testFlatMapOperator() {
        List<Integer> receivedItems = new ArrayList<>();
        AtomicInteger completeCount = new AtomicInteger(0);

        Observable<Integer> source = Observable.just(1, 2, 3);
        Observable<Integer> flatMapped = FlatMapOperator.flatMap(
            source,
            x -> Observable.just(x, x * 2)
        );

        flatMapped.subscribe(new Observer<>() {
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

        receivedItems.sort(Integer::compareTo);
        assertEquals(List.of(1, 2, 2, 3, 4, 6), receivedItems);
        assertEquals(1, completeCount.get());
    }

    @Test
    @DisplayName("Проверка обработки ошибок в операторах")
    void testOperatorsErrorHandling() {
        AtomicInteger errorCount = new AtomicInteger(0);
        RuntimeException testException = new RuntimeException("Test error");

        Observable<Integer> source = Observable.create(observer -> {
            observer.onNext(1);
            throw testException;
        });

        Observable<String> mapped = MapOperator.map(source, Object::toString);

        mapped.subscribe(new Observer<>() {
            boolean errorOccurred = false;
            @Override
            public void onNext(String item) {
                if (errorOccurred) fail("Should not receive items after error");
            }
            @Override
            public void onError(Throwable t) {
                assertEquals(testException, t);
                errorOccurred = true;
                errorCount.incrementAndGet();
            }
            @Override
            public void onComplete() {
                fail("Should not complete after error");
            }
        });

        assertEquals(1, errorCount.get());
    }

    @Test
    @DisplayName("Проверка обработки ошибок в цепочке операторов")
    void testErrorHandlingInOperatorChain() {
        List<String> receivedItems = new ArrayList<>();
        AtomicInteger errorCount = new AtomicInteger(0);
        RuntimeException testException = new RuntimeException("Test error");

        Observable<Integer> source = Observable.create(observer -> {
            observer.onNext(1);
            observer.onNext(2);
            throw testException;
        });

        Observable<String> transformed = MapOperator.map(
            FilterOperator.filter(
                MapOperator.map(source, Object::toString),
                s -> !s.equals("2")
            ),
            s -> "Value: " + s
        );

        transformed.subscribe(new Observer<>() {
            boolean errorOccurred = false;
            
            @Override
            public void onNext(String item) {
                if (errorOccurred) fail("Should not receive items after error");
                receivedItems.add(item);
            }

            @Override
            public void onError(Throwable t) {
                assertEquals(testException, t);
                errorOccurred = true;
                errorCount.incrementAndGet();
            }

            @Override
            public void onComplete() {
                fail("Should not complete after error");
            }
        });

        assertEquals(List.of("Value: 1"), receivedItems);
        assertEquals(1, errorCount.get());
    }
} 