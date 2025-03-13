package com.beyond.ordersystem.ordering.service;

import com.beyond.ordersystem.common.config.RestTemplateConfig;
import com.beyond.ordersystem.common.dto.StockRabbitDto;
import com.beyond.ordersystem.common.service.StockInventoryService;
import com.beyond.ordersystem.common.service.StockRabbitmqService;
import com.beyond.ordersystem.ordering.domain.OrderDetail;
import com.beyond.ordersystem.ordering.domain.OrderStatus;
import com.beyond.ordersystem.ordering.domain.Ordering;
import com.beyond.ordersystem.ordering.dto.*;
import com.beyond.ordersystem.ordering.repository.OrderingDetailRepository;
import com.beyond.ordersystem.ordering.repository.OrderingRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.*;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@Transactional
public class OrderingService {
    private final OrderingRepository orderingRepository;
    private final OrderingDetailRepository orderingDetailRepository;
    private final StockInventoryService stockInventoryService;
    private final StockRabbitmqService stockRabbitmqService;
    private final RestTemplate restTemplate;
    private final ProductFeign productFeign;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public OrderingService(OrderingRepository orderingRepository, OrderingDetailRepository orderingDetailRepository, StockInventoryService stockInventoryService, StockRabbitmqService stockRabbitmqService, RestTemplate restTemplate, ProductFeign productFeign, KafkaTemplate<String, Object> kafkaTemplate) {
        this.orderingRepository = orderingRepository;
        this.orderingDetailRepository = orderingDetailRepository;
        this.stockInventoryService = stockInventoryService;
        this.stockRabbitmqService = stockRabbitmqService;
        this.restTemplate = restTemplate;
        this.productFeign = productFeign;
        this.kafkaTemplate = kafkaTemplate;
    }

    public Ordering orderCreate(List<OrderCreateDto> dtos){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Ordering ordering = Ordering.builder().memberEmail(email).build();
        for(OrderCreateDto o:dtos){
//            product서버에 api요청을 통해 product객체를 받아와야함 ->동기적 처리 필수
            String productGetUrl = "http://seongjae-msa-product-service/product/"+o.getProductId();
            String token = SecurityContextHolder.getContext().getAuthentication().getCredentials().toString();
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization",token);
            HttpEntity<String> httpEntity = new HttpEntity<>(headers);
            ResponseEntity<ProductDto> response = restTemplate.exchange(productGetUrl, HttpMethod.GET,httpEntity,ProductDto.class);
            ProductDto productDto = response.getBody();
            System.out.println(productDto);
            int quantity = o.getProductCount();
            if(productDto.getStockQuantity()<quantity){
                throw new IllegalArgumentException("재고가 부족합니다");
            }else{
//                재고 감소 api요청을 product서버에 보내야함 -> 비동기처리 가능
                String productUpdateStockUrl = "http://seongjae-msa-product-service/product/updatestock";
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<ProductUpdateStockDto> updateEntity = new HttpEntity<>(
                        ProductUpdateStockDto.builder()
                                .productId(o.getProductId())
                                .productQuantity(o.getProductCount()).build()
                        ,headers
                );
                restTemplate.exchange(productUpdateStockUrl, HttpMethod.PUT,updateEntity, Void.class);
            }

            OrderDetail orderDetail = OrderDetail.builder().ordering(ordering).productId(o.getProductId()).
                    quantity(o.getProductCount()).build();
            ordering.getOrderDetails().add(orderDetail);
        }
        orderingRepository.save(ordering);
        return ordering;
    }

    public List<OrderListResDto> orderList(){
        List<Ordering> orderings = orderingRepository.findAll();
        List<OrderListResDto> orderListResDtos = new ArrayList<>();
        for(Ordering o : orderings){
            orderListResDtos.add(o.fromEntity());
        }
        return orderListResDtos;
    }

    public List<OrderListResDto> myOrders(){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        List<OrderListResDto> orderListResDtos = new ArrayList<>();
        for(Ordering o : orderingRepository.findByMemberEmail(email)){
            orderListResDtos.add(o.fromEntity());
        }
        return orderListResDtos;
    }

    public Ordering orderCancel(Long id){
        Ordering ordering = orderingRepository.findById(id).orElseThrow(()-> new EntityNotFoundException("주문번호가 없습니다."));
        ordering.cancelStatus();
        return ordering;
    }

    public Ordering orderFeignKafkaCreate(List<OrderCreateDto> dtos){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Ordering ordering = Ordering.builder().memberEmail(email).build();
        for(OrderCreateDto o:dtos){
//            product서버에 feign 클라이언트를 통한 api요청 조회
            ProductDto productDto = productFeign.getProductById(o.getProductId());
            int quantity = o.getProductCount();
            if(productDto.getStockQuantity()<quantity){
                throw new IllegalArgumentException("재고가 부족합니다");
            }else{
//                재고 감소 api요청을 product서버에 보내야함 -> 비동기처리 가능 -> kafka에 메세지 발행
                ProductUpdateStockDto dto = ProductUpdateStockDto.builder()
                        .productId(o.getProductId())
                        .productQuantity(o.getProductCount()).build();
                kafkaTemplate.send("update-stock-topic",dto);
            }

            OrderDetail orderDetail = OrderDetail.builder().ordering(ordering).productId(o.getProductId()).
                    quantity(o.getProductCount()).build();
            ordering.getOrderDetails().add(orderDetail);
        }
        orderingRepository.save(ordering);
        return ordering;
    }
}
