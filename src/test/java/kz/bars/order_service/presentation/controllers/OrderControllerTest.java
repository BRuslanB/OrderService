package kz.bars.order_service.presentation.controllers;

import kz.bars.order_service.OrderServiceApplication;
import kz.bars.order_service.builder.OrderTestBuilder;
import kz.bars.order_service.builder.ProductTestBuilder;
import kz.bars.order_service.domain.models.Order;
import kz.bars.order_service.domain.models.Product;
import kz.bars.order_service.domain.repositories.OrderRepository;
import kz.bars.order_service.infrastructure.config.RedisConfigTest;
import kz.bars.order_service.infrastructure.config.SecurityConfigTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Тесты для проверки работы контроллера OrderController.
 */
@ActiveProfiles("test") // Используем тестовый профиль
@SpringBootTest(classes = {OrderServiceApplication.class, SecurityConfigTest.class, RedisConfigTest.class}) // Загружаем контекст Spring
@AutoConfigureMockMvc(addFilters = false) // Отключаем фильтры безопасности для тестов
@SuppressWarnings("unused") // Подавляет предупреждения о неиспользуемых методах
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrderRepository orderRepository;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll(); // Очищаем базу данных перед каждым тестом
    }

    /**
     * Тест проверяет создание заказа через POST-запрос.
     * Убедитесь, что заказ создаётся корректно с указанными данными.
     */
    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testCreateOrder() throws Exception {
        // Arrange
        String requestContent = """
            {
                "products": [
                    {"name": "Product A", "price": 100, "quantity": 2}
                ]
            }
        """;

        // Act & Assert
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestContent))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.products[0].name").value("Product A"))
                .andExpect(jsonPath("$.products[0].price").value(100))
                .andExpect(jsonPath("$.products[0].quantity").value(2))
                .andExpect(jsonPath("$.totalPrice").value(200));
    }

    /**
     * Тест проверяет получение заказа по его ID через GET-запрос.
     * Убедитесь, что возвращается корректный заказ.
     */
    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testGetOrderById() throws Exception {
        // Arrange
        Product product = ProductTestBuilder.builder()
                .name("Product A")
                .price(BigDecimal.valueOf(100.0))
                .quantity(2)
                .build()
                .toProduct();

        Order order = OrderTestBuilder.builder()
                .customerName("testuser")
                .products(List.of(product))
                .build()
                .toOrder();

        product.setOrder(order); // Устанавливаем связь продукта с заказом
        orderRepository.save(order); // Сохраняем заказ в репозиторий

        // Act & Assert
        mockMvc.perform(get("/orders/" + order.getOrderId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(order.getOrderId().toString()))
                .andExpect(jsonPath("$.customerName").value(order.getCustomerName()))
                .andExpect(jsonPath("$.products[0].name").value(product.getName()))
                .andExpect(jsonPath("$.products[0].price").value(product.getPrice()))
                .andExpect(jsonPath("$.products[0].quantity").value(product.getQuantity()));
    }

    /**
     * Тест проверяет получение всех заказов через GET-запрос.
     * Убедитесь, что возвращается список всех заказов.
     */
    @Test
    @WithMockUser(username = "adminuser", roles = {"ADMIN"})
    void testGetAllOrders() throws Exception {
        // Arrange
        Product product1 = ProductTestBuilder.builder()
                .name("Product A")
                .price(BigDecimal.valueOf(100.0))
                .quantity(2)
                .build()
                .toProduct();

        Product product2 = ProductTestBuilder.builder()
                .name("Product B")
                .price(BigDecimal.valueOf(50.0))
                .quantity(1)
                .build()
                .toProduct();

        Order order1 = OrderTestBuilder.builder()
                .customerName("user1")
                .products(List.of(product1))
                .build()
                .toOrder();

        Order order2 = OrderTestBuilder.builder()
                .customerName("user2")
                .products(List.of(product2))
                .build()
                .toOrder();

        product1.setOrder(order1);
        product2.setOrder(order2);

        orderRepository.saveAll(List.of(order1, order2)); // Сохраняем заказы в репозиторий

        // Act & Assert
        mockMvc.perform(get("/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2)) // Проверяем, что вернулось 2 заказа
                .andExpect(jsonPath("$[0].orderId").value(order1.getOrderId().toString()))
                .andExpect(jsonPath("$[0].customerName").value(order1.getCustomerName()))
                .andExpect(jsonPath("$[0].products[0].name").value(order1.getProducts().get(0).getName()))
                .andExpect(jsonPath("$[0].products[0].price").value(order1.getProducts().get(0).getPrice()))
                .andExpect(jsonPath("$[0].products[0].quantity").value(order1.getProducts().get(0).getQuantity()))
                .andExpect(jsonPath("$[1].orderId").value(order2.getOrderId().toString()))
                .andExpect(jsonPath("$[1].customerName").value(order2.getCustomerName()))
                .andExpect(jsonPath("$[1].products[0].name").value(order2.getProducts().get(0).getName()))
                .andExpect(jsonPath("$[1].products[0].price").value(order2.getProducts().get(0).getPrice()))
                .andExpect(jsonPath("$[1].products[0].quantity").value(order2.getProducts().get(0).getQuantity()));    }

    /**
     * Тест проверяет удаление заказа через DELETE-запрос.
     * Убедитесь, что заказ помечается как удалённый.
     */
    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testDeleteOrder() throws Exception {
        // Arrange
        Product product = ProductTestBuilder.builder()
                .name("Product A")
                .price(BigDecimal.valueOf(100.0))
                .quantity(2)
                .build()
                .toProduct();

        Order order = OrderTestBuilder.builder()
                .customerName("testuser")
                .products(List.of(product))
                .build()
                .toOrder();

        product.setOrder(order);
        orderRepository.save(order);

        // Act & Assert
        mockMvc.perform(delete("/orders/" + order.getOrderId()))
                .andExpect(status().isOk());

        assertTrue(orderRepository.findById(order.getOrderId()).get().isDeleted()); // Проверка флага isDeleted
    }
}
