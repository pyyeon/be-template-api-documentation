package com.springboot.slice.controller.order;

import com.springboot.exception.BusinessLogicException;
import com.springboot.helper.StubData;
import com.springboot.order.entity.Order;
import com.springboot.order.repository.OrderRepository;
import com.springboot.order.service.OrderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class OrderServiceHomeworkTest {
    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    @Test
    public void cancelOrderTest() {
        // given
        long orderId = 1L;
        Order order = StubData.MockOrder.getResponseBody(orderId);

        // Stubbing by Mockito
        given(orderRepository.findById(orderId)).willReturn(Optional.of(order));

        // when
        Executable executable = () -> orderService.cancelOrder(orderId);

        // then
        assertThrows(BusinessLogicException.class, executable);
    }
}
