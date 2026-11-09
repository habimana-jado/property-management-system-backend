package rw.afriteck.pms.stock.internal.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.afriteck.pms.common.exception.StockNotFoundException;
import rw.afriteck.pms.common.exception.StockRuleViolationException;
import rw.afriteck.pms.stock.api.StockQueryApi;
import rw.afriteck.pms.stock.api.query.DocumentFilter;
import rw.afriteck.pms.stock.api.query.MovementFilter;
import rw.afriteck.pms.stock.api.query.StockOnHandFilter;
import rw.afriteck.pms.stock.api.query.StocktakeFilter;
import rw.afriteck.pms.stock.api.view.*;
import rw.afriteck.pms.stock.internal.enums.EStoreType;
import rw.afriteck.pms.stock.internal.model.StockDocument;
import rw.afriteck.pms.stock.internal.model.Stocktake;
import rw.afriteck.pms.stock.internal.model.Store;
import rw.afriteck.pms.stock.internal.repository.StockDocumentRepository;
import rw.afriteck.pms.stock.internal.repository.StockTakeRepository;

import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
class StockQueryServiceImpl implements StockQueryApi {

    private static final int MAX_PAGE_SIZE = 200;
    private static final int MAX_PAGE = 100_000;

    // constructor expressions build the records directly: no entities loaded, no N+1
    private static final String ON_HAND_SELECT = "select new " + StockOnHandRow.class.getName()
            + "(s.id, s.name, i.id, i.code, i.name, c.id, c.name, u.code, b.quantityOnHand)";
    private static final String ON_HAND_FROM = " from StockBalance b join b.store s join b.item i"
            + " join i.category c join i.baseUnit u";

    private static final String MOVEMENT_SELECT = "select new " + MovementRow.class.getName()
            + "(m.id, m.occurredAt, s.id, s.name, i.id, i.code, i.name, m.movementType, m.quantity,"
            + " m.balanceAfter, d.id, d.documentNo, m.sourceType, m.sourceId, m.sourceLineId,"
            + " m.reversalOf.id, m.reason, m.note, m.performedBy)";
    private static final String MOVEMENT_FROM = " from StockMovement m join m.store s join m.item i"
            + " left join m.documentLine dl left join dl.document d";

    private static final String DOCUMENT_SELECT = "select new " + StockDocumentSummary.class.getName()
            + "(d.id, d.documentNo, d.documentType, d.purpose, d.status, s.id, s.name, c.id, c.name,"
            + " d.postedBy, d.postedAt)";
    private static final String DOCUMENT_FROM = " from StockDocument d join d.store s left join d.counterpartStore c";

    private static final String STOCKTAKE_SELECT = "select new " + StocktakeSummary.class.getName()
            + "(t.id, s.id, s.name, t.status, t.startedBy, t.startedAt, t.approvedAt, t.adjustmentDocument.id)";
    private static final String STOCKTAKE_FROM = " from Stocktake t join t.store s";

    @PersistenceContext
    private EntityManager em;

    private final StockDocumentRepository documents;
    private final StockTakeRepository stocktakes;

    // ---------------- stock on hand ----------------
    @Override
    public PageResult<StockOnHandRow> stockOnHand(StockOnHandFilter f, int page, int size) {
        require(f.branchId() != null, "branchId is required");
        JpqlQuery q = new JpqlQuery().and("s.branch.id = :branchId", "branchId", f.branchId());
        if (f.storeId() != null)    q.and("s.id = :storeId", "storeId", f.storeId());
        if (f.categoryId() != null) q.and("c.id = :categoryId", "categoryId", f.categoryId());
        if (hasText(f.search())) {
            q.and("(lower(i.name) like :search or lower(i.code) like :search)",
                    "search", "%" + f.search().trim().toLowerCase() + "%");
        }
        if (!f.includeZero()) q.and("b.quantityOnHand <> 0");
        return fetchPage(ON_HAND_SELECT, ON_HAND_FROM, q, " order by i.name, s.name", "b",
                StockOnHandRow.class, page, size);
    }

    // ---------------- movement history ----------------
    @Override
    public PageResult<MovementRow> movements(MovementFilter f, int page, int size) {
        // the ledger is the biggest table: never allow an unbounded scan
        require(f.storeId() != null || f.itemId() != null || f.documentId() != null
                        || f.sourceId() != null || f.from() != null,
                "Filter movements by store, item, document, source or a start date");
        require(f.sourceId() == null || f.sourceType() != null, "sourceType is required with sourceId");

        JpqlQuery q = new JpqlQuery();
        if (f.storeId() != null) q.and("s.id = :storeId", "storeId", f.storeId());
        if (f.itemId() != null)  q.and("i.id = :itemId", "itemId", f.itemId());
        if (f.types() != null && !f.types().isEmpty()) q.and("m.movementType in :types", "types", f.types());
        if (f.from() != null) q.and("m.occurredAt >= :fromTs", "fromTs", f.from());
        if (f.to() != null)   q.and("m.occurredAt < :toTs", "toTs", f.to());
        if (f.documentId() != null) q.and("d.id = :documentId", "documentId", f.documentId());
        if (f.sourceId() != null) {
            q.and("m.sourceType = :sourceType", "sourceType", f.sourceType());
            q.and("m.sourceId = :sourceId", "sourceId", f.sourceId());
        }
        return fetchPage(MOVEMENT_SELECT, MOVEMENT_FROM, q, " order by m.occurredAt desc, m.id desc", "m",
                MovementRow.class, page, size);
    }

    // ---------------- low stock (branch-wide total vs reorder level) ----------------
    @Override
    public List<LowStockAlert> lowStock(UUID branchId) {
        require(branchId != null, "branchId is required");

        // 1. active items that have a reorder level
        List<Object[]> candidates = em.createQuery("""
                select i.id, i.code, i.name, u.code, i.reorderLevel
                from StockItem i join i.baseUnit u
                where i.active = true and i.reorderLevel is not null
                order by i.name""", Object[].class).getResultList();
        if (candidates.isEmpty()) return List.of();
        List<UUID> ids = candidates.stream().map(r -> (UUID) r[0]).toList();

        // 2. their branch-wide totals, damaged store excluded (unusable stock)
        Map<UUID, BigDecimal> totals = new HashMap<>();
        em.createQuery("""
                select b.item.id, sum(b.quantityOnHand) from StockBalance b
                where b.store.branch.id = :branchId and b.store.storeType <> :excluded and b.item.id in :ids
                group by b.item.id""", Object[].class)
                .setParameter("branchId", branchId)
                .setParameter("excluded", EStoreType.DAMAGED)
                .setParameter("ids", ids)
                .getResultList()
                .forEach(r -> totals.put((UUID) r[0], (BigDecimal) r[1]));

        // 3. low = total <= reorder level. An item with no balance rows counts as 0, so never-stocked items are flagged
        List<Object[]> low = candidates.stream()
                .filter(r -> totals.getOrDefault((UUID) r[0], BigDecimal.ZERO).compareTo((BigDecimal) r[4]) <= 0)
                .toList();
        if (low.isEmpty()) return List.of();

        // 4. per-store breakdown for the low items only, in one query
        Map<UUID, List<StoreQuantity>> byItem = new HashMap<>();
        em.createQuery("""
                select b.item.id, s.id, s.name, b.quantityOnHand
                from StockBalance b join b.store s
                where s.branch.id = :branchId and s.storeType <> :excluded and b.item.id in :ids
                order by s.name""", Object[].class)
                .setParameter("branchId", branchId)
                .setParameter("excluded", EStoreType.DAMAGED)
                .setParameter("ids", low.stream().map(r -> (UUID) r[0]).toList())
                .getResultList()
                .forEach(r -> byItem.computeIfAbsent((UUID) r[0], k -> new ArrayList<>())
                        .add(new StoreQuantity((UUID) r[1], (String) r[2], (BigDecimal) r[3])));

        return low.stream().map(r -> new LowStockAlert(
                (UUID) r[0], (String) r[1], (String) r[2], (String) r[3], (BigDecimal) r[4],
                totals.getOrDefault((UUID) r[0], BigDecimal.ZERO),
                byItem.getOrDefault((UUID) r[0], List.of()))).toList();
    }

    // ---------------- documents ----------------
    @Override
    public PageResult<StockDocumentSummary> documents(DocumentFilter f, int page, int size) {
        require(f.branchId() != null, "branchId is required");
        JpqlQuery q = new JpqlQuery().and("s.branch.id = :branchId", "branchId", f.branchId());
        if (f.storeId() != null) q.and("(s.id = :storeId or c.id = :storeId)", "storeId", f.storeId());
        if (f.type() != null)    q.and("d.documentType = :docType", "docType", f.type());
        if (f.status() != null)  q.and("d.status = :docStatus", "docStatus", f.status());
        if (f.from() != null)    q.and("d.postedAt >= :fromTs", "fromTs", f.from());
        if (f.to() != null)      q.and("d.postedAt < :toTs", "toTs", f.to());
        return fetchPage(DOCUMENT_SELECT, DOCUMENT_FROM, q, " order by d.postedAt desc, d.id desc", "d",
                StockDocumentSummary.class, page, size);
    }

    @Override
    public StockDocumentView document(UUID id) {
        StockDocument d = documents.findDetailedById(id)
                .orElseThrow(() -> new StockNotFoundException("Stock document not found"));
        Store counterpart = d.getCounterpartStore();
        List<DocumentLineView> lines = d.getLines().stream()
                .sorted(Comparator.comparing(l -> l.getItem().getName()))
                .map(l -> new DocumentLineView(l.getItem().getId(), l.getItem().getCode(), l.getItem().getName(),
                        l.getItem().getBaseUnit().getCode(), l.getQuantity(), l.getEnteredQuantity(),
                        l.getEnteredUnit().getCode()))
                .toList();
        return new StockDocumentView(d.getId(), d.getDocumentNo(), d.getDocumentType(), d.getPurpose(),
                d.getStatus(), d.getStore().getId(), d.getStore().getName(),
                counterpart == null ? null : counterpart.getId(),
                counterpart == null ? null : counterpart.getName(),
                d.getSupplierName(), d.getExternalReference(), d.getNote(),
                d.getPostedBy(), d.getPostedAt(), lines);
    }

    // ---------------- stocktakes ----------------
    @Override
    public PageResult<StocktakeSummary> stocktakes(StocktakeFilter f, int page, int size) {
        require(f.branchId() != null, "branchId is required");
        JpqlQuery q = new JpqlQuery().and("s.branch.id = :branchId", "branchId", f.branchId());
        if (f.storeId() != null) q.and("s.id = :storeId", "storeId", f.storeId());
        if (f.status() != null)  q.and("t.status = :status", "status", f.status());
        return fetchPage(STOCKTAKE_SELECT, STOCKTAKE_FROM, q, " order by t.startedAt desc, t.id desc", "t",
                StocktakeSummary.class, page, size);
    }

    @Override
    public StocktakeView stocktake(UUID id) {
        Stocktake t = stocktakes.findDetailedById(id)
                .orElseThrow(() -> new StockNotFoundException("Stocktake not found"));
        List<StocktakeLineView> lines = t.getLines().stream()
                .sorted(Comparator.comparing(l -> l.getItem().getName()))
                .map(l -> new StocktakeLineView(l.getItem().getId(), l.getItem().getCode(), l.getItem().getName(),
                        l.getItem().getBaseUnit().getCode(), l.getExpectedQuantity(),
                        l.getCountedQuantity(), l.getVariance()))     // variance is null while uncounted
                .toList();
        return new StocktakeView(t.getId(), t.getStore().getId(), t.getStore().getName(), t.getStatus(),
                t.getStartedBy(), t.getStartedAt(), t.getApprovedBy(), t.getApprovedAt(), t.getNote(),
                t.getAdjustmentDocument() == null ? null : t.getAdjustmentDocument().getId(), lines);
    }

    // ---------------- helpers ----------------
    private <T> PageResult<T> fetchPage(String select, String from, JpqlQuery q, String orderBy,
                                        String countTarget, Class<T> type, int page, int size) {
        int pageSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        int pageNo = Math.min(Math.max(page, 0), MAX_PAGE);

        TypedQuery<Long> count = em.createQuery("select count(" + countTarget + ")" + from + q.where(), Long.class);
        q.bind(count);
        long total = count.getSingleResult();
        if (total == 0) return new PageResult<>(List.of(), pageNo, pageSize, 0);

        TypedQuery<T> data = em.createQuery(select + from + q.where() + orderBy, type);
        q.bind(data);
        data.setFirstResult(pageNo * pageSize);
        data.setMaxResults(pageSize);
        return new PageResult<>(data.getResultList(), pageNo, pageSize, total);
    }

    private static boolean hasText(String s) { return s != null && !s.isBlank(); }

    private static void require(boolean condition, String message) {
        if (!condition) throw new StockRuleViolationException(message);
    }
}
