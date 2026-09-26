package rw.afriteck.pms.restaurant.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import rw.afriteck.pms.restaurant.dtos.KitchenTicketGroupResponse;
import rw.afriteck.pms.restaurant.dtos.OrderPlacementResponse;
import rw.afriteck.pms.restaurant.dtos.TableBillItemResponse;
import rw.afriteck.pms.restaurant.enums.EMenuItemType;
import rw.afriteck.pms.restaurant.model.TableBill;
import rw.afriteck.pms.restaurant.model.TableBillItem;
import rw.afriteck.pms.restaurant.model.TableMaster;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = TableBillItemMapper.class)
public interface OrderMapper {

    @Mapping(source = "bill.billNo", target = "billNo")
    @Mapping(source = "table.tableNumber", target = "tableNumber")
    @Mapping(target = "ticketGroups", expression = "java(groupByType(newItems))")
    OrderPlacementResponse toPlacementResponse(TableBill bill, TableMaster table, List<TableBillItem> newItems);

    default List<KitchenTicketGroupResponse> groupByType(List<TableBillItem> items) {
        Map<EMenuItemType, List<TableBillItem>> grouped = items.stream()
                .collect(Collectors.groupingBy(
                        i -> i.getMenuMaster().getMenuItemType(),
                        () -> new EnumMap<>(EMenuItemType.class),
                        Collectors.toList()
                ));

        return grouped.entrySet().stream()
                .map(e -> new KitchenTicketGroupResponse(
                        e.getKey(),
                        e.getValue().stream().map(this::toTableBillItemResponse).toList()
                ))
                .toList();
    }

    TableBillItemResponse toTableBillItemResponse(TableBillItem item);
}

