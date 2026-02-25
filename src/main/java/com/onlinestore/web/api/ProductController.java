package com.onlinestore.web.api;

import com.onlinestore.domain.product.Product;
import com.onlinestore.domain.product.ProductRepository;
import com.onlinestore.web.dto.ProductResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/products")
@Tag(name = "Products", description = "Product catalog endpoints")
public class ProductController {

    private final ProductRepository productRepository;

    public ProductController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @GetMapping("/{productId}")
    @Operation(
            summary = "Get product by ID",
            description = "Retrieve product details including current stock availability"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Product found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProductResponse.class),
                            examples = @ExampleObject(value = """
                    {
                        "id": "550e8400-e29b-41d4-a716-446655440000",
                        "name": "Laptop",
                        "price": 999.99,
                        "stockQuantity": 50
                    }
                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Product not found",
                    content = @Content(
                            examples = @ExampleObject(value = """
                    {
                        "status": 400,
                        "error": "Bad Request",
                        "message": "Product not found",
                        "timestamp": "2026-01-22T12:00:00Z"
                    }
                    """)
                    )
            )
    })
    public ResponseEntity<ProductResponse> getProduct(
            @Parameter(
                    description = "Product UUID",
                    example = "550e8400-e29b-41d4-a716-446655440000"
            )
            @PathVariable UUID productId
    ) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        return ResponseEntity.ok(ProductResponse.from(product));
    }
}