package rw.afriteck.pms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rw.afriteck.pms.model.TableMaster;

import java.util.UUID;

public interface TableMasterRepo extends JpaRepository<TableMaster, UUID> {
}
