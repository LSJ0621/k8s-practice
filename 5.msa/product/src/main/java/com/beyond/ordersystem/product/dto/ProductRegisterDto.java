package com.beyond.ordersystem.product.dto;

import com.beyond.ordersystem.product.domain.Product;
import com.beyond.ordersystem.product.service.ProductService;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ProductRegisterDto {
    @NotEmpty
    private String name;
    @NotEmpty
    private String category;
    @NotEmpty
    private int price;
    @NotEmpty
    private int stockQuantity;
    private MultipartFile productImage;

    public Product toEntity(String email) {
        return Product.builder().name(this.name).category(this.category).price(this.price).stockQuantity(this.stockQuantity)
                .memberEmail(email).build();
    }
}
