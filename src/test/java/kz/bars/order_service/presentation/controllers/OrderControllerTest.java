package kz.bars.order_service.presentation.controllers;

import kz.bars.order_service.application.dto.OrderRequest;
import kz.bars.order_service.application.dto.ProductRequest;
import kz.bars.order_service.builder.OrderRequestTestBuilder;
import kz.bars.order_service.builder.OrderTestBuilder;
import kz.bars.order_service.builder.ProductTestBuilder;
import kz.bars.order_service.domain.models.Order;
import kz.bars.order_service.domain.models.Product;
import kz.bars.order_service.domain.repositories.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Тесты для проверки работы контроллера OrderController.
 */
@ActiveProfiles("test") // Используем тестовый профиль
@SpringBootTest // Загружаем полный контекст Spring
@AutoConfigureMockMvc // Автоматическая настройка MockMvc
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc; // Автоматически настраиваемый MockMvc

    @Autowired
    private OrderRepository orderRepository; // Реальная реализация OrderRepository

    /**
     * Очищает базу данных перед каждым тестом.
     */
    @BeforeEach
    void setUp() {
        orderRepository.deleteAll(); // Очищаем базу данных
    }

    /**
     * Тест проверяет создание заказа через POST-запрос.
     * Убедитесь, что заказ создаётся корректно, включая имя клиента, список продуктов и общую стоимость.
     */
    @Test
    void testCreateOrder() throws Exception {
        // Arrange
        OrderRequest request = OrderRequestTestBuilder.builder()
                .customerName("John Doe")
                .products(List.of(
                        new ProductRequest("Product A", BigDecimal.valueOf(100), 2)
                ))
                .build()
                .toOrderRequest();

        // Act & Assert
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                        "customerName": "John Doe",
                        "products": [
                            {"name": "Product A", "price": 100, "quantity": 2}
                        ]
                    }
                    """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.customerName").value(request.getCustomerName()))
                .andExpect(jsonPath("$.products[0].name").value(request.getProducts().get(0).getName()))
                .andExpect(jsonPath("$.products[0].price").value(request.getProducts().get(0).getPrice()))
                .andExpect(jsonPath("$.products[0].quantity").value(request.getProducts().get(0).getQuantity()))
                .andExpect(jsonPath("$.totalPrice").value(200));
    }

    /**
     * Тест проверяет получение заказа по его ID через GET-запрос.
     * Убедитесь, что запрос возвращает правильный заказ с соответствующими данными.
     */
    @Test
    void testGetOrderById() throws Exception {
        // Arrange
        Product product = ProductTestBuilder.builder()
                .name("Product A")
                .price(BigDecimal.valueOf(100))
                .quantity(2)
                .build()
                .toProduct();

        Order order = OrderTestBuilder.builder()
                .customerName("John Doe")
                .products(List.of(product))
                .build()
                .toOrder();

        // Связываем продукты с заказом
        product.setOrder(order);

        Order savedOrder = orderRepository.save(order);

        // Act & Assert
        mockMvc.perform(get("/orders/" + savedOrder.getOrderId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(savedOrder.getOrderId()))
                .andExpect(jsonPath("$.customerName").value("John Doe"))
                .andExpect(jsonPath("$.products[0].name").value("Product A"))
                .andExpect(jsonPath("$.products[0].price").value(100))
                .andExpect(jsonPath("$.products[0].quantity").value(2));
    }

    /**
     * Тест проверяет обновление заказа через PUT-запрос.
     * Убедитесь, что заказ обновляется корректно и возвращает ожидаемые данные.
     */
    @Test
    void testUpdateOrder() throws Exception {
        // Arrange
        Product product = ProductTestBuilder.builder()
                .name("Product A")
                .price(BigDecimal.valueOf(100))
                .quantity(2)
                .build()
                .toProduct();

        Order order = OrderTestBuilder.builder()
                .customerName("John Doe")
                .products(List.of(product))
                .build()
                .toOrder();

        // Связываем продукты с заказом
        product.setOrder(order);

        Order savedOrder = orderRepository.save(order);

        // Новый запрос для обновления
        OrderRequest updatedRequest = OrderRequestTestBuilder.builder()
                .customerName("John Doe Updated")
                .products(List.of(
                        new ProductRequest("Product B", BigDecimal.valueOf(50), 1)
                ))
                .build()
                .toOrderRequest();

        // Act & Assert
        mockMvc.perform(put("/orders/" + savedOrder.getOrderId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                        "customerName": "John Doe Updated",
                        "products": [
                            {"name": "Product B", "price": 50, "quantity": 1}
                        ]
                    }
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerName").value(updatedRequest.getCustomerName()))
                .andExpect(jsonPath("$.products[0].name").value(updatedRequest.getProducts().get(0).getName()))
                .andExpect(jsonPath("$.products[0].price").value(updatedRequest.getProducts().get(0).getPrice()))
                .andExpect(jsonPath("$.products[0].quantity").value(updatedRequest.getProducts().get(0).getQuantity()));
    }

    /**
     * Тест проверяет удаление заказа через DELETE-запрос.
     * Убедитесь, что запрос возвращает статус 204 (No Content).
     */
    @Test
    void testDeleteOrder() throws Exception {
        // Arrange
        Product product = ProductTestBuilder.builder()
                .name("Product A")
                .price(BigDecimal.valueOf(100))
                .quantity(2)
                .build()
                .toProduct();

        Order order = OrderTestBuilder.builder()
                .customerName("John Doe")
                .products(List.of(product))
                .build()
                .toOrder();

        Order savedOrder = orderRepository.save(order);

        // Act & Assert
        mockMvc.perform(delete("/orders/" + savedOrder.getOrderId()))
                .andExpect(status().isNoContent());
    }
}
