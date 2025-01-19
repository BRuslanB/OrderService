package kz.bars.order_service.presentation.controllers;

import kz.bars.order_service.OrderServiceApplication;
import kz.bars.order_service.application.dto.OrderRequest;
import kz.bars.order_service.application.dto.ProductRequest;
import kz.bars.order_service.builder.OrderRequestTestBuilder;
import kz.bars.order_service.builder.OrderTestBuilder;
import kz.bars.order_service.builder.ProductTestBuilder;
import kz.bars.order_service.domain.models.Order;
import kz.bars.order_service.domain.models.Product;
import kz.bars.order_service.domain.repositories.OrderRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Тесты для проверки работы контроллера OrderController.
 */
@ActiveProfiles("test") // Используем тестовый профиль
@SpringBootTest(classes = {OrderServiceApplication.class, SecurityConfigTest.class}) // Загружаем полный контекст Spring
@AutoConfigureMockMvc(addFilters = false) // Отключение фильтров безопасности
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc; // MockMvc для тестирования REST

    @Autowired
    private OrderRepository orderRepository; // Реальный репозиторий

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll(); // Очищаем базу данных перед каждым тестом
    }

    /**
     * Тест проверяет создание заказа через POST-запрос.
     * Убедитесь, что заказ создаётся корректно, включая имя клиента, список продуктов и общую стоимость.
     */
    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testCreateOrder() throws Exception {
        // Arrange
        OrderRequest request = OrderRequestTestBuilder.builder()
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
                        "products": [
                            {"name": "Product A", "price": 100, "quantity": 2}
                        ]
                    }
                    """))
                .andDo(print()) // Выводит все детали запроса и ответа
                .andExpect(status().isCreated())
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
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testGetOrderById() throws Exception {
        // Arrange
        Product product = ProductTestBuilder.builder()
                .name("Product A")
                .price(BigDecimal.valueOf(100))
                .quantity(2)
                .build()
                .toProduct();

        Order order = OrderTestBuilder.builder()
                .customerName("testuser")
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
                .andExpect(jsonPath("$.customerName").value("testuser"))
                .andExpect(jsonPath("$.products[0].name").value("Product A"))
                .andExpect(jsonPath("$.products[0].price").value(100))
                .andExpect(jsonPath("$.products[0].quantity").value(2));
    }

    /**
     * Тест проверяет получение всех заказов через GET-запрос.
     * Тест работает для роли ADMIN .
     */
    @Test
    @WithMockUser(username = "adminuser", roles = {"ADMIN"})
    void testGetAllOrders() throws Exception {
        // Arrange: Создаем несколько заказов
        Product product1 = ProductTestBuilder.builder()
                .name("Product A")
                .price(BigDecimal.valueOf(100))
                .quantity(2)
                .build()
                .toProduct();

        Product product2 = ProductTestBuilder.builder()
                .name("Product B")
                .price(BigDecimal.valueOf(50))
                .quantity(1)
                .build()
                .toProduct();

        Order order1 = OrderTestBuilder.builder()
                .customerName("testuser1")
                .products(List.of(product1))
                .build()
                .toOrder();

        Order order2 = OrderTestBuilder.builder()
                .customerName("testuser2")
                .products(List.of(product2))
                .build()
                .toOrder();

        // Связываем продукты с заказами
        product1.setOrder(order1);
        product2.setOrder(order2);

        // Сохраняем заказы в базе данных
        orderRepository.save(order1);
        orderRepository.save(order2);

        // Act & Assert
        mockMvc.perform(get("/orders")) // Запрашиваем все заказы
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2)) // Проверяем, что вернулось 2 заказа
                .andExpect(jsonPath("$[0].customerName").value("testuser1"))
                .andExpect(jsonPath("$[0].products[0].name").value("Product A"))
                .andExpect(jsonPath("$[0].products[0].price").value(100))
                .andExpect(jsonPath("$[0].products[0].quantity").value(2))
                .andExpect(jsonPath("$[1].customerName").value("testuser2"))
                .andExpect(jsonPath("$[1].products[0].name").value("Product B"))
                .andExpect(jsonPath("$[1].products[0].price").value(50))
                .andExpect(jsonPath("$[1].products[0].quantity").value(1));
    }

    /**
     * Тест проверяет обновление заказа через PUT-запрос.
     * Убедитесь, что заказ обновляется корректно и возвращает ожидаемые данные.
     */
    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testUpdateOrder() throws Exception {
        // Arrange
        Product product = ProductTestBuilder.builder()
                .name("Product A")
                .price(BigDecimal.valueOf(100))
                .quantity(2)
                .build()
                .toProduct();

        Order order = OrderTestBuilder.builder()
                .customerName("testuser")
                .products(List.of(product))
                .build()
                .toOrder();

        // Связываем продукты с заказом
        product.setOrder(order);

        Order savedOrder = orderRepository.save(order);

        // Новый запрос для обновления
        OrderRequest updatedRequest = OrderRequestTestBuilder.builder()
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
                        "products": [
                            {"name": "Product B", "price": 50, "quantity": 1}
                        ]
                    }
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products[0].name").value(updatedRequest.getProducts().get(0).getName()))
                .andExpect(jsonPath("$.products[0].price").value(updatedRequest.getProducts().get(0).getPrice()))
                .andExpect(jsonPath("$.products[0].quantity").value(updatedRequest.getProducts().get(0).getQuantity()));
    }

    /**
     * Тест проверяет удаление заказа через DELETE-запрос.
     * Убедитесь, что запрос возвращает статус 204 (No Content).
     */
    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testDeleteOrder() throws Exception {
        // Arrange
        Product product = ProductTestBuilder.builder()
                .name("Product A")
                .price(BigDecimal.valueOf(100))
                .quantity(2)
                .build()
                .toProduct();

        Order order = OrderTestBuilder.builder()
                .customerName("testuser")
                .products(List.of(product))
                .build()
                .toOrder();

        Order savedOrder = orderRepository.save(order);

        // Act & Assert
        mockMvc.perform(delete("/orders/" + savedOrder.getOrderId()))
                .andExpect(status().isNoContent());
    }
}
