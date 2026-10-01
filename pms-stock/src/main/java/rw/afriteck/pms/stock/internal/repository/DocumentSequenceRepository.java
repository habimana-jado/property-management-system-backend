package rw.afriteck.pms.stock.internal.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rw.afriteck.pms.stock.api.enums.EDocumentType;
import rw.afriteck.pms.stock.internal.model.DocumentSequence;

import java.util.Optional;
import java.util.UUID;

public interface DocumentSequenceRepository extends JpaRepository<DocumentSequence, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from DocumentSequence s where s.branch.id = :branchId and s.documentType = :type")
    Optional<DocumentSequence> findForUpdate(@Param("branchId") UUID branchId, @Param("type") EDocumentType type);
}
