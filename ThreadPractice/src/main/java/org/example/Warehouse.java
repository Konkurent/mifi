package org.example;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Warehouse {

    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Lock readLock = lock.readLock();
    private final Lock writeLock = lock.writeLock();

    private final Map<String, Integer> storage = new HashMap<>();

    private volatile AtomicInteger counter = new AtomicInteger();

    public boolean addItem(String itemType, int count) {
        try {
            boolean lock = writeLock.tryLock(1000, TimeUnit.MICROSECONDS);
            if (!lock) return false;
            try {
                storage.put(itemType, storage.getOrDefault(itemType, 0) + count);
                counter.addAndGet(count);
                return true;
            } finally {
                writeLock.unlock();
            }
        } catch (InterruptedException e) {
            return false;
        }
    }

    public boolean removeItem(String itemType, int count) {
        writeLock.lock();
        try {
            if (storage.getOrDefault(itemType, 0) < count) {
                return false;
            }
            return addItem(itemType, count * -1);
        } finally {
            writeLock.unlock();
        }
    }

    public int getItemCount(String itemType) {
        readLock.lock();
        try {
            return storage.getOrDefault(itemType, 0);
        } finally {
            readLock.unlock();
        }
    }

    public boolean transfer(Warehouse other, String itemType, int count) {
        writeLock.lock();
        try {
            if (removeItem(itemType, count)) {
                return other.addItem(itemType, count);
            }
            return false;
        } finally {
            writeLock.unlock();
        }
    }

}
