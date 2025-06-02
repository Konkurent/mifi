package skillfactory.operators;

import skillfactory.api.Observable;
import skillfactory.api.Observer;

import java.util.function.Predicate;

public class FilterOperator<T> {

    public static <T> Observable<T> filter(Observable<T> source, Predicate<T> predicate) {
        return Observable.create(observer -> {
            source.subscribe(
                    new Observer<T>() {
                        @Override
                        public void onNext(T item) {
                            if (predicate.test(item)) {
                                observer.onNext(item);
                            }
                        }

                        @Override
                        public void onError(Throwable t) {
                            observer.onError(t);
                        }

                        @Override
                        public void onComplete() {
                            observer.onComplete();
                        }
                    }
            );
        });
    }

}
