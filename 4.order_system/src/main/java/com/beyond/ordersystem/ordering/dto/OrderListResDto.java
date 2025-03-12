package com.beyond.ordersystem.ordering.dto;

import com.beyond.ordersystem.ordering.domain.OrderDetail;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class OrderListResDto {
    private Long id;
    private String memberEmail;
    private String orderStatus;
    private List<OrderDetailResDto> orderDetails;
}
