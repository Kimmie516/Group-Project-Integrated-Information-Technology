package sit.int221.backend.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sit.int221.backend.dtos.order.OrderResponseDto;
import sit.int221.backend.services.SellerOrderService;

@RestController
@RequestMapping("/v2/sellers")
@RequiredArgsConstructor
public class SellerOrderController {
    private final SellerOrderService sellerOrderService;

    @GetMapping("/{sid}/orders")
    public ResponseEntity<Page<OrderResponseDto>> getOrdersBySeller(
            @PathVariable Integer sid,
            Pageable pageable
    ) {
        Page<OrderResponseDto> orders = sellerOrderService.getOrdersBySeller(sid, pageable);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{sid}/orders/{oid}")
    public ResponseEntity<OrderResponseDto> getOrderBySellerAndId(
            @PathVariable Integer sid,
            @PathVariable Integer oid
    ) {
        OrderResponseDto order = sellerOrderService.getOrderBySellerAndId(sid, oid);
        return ResponseEntity.ok(order);
    }
}
