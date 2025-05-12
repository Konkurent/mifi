package skillfactory.specialinstruments.dao.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import skillfactory.specialinstruments.dao.repositories.OTPConfigurationRepository;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Component
@RequiredArgsConstructor
public class OTPConfigurationFasade implements OTPConfiguration{

    @JsonIgnore
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    @JsonIgnore
    private OTPConfigurationEntity mirror;
    @JsonIgnore
    private final OTPConfigurationRepository repository;

    @PostConstruct
    public void init() {
        mirror = repository.findById(1L)
                .orElseThrow(() -> new RuntimeException("Configuration not found"));
    }

    @Override
    public Integer getDuration() {
        lock.readLock().lock();
        try {
            return mirror.getDuration();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Integer getLength() {
        lock.readLock().lock();
        try {
            return mirror.getLength();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void setDuration(Integer duration) {
        lock.writeLock().lock();
        try {
            mirror.setDuration(duration);
            this.mirror = repository.save(mirror);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void setLength(Integer length) {
        lock.writeLock().lock();
        try {
            mirror.setLength(length);
            this.mirror = repository.save(mirror);
        } finally {
            lock.writeLock().unlock();
        }
    }
}
