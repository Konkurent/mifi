package skillfactory.operators;

import skillfactory.api.Disposable;
import skillfactory.api.Observable;
import skillfactory.api.Observer;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public class FlatMapOperator {

    public static <T, U> Observable<U> flatMap(
            Observable<T> source,
            Function<? super T, Observable<U>> mapper
    ) {
        return Observable.create(observer -> {
            ConcurrentLinkedQueue<Disposable> disposables = new ConcurrentLinkedQueue<>();
            AtomicInteger activeCount = new AtomicInteger(1); // 1 — родительский поток
            ConcurrentLinkedQueue<Throwable> errors = new ConcurrentLinkedQueue<>();

            Disposable parentDisp = source.subscribe(new Observer<T>() {
                @Override
                public void onNext(T item) {
                    activeCount.incrementAndGet();
                    Disposable innerDisp = mapper.apply(item)
                            .subscribe(new Observer<U>() {
                                @Override
                                public void onNext(U inner) {
                                    observer.onNext(inner);
                                }
                                @Override
                                public void onError(Throwable t) {
                                    errors.add(t);
                                    completeIfDone();
                                }
                                @Override
                                public void onComplete() {
                                    completeIfDone();
                                }
                            });
                    disposables.add(innerDisp);
                }

                @Override
                public void onError(Throwable t) {
                    errors.add(t);
                    completeIfDone();
                }

                @Override
                public void onComplete() {
                    completeIfDone();
                }

                private void completeIfDone() {
                    if (activeCount.decrementAndGet() == 0) {
                        // если были ошибки — передаем первую
                        Throwable err = errors.poll();
                        if (err != null) {
                            observer.onError(err);
                        } else {
                            observer.onComplete();
                        }
                        disposables.forEach(Disposable::dispose);
                    }
                }
            });

            disposables.add(parentDisp);
        });
    }
}
