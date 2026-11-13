package rw.afriteck.pms.stock.internal.service;

final class Paging {
    static int size(int requested) {
        return Math.min(Math.max(requested, 1), 200);
    }

    static int page(int requested) {
        return Math.min(Math.max(requested, 0), 100_000);
    }
}
