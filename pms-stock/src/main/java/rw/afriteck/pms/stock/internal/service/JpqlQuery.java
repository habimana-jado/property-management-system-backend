package rw.afriteck.pms.stock.internal.service;

import jakarta.persistence.Query;
import java.util.LinkedHashMap;
import java.util.Map;

/** Builds the WHERE clause and its named parameters together, so they can never get out of step. */
final class JpqlQuery {

    private final StringBuilder where = new StringBuilder();
    private final Map<String, Object> params = new LinkedHashMap<>();

    /** A condition with no parameter, e.g. "b.quantityOnHand <> 0". */
    JpqlQuery and(String condition) {
        where.append(where.length() == 0 ? " where " : " and ").append(condition);
        return this;
    }

    /** A condition with a named parameter. The name in the text (":storeId") must match 'param' ("storeId"). */
    JpqlQuery and(String condition, String param, Object value) {
        params.put(param, value);
        return and(condition);
    }

    String where() {
        return where.toString();
    }

    /** Sets every collected parameter on the query (works for both the count query and the data query). */
    void bind(Query query) {
        for (Map.Entry<String, Object> p : params.entrySet()) {
            query.setParameter(p.getKey(), p.getValue());
        }
    }
}