package rw.afriteck.pms.stock.internal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rw.afriteck.pms.stock.api.enums.EMovementSourceType;
import rw.afriteck.pms.stock.internal.model.StockMovement;

import java.util.List;
import java.util.UUID;

public interface StockMovementRepository extends JpaRepository<StockMovement, UUID> {

    @Query("""
        select m from StockMovement m
        where m.sourceType = :type and m.sourceId = :sourceId
          and (:lineId is null or m.sourceLineId = :lineId)
          and m.reversalOf is null
          and not exists (select 1 from StockMovement r where r.reversalOf = m)
        """)
    List<StockMovement> findReversible(@Param("type") EMovementSourceType type,
                                       @Param("sourceId") UUID sourceId,
                                       @Param("lineId") UUID lineId);
}
