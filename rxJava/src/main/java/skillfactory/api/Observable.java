package skillfactory.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import skillfactory.impl.DisposableImpl;
import skillfactory.sheduler.Scheduler;

public class Observable<T> {
    private static final Logger log = LoggerFactory.getLogger(Observable.class);
    
    private final Subscriber<T> subscriber;

    private Observable(Subscriber<T> subscriber) {
        this.subscriber = subscriber;
    }

    public static <T> Observable<T> create(Subscriber<T> source) {
        return new Observable<>(source);
    }

    @SafeVarargs
    public static <T> Observable<T> just(T... params) {
        return create(observer -> {
            for (T param : params) {
                observer.onNext(param);
            }
            observer.onComplete();
        });
    }

    public Observable<T> subscribeOn(Scheduler scheduler) {
        return Observable.create(observer ->
                scheduler.execute(() -> this.subscribe(observer))
        );
    }

    public Observable<T> observeOn(Scheduler scheduler) {
        return Observable.create(observer ->
                this.subscribe(new Observer<T>() {
                    @Override
                    public void onNext(T item) {
                        scheduler.execute(() -> observer.onNext(item));
                    }
                    @Override
                    public void onError(Throwable t) {
                        scheduler.execute(() -> observer.onError(t));
                    }
                    @Override
                    public void onComplete() {
                        scheduler.execute(observer::onComplete);
                    }
                })
        );
    }

    public Disposable subscribe(Observer<T> observer) {
        DisposableImpl disposable = new DisposableImpl();
        try {
            subscriber.subscribe(
                    new Observer<>() {
                        @Override
                        public void onNext(T item) {
                            if (!disposable.isDisposed()) {
                                observer.onNext(item);
                            }
                        }

                        @Override
                        public void onError(Throwable t) {
                            if (disposable.isDisposed()) return;
                            observer.onError(t);
                        }

                        @Override
                        public void onComplete() {
                            if (disposable.isDisposed()) return;
                            observer.onComplete();
                        }
                    }
            );
        } catch (Exception e) {
            log.error("Error during subscription", e);
            observer.onError(e);
        }
        return disposable;
    }

    public interface Subscriber<T> {
        void subscribe(Observer<T> observer) throws Exception;
    }

}
