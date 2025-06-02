package skillfactory.api;


import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Observable<T> {
    private final Subscriber<T> subscriber;

    private Observable(Subscriber<T> subscriber) {
        this.subscriber = subscriber;
    }

    public static <T> Observable<T> create(Subscriber<T> source) {
        return new Observable<>(source);
    }

    public static <T> Observable<T> just(T... params) {
        return create(observer -> {
            for (T param : params) {
                observer.onNext(param);
            }
            observer.onComplete();
        });
    }

    public void subscribe(Observer<T> observer) {
        try {
            subscriber.subscribe(observer);
        } catch (Exception e) {
            log.error("Error during subscription", e);
            observer.onError(e);
        }
    }

    public interface Subscriber<T> {
        void subscribe(Observer<T> observer) throws Exception;
    }
}
