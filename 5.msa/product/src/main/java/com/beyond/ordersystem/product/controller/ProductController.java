package com.beyond.ordersystem.product.controller;

import com.beyond.ordersystem.product.domain.Product;
import com.beyond.ordersystem.product.dto.ProductRegisterDto;
import com.beyond.ordersystem.product.dto.ProductResDto;
import com.beyond.ordersystem.product.dto.ProductSearchDto;
import com.beyond.ordersystem.product.dto.ProductUpdateStockDto;
import com.beyond.ordersystem.product.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/product")
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping("/create")
    @PreAuthorize(("hasRole('ADMIN')"))
    public ResponseEntity<?> productCreate(ProductRegisterDto dto){
        Product product = productService.productCreate(dto);
        return new ResponseEntity<>(product.getId(), HttpStatus.CREATED);
    }

    @GetMapping("/list")
    public ResponseEntity<?> productList(Pageable pageable, ProductSearchDto dto){
        Page<ProductResDto> productResDtos = productService.findAll(pageable,dto);
        return new ResponseEntity<>(productResDtos,HttpStatus.OK);

    }

    @GetMapping("/{id}")
    public ResponseEntity<?> productDetail(@PathVariable Long id){
        ProductResDto productResDto = productService.productDetail(id);
        return new ResponseEntity<>(productResDto, HttpStatus.OK);
    }

    @PutMapping("/updatestock")
    public ResponseEntity<?> productStockUpdate(@RequestBody ProductUpdateStockDto dto){
        Product product = productService.updateStockQuantity(dto);
        return new ResponseEntity<>(product.getId(),HttpStatus.OK);
    }
}
