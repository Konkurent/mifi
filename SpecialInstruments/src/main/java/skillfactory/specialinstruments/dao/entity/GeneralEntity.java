package skillfactory.specialinstruments.dao.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;

@Getter
@MappedSuperclass
@Where(clause = "deleted = false")
public class GeneralEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "otp_id_seq")
    @SequenceGenerator(name = "otp_id_seq", sequenceName = "otp_id_seq", allocationSize = 1)
    private Long id;

    @Setter
    private Boolean active;

    @Setter
    private Boolean deleted;

    private LocalDateTime createDateTime;

    private LocalDateTime updateDateTime;

    public GeneralEntity() {
        this.active = true;
        this.deleted = false;
        this.createDateTime = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updateDateTime = LocalDateTime.now();
    }

    // ---------------- Super Builder for super entity

    protected GeneralEntity(GeneralEntityBuilder<?, ?> b) {
        this();
    }

    public static GeneralEntityBuilder<?, ?> builder() {
        return new GeneralEntityBuilderImpl();
    }

    public static abstract class GeneralEntityBuilder<C extends GeneralEntity, B extends GeneralEntityBuilder<C, B>> {

        protected abstract B self();

        public abstract C build();
    }

    private static final class GeneralEntityBuilderImpl extends GeneralEntityBuilder<GeneralEntity, GeneralEntityBuilderImpl> {
        private GeneralEntityBuilderImpl() {
        }

        protected GeneralEntityBuilderImpl self() {
            return this;
        }

        public GeneralEntity build() {
            return new GeneralEntity(this);
        }
    }
}
