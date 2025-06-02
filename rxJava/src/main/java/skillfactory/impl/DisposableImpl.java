package skillfactory.impl;

import skillfactory.api.Disposable;

import java.util.concurrent.atomic.AtomicBoolean;

public class DisposableImpl implements Disposable {
    private final AtomicBoolean disposed = new AtomicBoolean(false);

    @Override
    public void dispose() {
        disposed.set(true);
    }

    public boolean isDisposed() {
        return disposed.get();
    }
}
