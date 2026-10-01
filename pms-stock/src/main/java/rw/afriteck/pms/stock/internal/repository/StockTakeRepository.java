package rw.afriteck.pms.stock.internal.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rw.afriteck.pms.stock.internal.enums.EStocktakeStatus;
import rw.afriteck.pms.stock.internal.model.Stocktake;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface StockTakeRepository extends JpaRepository<Stocktake, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Stocktake s where s.id = :id")
    Optional<Stocktake> findForUpdate(@Param("id") UUID id);

    boolean existsByStoreIdAndStatusIn(UUID storeId, Collection<EStocktakeStatus> statuses);
}
