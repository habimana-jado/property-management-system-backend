package rw.afriteck.pms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rw.afriteck.pms.model.MenuMaster;

import java.util.UUID;

public interface MenuMasterRepo extends JpaRepository<MenuMaster, UUID> {
}
