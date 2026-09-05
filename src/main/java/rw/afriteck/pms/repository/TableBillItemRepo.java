package rw.afriteck.pms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rw.afriteck.pms.model.TableBillItem;

import java.util.UUID;

public interface TableBillItemRepo extends JpaRepository<TableBillItem, UUID> {
}
