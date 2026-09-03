package com.example.bookshop.order;

import com.example.bookshop.book.InsufficientStockException;
import com.example.bookshop.config.NotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * HTTP-level tests for {@link OrderController}: status codes and JSON shape
 * only. {@link CheckoutService} is mocked, so these do not re-verify the
 * checkout business rules - {@link CheckoutServiceTest} owns that.
 */
@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CheckoutService checkoutService;

    @Test
    void checkoutReturns201WithOrderBody() throws Exception {
        OrderResponse response = new OrderResponse(
                1L, "BND-ABCDEFGH", 7L, OrderStatus.PAID,
                Instant.parse("2026-09-02T10:00:00Z"),
                new BigDecimal("19.98"),
                List.of(new OrderLineResponse("111", "Dune", new BigDecimal("9.99"), 2, new BigDecimal("19.98"))));
        when(checkoutService.checkout(5L)).thenReturn(response);

        mockMvc.perform(post("/api/carts/5/checkout"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.reference").value("BND-ABCDEFGH"))
                .andExpect(jsonPath("$.status").value("PAID"))
                .andExpect(jsonPath("$.total").value(19.98))
                .andExpect(jsonPath("$.lines[0].isbn").value("111"))
                .andExpect(jsonPath("$.lines[0].quantity").value(2))
                .andExpect(jsonPath("$.lines[0].lineTotal").value(19.98));
    }

    @Test
    void checkoutOnUnknownCartReturns404() throws Exception {
        when(checkoutService.checkout(404L)).thenThrow(NotFoundException.cart(404L));

        mockMvc.perform(post("/api/carts/404/checkout"))
                .andExpect(status().isNotFound());
    }

    @Test
    void checkoutOnEmptyCartReturns400() throws Exception {
        when(checkoutService.checkout(6L)).thenThrow(new IllegalArgumentException("cart 6 has no items to check out"));

        mockMvc.perform(post("/api/carts/6/checkout"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void checkoutWithInsufficientStockReturns409WithStockDetail() throws Exception {
        when(checkoutService.checkout(7L)).thenThrow(new InsufficientStockException("111", 5, 2));

        mockMvc.perform(post("/api/carts/7/checkout"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.isbn").value("111"))
                .andExpect(jsonPath("$.requested").value(5))
                .andExpect(jsonPath("$.available").value(2));
    }

    @Test
    void getOrderReturns200WithOrderBody() throws Exception {
        OrderResponse response = new OrderResponse(
                2L, "BND-11112222", 7L, OrderStatus.PAID,
                Instant.parse("2026-09-02T10:00:00Z"),
                new BigDecimal("6.50"),
                List.of(new OrderLineResponse("222", "Emma", new BigDecimal("6.50"), 1, new BigDecimal("6.50"))));
        when(checkoutService.findOrder(2L)).thenReturn(response);

        mockMvc.perform(get("/api/orders/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.reference").value("BND-11112222"))
                .andExpect(jsonPath("$.customerId").value(7));
    }

    @Test
    void getOrderOnUnknownIdReturns404() throws Exception {
        when(checkoutService.findOrder(999L)).thenThrow(NotFoundException.order(999L));

        mockMvc.perform(get("/api/orders/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void listOrdersReturnsCustomersOrdersNewestFirst() throws Exception {
        OrderResponse newer = new OrderResponse(
                9L, "BND-NEWER000", 7L, OrderStatus.PAID,
                Instant.parse("2026-09-02T10:00:00Z"),
                new BigDecimal("6.50"),
                List.of(new OrderLineResponse("222", "Emma", new BigDecimal("6.50"), 1, new BigDecimal("6.50"))));
        OrderResponse older = new OrderResponse(
                4L, "BND-OLDER000", 7L, OrderStatus.PAID,
                Instant.parse("2026-08-01T10:00:00Z"),
                new BigDecimal("19.98"),
                List.of(new OrderLineResponse("111", "Dune", new BigDecimal("9.99"), 2, new BigDecimal("19.98"))));
        when(checkoutService.findOrdersForCustomer(7L)).thenReturn(List.of(newer, older));

        mockMvc.perform(get("/api/orders?customerId=7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].reference").value("BND-NEWER000"))
                .andExpect(jsonPath("$[1].reference").value("BND-OLDER000"))
                .andExpect(jsonPath("$[0].lines[0].titleAtPurchase").value("Emma"));
    }

    @Test
    void listOrdersReturnsEmptyArrayForACustomerWhoHasNeverOrdered() throws Exception {
        when(checkoutService.findOrdersForCustomer(8L)).thenReturn(List.of());

        mockMvc.perform(get("/api/orders?customerId=8"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void listOrdersWithoutACustomerIdReturns400() throws Exception {
        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isBadRequest());
    }
}
