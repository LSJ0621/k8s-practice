package com.beyond.ordersystem.ordering.service;

import com.beyond.ordersystem.common.service.StockInventoryService;
import com.beyond.ordersystem.member.controller.SseController;
import com.beyond.ordersystem.member.domain.Member;
import com.beyond.ordersystem.member.repository.MemberRepository;
import com.beyond.ordersystem.ordering.domain.OrderDetail;
import com.beyond.ordersystem.ordering.domain.Ordering;
import com.beyond.ordersystem.ordering.dto.OrderCreateDto;
import com.beyond.ordersystem.ordering.dto.OrderListResDto;
import com.beyond.ordersystem.ordering.repository.OrderingDetailRepository;
import com.beyond.ordersystem.ordering.repository.OrderingRepository;
import com.beyond.ordersystem.product.domain.Product;
import com.beyond.ordersystem.product.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class OrderingService {
    private final OrderingRepository orderingRepository;
    private final MemberRepository memberRepository;
    private final OrderingDetailRepository orderingDetailRepository;
    private final ProductRepository productRepository;
    private final StockInventoryService stockInventoryService;
    private final SseController sseController;

    public OrderingService(OrderingRepository orderingRepository, MemberRepository memberRepository, OrderingDetailRepository orderingDetailRepository, ProductRepository productRepository, StockInventoryService stockInventoryService, SseController sseController) {
        this.orderingRepository = orderingRepository;
        this.memberRepository = memberRepository;
        this.orderingDetailRepository = orderingDetailRepository;
        this.productRepository = productRepository;
        this.stockInventoryService = stockInventoryService;
        this.sseController = sseController;
    }

    public Ordering orderCreate(List<OrderCreateDto> dtos){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Member member = memberRepository.findByEmail(email).orElseThrow(()->new EntityNotFoundException("member is not found"));

//        방법2. cascading 사용하여 db저장
//        Ordering 객체 생성하면서 OrderingDetail 객체 같이 생성
        Ordering ordering = Ordering.builder().member(member).build();
        for(OrderCreateDto o:dtos){
//            update쿼리를 사용할때 썼던 product
//            Product product = productRepository.findByIdForUpdate(o.getProductId()).orElseThrow(()-> new EntityNotFoundException("product is not found"));
            Product product = productRepository.findById(o.getProductId()).orElseThrow(()-> new EntityNotFoundException("product is not found"));
            int quantity = o.getProductCount();

//            동시성 이슈 고려 안한 코드
            if(product.getStockQuantity()<o.getProductCount()){
                throw new IllegalArgumentException("재고가 부족합니다");
            }else{
//                재고감소 로직.
                product.updateStockQuantity(o.getProductCount());
            }

            OrderDetail orderDetail = OrderDetail.builder().ordering(ordering).product(product).
                    quantity(o.getProductCount()).build();
            ordering.getOrderDetails().add(orderDetail);
        }
        Ordering ordering1 = orderingRepository.save(ordering);

//        sse를 통한 admin계정에 메시지 발송
        sseController.publishMessage(ordering1.fromEntity(),"admin@naver.com");

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
        Member member = memberRepository.findByEmail(email).orElseThrow(()->new EntityNotFoundException("member is not found"));;
        List<OrderListResDto> orderListResDtos = new ArrayList<>();
        for(Ordering o : member.getOrderingList()){
            orderListResDtos.add(o.fromEntity());
        }
        return orderListResDtos;
    }

    public Ordering orderCancel(Long id){
        Ordering ordering = orderingRepository.findById(id).orElseThrow(()-> new EntityNotFoundException("주문번호가 없습니다."));
        ordering.cancelStatus();
        for(OrderDetail o : ordering.getOrderDetails()){
            int canceledQuantity = o.getQuantity();
            int stockQuantity = o.getProduct().getStockQuantity();
            o.getProduct().canceleOrder(canceledQuantity);
        }
        return ordering;
    }
}
