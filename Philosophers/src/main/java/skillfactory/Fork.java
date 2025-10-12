package skillfactory;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Fork {

    private final Lock lock = new ReentrantLock();

    public boolean locked;

    public boolean get() {
        if (lock.tryLock()) {
            locked = true;
            return true;
        } else {
            return false;
        }
    }

    public void put() {
        locked = false;
        lock.unlock();
    }

}
