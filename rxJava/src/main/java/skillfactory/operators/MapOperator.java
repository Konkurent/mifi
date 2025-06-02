package skillfactory.operators;

import skillfactory.api.Observable;
import skillfactory.api.Observer;

import java.util.function.Function;

public class MapOperator {

    public static <T, R> Observable<R> map(Observable<T> source, Function<T, R> mapper) {
        return Observable.create(observer -> {
            source.subscribe(new Observer<T>() {
                @Override
                public void onNext(T item) {
                    observer.onNext(mapper.apply(item));
                }

                @Override
                public void onError(Throwable t) {
                    observer.onError(t);
                }

                @Override
                public void onComplete() {
                    observer.onComplete();
                }
            });
        });
    }

}
