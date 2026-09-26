package rw.afriteck.pms.restaurant.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import rw.afriteck.pms.restaurant.dtos.BillSnapshotResponse;
import rw.afriteck.pms.restaurant.dtos.CombinedBillLineResponse;
import rw.afriteck.pms.restaurant.model.TableBill;
import rw.afriteck.pms.restaurant.model.TableMaster;

import java.util.List;

@Mapper(componentModel = "spring", uses = TableBillItemMapper.class)
public interface BillSnapshotMapper {

    @Mapping(source = "tableBill.billNo", target = "billNo")
    @Mapping(source = "tableBill.billStatus", target = "billStatus")
    @Mapping(source = "tableBill.subtotal", target = "subtotal")
    @Mapping(source = "tableBill.taxAmount", target = "taxAmount")
    @Mapping(source = "tableBill.discountAmount", target = "discountAmount")
    @Mapping(source = "tableBill.totalAmount", target = "totalAmount")
    @Mapping(source = "tableMaster.tableNumber", target = "tableNumber")
    @Mapping(source = "itemsCombined", target = "tableBillItemsCombined")
    BillSnapshotResponse toBillSnapshotResponse(TableBill tableBill, TableMaster tableMaster, List<CombinedBillLineResponse> itemsCombined);
}