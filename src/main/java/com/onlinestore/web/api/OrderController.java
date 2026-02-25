package com.onlinestore.web.api;

import com.onlinestore.application.order.*;
import com.onlinestore.domain.order.Order;
import com.onlinestore.infrastructure.security.OwnershipValidator;
import com.onlinestore.web.dto.*;
import com.onlinestore.web.dto.mapper.OrderDtoMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "Orders", description = "Order management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class OrderController {

    private final CreateOrderUseCase createOrderUseCase;
    private final PayOrderUseCase payOrderUseCase;
    private final CancelOrderUseCase cancelOrderUseCase;
    private final GetOrderUseCase getOrderUseCase;
    private final OwnershipValidator ownershipValidator;
    private final OrderDtoMapper orderDtoMapper;

    public OrderController(
            CreateOrderUseCase createOrderUseCase,
            PayOrderUseCase payOrderUseCase,
            CancelOrderUseCase cancelOrderUseCase,
            GetOrderUseCase getOrderUseCase,
            OwnershipValidator ownershipValidator,  // ← Added comma
            OrderDtoMapper orderDtoMapper
    ) {
        this.createOrderUseCase = createOrderUseCase;
        this.payOrderUseCase = payOrderUseCase;
        this.cancelOrderUseCase = cancelOrderUseCase;
        this.getOrderUseCase = getOrderUseCase;
        this.ownershipValidator = ownershipValidator;
        this.orderDtoMapper = orderDtoMapper;
    }

    // ==================== CREATE ORDER ====================

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Create new order",
            description = "Create a new order with specified products. Stock will be reserved immediately."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Order created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CreateOrderResponse.class),
                            examples = @ExampleObject(value = """
                    {
                        "orderId": "550e8400-e29b-41d4-a716-446655440000",
                        "message": "Order created successfully"
                    }
                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request - missing required fields"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - JWT token missing or invalid"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Insufficient stock",
                    content = @Content(
                            examples = @ExampleObject(value = """
                    {
                        "status": 409,
                        "error": "Insufficient Stock",
                        "message": "Product 550e8400-e29b-41d4-a716-446655440000: requested 10, available 5",
                        "timestamp": "2026-01-22T12:00:00Z"
                    }
                    """)
                    )
            )
    })
    public ResponseEntity<CreateOrderResponse> createOrder(
            @Valid @RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Order details with product IDs and quantities",
                    required = true,
                    content = @Content(
                            examples = @ExampleObject(value = """
                    {
                        "items": [
                            {
                                "productId": "550e8400-e29b-41d4-a716-446655440000",
                                "quantity": 2
                            },
                            {
                                "productId": "6ba7b810-9dad-11d1-80b4-00c04fd430c8",
                                "quantity": 1
                            }
                        ]
                    }
                    """)
                    )
            )
            CreateOrderRequest request
    ) {
        UUID currentUserId = ownershipValidator.getCurrentUserId();

        var command = new CreateOrderUseCase.CreateOrderCommand(
                currentUserId,
                request.items().stream()
                        .map(item -> new CreateOrderUseCase.CreateOrderCommand.OrderItemDto(
                                item.productId(),
                                item.quantity()
                        ))
                        .toList()
        );

        UUID orderId = createOrderUseCase.execute(command);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(CreateOrderResponse.success(orderId));
    }

    // ==================== GET ORDER BY ID ====================

    @GetMapping("/{orderId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Get order by ID",
            description = "Retrieve order details. Users can only access their own orders, admins can access all orders."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Order found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = OrderResponse.class),
                            examples = @ExampleObject(value = """
                    {
                        "id": "550e8400-e29b-41d4-a716-446655440000",
                        "userId": "123e4567-e89b-12d3-a456-426614174000",
                        "items": [
                            {
                                "productId": "6ba7b810-9dad-11d1-80b4-00c04fd430c8",
                                "productName": "Laptop",
                                "quantity": 1,
                                "pricePerUnit": 999.99,
                                "subtotal": 999.99
                            }
                        ],
                        "status": "PENDING_PAYMENT",
                        "paymentId": null,
                        "totalAmount": 999.99,
                        "createdAt": "2026-01-22T10:00:00Z",
                        "updatedAt": "2026-01-22T10:00:00Z"
                    }
                    """)
                    )
            ),
            @ApiResponse(responseCode = "403", description = "Forbidden - not order owner"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    public ResponseEntity<OrderResponse> getOrder(
            @Parameter(description = "Order UUID", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID orderId
    ) {
        Order order = getOrderUseCase.getById(orderId);

        if (!ownershipValidator.isOwnerOrAdmin(order.getUserId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(orderDtoMapper.toResponse(order)); // ← Use mapper
    }

    // ==================== GET MY ORDERS ====================

    @GetMapping("/my-orders")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Get my orders",
            description = "Retrieve all orders for the authenticated user"
    )
    @ApiResponse(
            responseCode = "200",
            description = "List of user's orders",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = OrderResponse.class)
            )
    )
    public ResponseEntity<List<OrderResponse>> getMyOrders() {
        UUID currentUserId = ownershipValidator.getCurrentUserId();
        List<Order> orders = getOrderUseCase.getByUserId(currentUserId);

        List<OrderResponse> response = orders.stream()
                .map(orderDtoMapper::toResponse) // ← Use mapper
                .toList();

        return ResponseEntity.ok(response);
    }

    // ==================== GET USER ORDERS (ADMIN) ====================

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Get orders by user ID (Admin only)",
            description = "Retrieve all orders for a specific user. Requires ADMIN role."
    )
    @ApiResponse(
            responseCode = "200",
            description = "List of user's orders"
    )
    @ApiResponse(
            responseCode = "403",
            description = "Forbidden - requires ADMIN role"
    )
    public ResponseEntity<List<OrderResponse>> getUserOrders(
            @Parameter(description = "User UUID")
            @PathVariable UUID userId
    ) {
        List<Order> orders = getOrderUseCase.getByUserId(userId);

        List<OrderResponse> response = orders.stream()
                .map(orderDtoMapper::toResponse) // ← Use mapper
                .toList();

        return ResponseEntity.ok(response);
    }

    // ==================== PAY ORDER ====================

    @PostMapping("/{orderId}/pay")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Pay for order",
            description = "Mark order as paid with payment ID. Order status will change to PAID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Payment successful"),
            @ApiResponse(responseCode = "403", description = "Not order owner"),
            @ApiResponse(responseCode = "409", description = "Invalid order state transition")
    })
    public ResponseEntity<Void> payOrder(
            @Parameter(description = "Order UUID")
            @PathVariable UUID orderId,
            @Valid @RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Payment details",
                    content = @Content(
                            examples = @ExampleObject(value = """
                    {
                        "orderId": "550e8400-e29b-41d4-a716-446655440000",
                        "paymentId": "PAYMENT-12345"
                    }
                    """)
                    )
            )
            PayOrderRequest request
    ) {
        Order order = getOrderUseCase.getById(orderId);

        if (!ownershipValidator.isOwner(order.getUserId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        var command = new PayOrderUseCase.PayOrderCommand(
                orderId,
                request.paymentId()
        );

        payOrderUseCase.execute(command);

        return ResponseEntity.ok().build();
    }

    // ==================== CANCEL ORDER ====================

    @PostMapping("/{orderId}/cancel")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Cancel order",
            description = "Cancel order and restore product stock. Only possible for non-completed orders."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order cancelled successfully"),
            @ApiResponse(responseCode = "403", description = "Not order owner or admin"),
            @ApiResponse(responseCode = "409", description = "Cannot cancel completed order")
    })
    public ResponseEntity<Void> cancelOrder(
            @Parameter(description = "Order UUID")
            @PathVariable UUID orderId,
            @Valid @RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Cancellation details",
                    content = @Content(
                            examples = @ExampleObject(value = """
                    {
                        "orderId": "550e8400-e29b-41d4-a716-446655440000",
                        "reason": "Customer changed their mind"
                    }
                    """)
                    )
            )
            CancelOrderRequest request
    ) {
        Order order = getOrderUseCase.getById(orderId);

        if (!ownershipValidator.isOwnerOrAdmin(order.getUserId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        var command = new CancelOrderUseCase.CancelOrderCommand(
                orderId,
                request.reason()
        );

        cancelOrderUseCase.execute(command);

        return ResponseEntity.ok().build();
    }
}