package rw.afriteck.pms.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rw.afriteck.pms.dtos.CreateTableMasterRequest;
import rw.afriteck.pms.dtos.TableMasterResponse;
import rw.afriteck.pms.service.ITableMasterService;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pms/tables")
@RequiredArgsConstructor
public class TableMasterController {
    private final ITableMasterService tableMasterService;

    @PostMapping
    public ResponseEntity<TableMasterResponse> register(@Valid @RequestBody CreateTableMasterRequest tableMasterRequest){
        return ResponseEntity.ok(tableMasterService.register(tableMasterRequest));
    }

    @GetMapping
    public ResponseEntity<Page<TableMasterResponse>> findAll(@PageableDefault(size = 20, sort = "id")Pageable pageable){
        return ResponseEntity.ok(tableMasterService.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TableMasterResponse> findOne(@PathVariable("id")UUID id){
        return ResponseEntity.ok(tableMasterService.findOne(id));
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<TableMasterResponse> activate(@PathVariable("id") UUID id){
        return ResponseEntity.ok(tableMasterService.activate(id));
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<TableMasterResponse> deactivate(@PathVariable("id") UUID id){
        return ResponseEntity.ok(tableMasterService.deactivate(id));
    }


}
