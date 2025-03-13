package com.beyond.ordersystem.product.domain;

import com.beyond.ordersystem.product.dto.ProductResDto;
import jakarta.persistence.*;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
@Entity
@Builder
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String category;
    private Integer price;
    private Integer stockQuantity;
    private String imagePath;
    private String memberEmail;

    public ProductResDto fromEntity(){
        return ProductResDto.builder().id(this.id).name(this.name).category(this.category).
                price(this.price).stockQuantity(this.stockQuantity).imagePath(this.imagePath).build();
    }

    public void updateImagePath(String imagePath){
        this.imagePath = imagePath;
    }

    public void updateStockQuantity(int productCount){
        this.stockQuantity = this.stockQuantity- productCount;
    }

    public void canceleOrder(int canceledQuantity){
        this.stockQuantity = this.stockQuantity+canceledQuantity;
    }
}
