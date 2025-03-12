package com.beyond.ordersystem.ordering.repository;

import com.beyond.ordersystem.ordering.domain.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderingDetailRepository extends JpaRepository<OrderDetail,Long> {

}
