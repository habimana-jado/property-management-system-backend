package rw.afriteck.pms.restaurant.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import rw.afriteck.pms.common.enums.ERecordStatus;
import rw.afriteck.pms.restaurant.enums.EBillStatus;
import rw.afriteck.pms.restaurant.enums.ETableStatus;
import rw.afriteck.pms.restaurant.model.TableBill;
import rw.afriteck.pms.restaurant.model.TableMaster;
import rw.afriteck.pms.restaurant.repository.TableBillRepo;
import rw.afriteck.pms.restaurant.repository.TableMasterRepo;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplActiveBillTest {

    @Mock
    TableBillRepo tableBillRepo;
    @Mock
    TableMasterRepo tableMasterRepo;
    @InjectMocks
    OrderServiceImpl orderService;

    private TableMaster table(ETableStatus status) {
        TableMaster t = new TableMaster();
        t.setId(UUID.randomUUID());
        t.setTableStatus(status);
        t.setRecordStatus(ERecordStatus.ACTIVE);
        return t;
    }

    private TableBill activeBillOn(TableMaster table, EBillStatus status) {
        TableBill b = new TableBill();
        b.setId(UUID.randomUUID());
        b.setBillStatus(status);
        when(tableBillRepo.findActiveBillByTableId(eq(table.getId()), anyList())).thenReturn(Optional.of(b));
        return b;
    }

    @Test
    void aRequestedBillIsReopenedWhenOrderingContinues() {
        TableMaster t = table(ETableStatus.BILLED);
        TableBill bill = activeBillOn(t, EBillStatus.BILL_REQUESTED);

        TableBill result = orderService.retrieveOrCreateActiveBill(t);

        assertThat(result).isSameAs(bill);
        assertThat(result.getBillStatus()).isEqualTo(EBillStatus.OPEN);
    }

    @Test
    void aBilledTableBecomesOccupiedAgainAndIsSavedOnce() {
        TableMaster t = table(ETableStatus.BILLED);
        activeBillOn(t, EBillStatus.BILL_REQUESTED);

        orderService.retrieveOrCreateActiveBill(t);

        assertThat(t.getTableStatus()).isEqualTo(ETableStatus.OCCUPIED);
        verify(tableMasterRepo, times(1)).save(t);
    }

    @Test
    void anOpenBillOnAnOccupiedTableIsLeftAlone() {
        TableMaster t = table(ETableStatus.OCCUPIED);
        TableBill bill = activeBillOn(t, EBillStatus.OPEN);

        TableBill result = orderService.retrieveOrCreateActiveBill(t);

        assertThat(result.getBillStatus()).isEqualTo(EBillStatus.OPEN);
        verify(tableMasterRepo, never()).save(any());
        assertThat(result).isSameAs(bill);
    }
}
