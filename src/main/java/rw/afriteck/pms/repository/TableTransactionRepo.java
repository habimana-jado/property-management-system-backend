package rw.afriteck.pms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rw.afriteck.pms.model.TableTransaction;

import java.util.UUID;

public interface TableTransactionRepo extends JpaRepository<TableTransaction, UUID> {
}
