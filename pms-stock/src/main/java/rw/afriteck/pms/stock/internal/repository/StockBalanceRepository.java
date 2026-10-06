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

    // Report 3: branch-wide total vs reorder level (damaged store excluded). Rows: [StockItem, BigDecimal total]
    @Query("""
            select i, sum(b.quantityOnHand) from StockBalance b join b.item i
            where b.store.branch.id = :branchId and b.store.storeType <> :excluded
              and i.active = true and i.reorderLevel is not null
            group by i
            having sum(b.quantityOnHand) <= i.reorderLevel
            """)
    List<Object[]> findLowStock(@Param("branchId") UUID branchId, @Param("excluded") EStoreType excluded);
}
