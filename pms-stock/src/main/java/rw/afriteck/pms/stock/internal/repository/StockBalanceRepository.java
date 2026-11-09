package rw.afriteck.pms.stock.internal.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rw.afriteck.pms.stock.internal.enums.EStoreType;
import rw.afriteck.pms.stock.internal.model.StockBalance;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StockBalanceRepository extends JpaRepository<StockBalance, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from StockBalance b where b.store.id = :storeId and b.item.id = :itemId")
    Optional<StockBalance> findForUpdate(@Param("storeId") UUID storeId, @Param("itemId") UUID itemId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from StockBalance b where b.store.id = :storeId and b.item.id in :itemIds order by b.item.id")
    List<StockBalance> findAllForUpdate(@Param("storeId") UUID storeId, @Param("itemIds") Collection<UUID> itemIds);

    Optional<StockBalance> findByStoreIdAndItemId(UUID storeId, UUID itemId);

    boolean existsByStoreIdAndItemId(UUID storeId, UUID itemId);

    List<StockBalance> findByStoreId(UUID storeId);

    List<StockBalance> findByStoreIdAndItemIdIn(UUID storeId, Collection<UUID> itemIds);

}
