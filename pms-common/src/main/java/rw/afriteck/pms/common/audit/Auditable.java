package rw.afriteck.pms.common.audit;


import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
public abstract class Auditable {

    //TODO: Make this field nullable = false, check changes to make on data already in DB
    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    //TODO: Make this field nullable = false, check changes to make on data already in DB
    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;

    @CreatedBy
    @Column(name = "created_by_user_id", updatable = false)
    private UUID createdByUserId;

    @LastModifiedBy
    @Column(name = "updated_by_user_id")
    private UUID updatedByUserId;
}
