package sit.int221.backend.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sit.int221.backend.dtos.order.OrderRequestDto;
import sit.int221.backend.dtos.order.OrderResponseDto;
import sit.int221.backend.services.OrderService;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/v2")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping("/orders")
    public ResponseEntity<OrderResponseDto> createOrder(
            @Valid @RequestBody OrderRequestDto dto
    ) {
        OrderResponseDto created = orderService.createOrder(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/users/{id}/orders")
    public ResponseEntity<List<OrderResponseDto>> getOrdersByBuyer(@PathVariable Integer id) {
        List<OrderResponseDto> orders = orderService.getOrdersByBuyer(id);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/orders/{orderId}")
    public ResponseEntity<OrderResponseDto> getOrderById(@PathVariable Integer orderId) {
        OrderResponseDto dto = orderService.getOrderById(orderId);
        return ResponseEntity.ok(dto);
    }

    @PatchMapping("/orders/{orderId}/cancel")
    public ResponseEntity<OrderResponseDto> cancelOrder(@PathVariable Integer orderId) {
        OrderResponseDto canceled = orderService.cancelOrder(orderId);
        return ResponseEntity.ok(canceled);
    }
}
