package com.beyond.ordersystem.common.service;

import com.beyond.ordersystem.common.config.RabbitmqConfig;
import com.beyond.ordersystem.common.dto.StockRabbitDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StockRabbitmqService {
    // private final RabbitTemplate template;

    // public StockRabbitmqService(RabbitTemplate template) {
    //     this.template = template;
    // }

    // //    mq에 rdb 동기화 관련 메시지를 발행
    // public void publish(StockRabbitDto dto){
    //     template.convertAndSend(RabbitmqConfig.STOCK_DECREASE_QUEUE,dto);
    // }


}
