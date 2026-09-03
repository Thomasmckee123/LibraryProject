package com.example.bookshop.cart;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CartController.class)
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CartService cartService;

    @Test
    void getCartReturnsOkWithBody() throws Exception {
        CartResponse response = new CartResponse(1L,
                List.of(new CartLineResponse("978-0-13-468599-1", "Effective Java", "Joshua Bloch",
                        new BigDecimal("45.00"), 2, new BigDecimal("90.00"))),
                new BigDecimal("90.00"));
        when(cartService.getCart(1L)).thenReturn(response);

        mockMvc.perform(get("/api/carts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.items[0].isbn").value("978-0-13-468599-1"))
                .andExpect(jsonPath("$.items[0].title").value("Effective Java"))
                .andExpect(jsonPath("$.items[0].quantity").value(2))
                .andExpect(jsonPath("$.items[0].lineTotal").value(90.00))
                .andExpect(jsonPath("$.total").value(90.00));
    }

    @Test
    void getCartUnknownIdReturnsNotFound() throws Exception {
        when(cartService.getCart(99L)).thenThrow(com.example.bookshop.config.NotFoundException.cart(99L));

        mockMvc.perform(get("/api/carts/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void addItemReturnsOkWithUpdatedCart() throws Exception {
        CartResponse response = new CartResponse(1L,
                List.of(new CartLineResponse("978-1", "Some Book", "Some Author",
                        new BigDecimal("10.00"), 2, new BigDecimal("20.00"))),
                new BigDecimal("20.00"));
        when(cartService.addItem(eq(1L), any(AddItemRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/carts/1/items")
                        .contentType("application/json")
                        .content("{\"isbn\":\"978-1\",\"quantity\":2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].isbn").value("978-1"))
                .andExpect(jsonPath("$.items[0].quantity").value(2));
    }

    @Test
    void addItemWithNegativeQuantityReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/carts/1/items")
                        .contentType("application/json")
                        .content("{\"isbn\":\"978-1\",\"quantity\":-2}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addItemWithBlankIsbnReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/carts/1/items")
                        .contentType("application/json")
                        .content("{\"isbn\":\"\",\"quantity\":2}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void removeItemReturnsOkWithUpdatedCart() throws Exception {
        CartResponse response = new CartResponse(1L, List.of(), BigDecimal.ZERO);
        when(cartService.removeItem(1L, "978-1")).thenReturn(response);

        mockMvc.perform(delete("/api/carts/1/items/978-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isEmpty());
    }

    @Test
    void removeItemNotInCartReturnsNotFound() throws Exception {
        when(cartService.removeItem(1L, "absent"))
                .thenThrow(com.example.bookshop.config.NotFoundException.book("absent"));

        mockMvc.perform(delete("/api/carts/1/items/absent"))
                .andExpect(status().isNotFound());
    }
}
