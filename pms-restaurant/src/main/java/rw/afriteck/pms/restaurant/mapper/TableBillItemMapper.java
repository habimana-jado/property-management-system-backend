package rw.afriteck.pms.restaurant.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import rw.afriteck.pms.restaurant.dtos.TableBillItemResponse;
import rw.afriteck.pms.restaurant.dtos.TableBillItemSummary;
import rw.afriteck.pms.restaurant.model.TableBillItem;

import java.util.List;

@Mapper(componentModel = "spring", uses = MenuMasterMapper.class)
public interface TableBillItemMapper {

    @Mapping(target = "menuMaster", source = "menuMaster")
    TableBillItemResponse toResponse(TableBillItem restaurant);

    @Mapping(source = "menuMaster.menuItemName", target = "menuItemName")
    TableBillItemSummary toSummary(TableBillItem item);

    List<TableBillItemSummary> toSummaryList(List<TableBillItem> items);

}
