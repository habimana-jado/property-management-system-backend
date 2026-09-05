package rw.afriteck.pms.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rw.afriteck.pms.dtos.CreateMenuCategoryRequest;
import rw.afriteck.pms.dtos.MenuCategoryResponse;
import rw.afriteck.pms.service.IMenuCategoryService;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pms/menu-categories")
@RequiredArgsConstructor
public class MenuCategoryController {
    private final IMenuCategoryService menuCategoryService;

    @PostMapping
    public ResponseEntity<MenuCategoryResponse> register(@Valid @RequestBody CreateMenuCategoryRequest menuCategoryRequest){
        return ResponseEntity.ok(menuCategoryService.register(menuCategoryRequest));
    }

    @GetMapping
    public ResponseEntity<Page<MenuCategoryResponse>> findAll(@PageableDefault(size = 20, sort = "id") Pageable pageable){
        return ResponseEntity.ok(menuCategoryService.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MenuCategoryResponse> findOne(@PathVariable("id")UUID id){
        return ResponseEntity.ok(menuCategoryService.findOne(id));
    }

}
