package kz.bars.order_service.presentation.controllers;

import kz.bars.order_service.OrderServiceApplication;
import kz.bars.order_service.builder.OrderTestBuilder;
import kz.bars.order_service.builder.ProductTestBuilder;
import kz.bars.order_service.domain.models.Order;
import kz.bars.order_service.domain.models.Product;
import kz.bars.order_service.domain.repositories.OrderRepository;
import kz.bars.order_service.domain.repositories.UserRepository;
import kz.bars.order_service.infrastructure.config.RedisConfigTest;
import kz.bars.order_service.infrastructure.config.SecurityConfigTest;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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
@Log4j2
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

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
    @WithMockUser(username = "testuser")
    void testCreateOrder() throws Exception {
        // Arrange
        String requestContent = """
            {
                "products": [
                    {"name": "Product A", "price": 100, "quantity": 2}
                ]
            }
        """;

        // Проверяем, какие пользователи есть в БД перед тестом
        userRepository.findAll().forEach(user -> log.info("User in DB: {} with roles {}", user.getUsername(), user.getRoles()));

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
     * Тест проверяет обновление заказа через PUT-запрос.
     * Убедитесь, что заказ обновляется корректно с указанными данными.
     */
    @Test
    @WithMockUser(username = "testuser")
    void testUpdateOrder() throws Exception {
        // Arrange
        // Создаем продукт для первоначального заказа
        Product initialProduct = ProductTestBuilder.builder()
                .name("Product A")
                .price(BigDecimal.valueOf(100.0))
                .quantity(2)
                .build()
                .toProduct();

        // Создаем первоначальный заказ
        Order initialOrder = OrderTestBuilder.builder()
                .customerName("testuser")
                .products(List.of(initialProduct))
                .build()
                .toOrder();

        // Устанавливаем связь продукта с заказом и сохраняем в репозиторий
        initialProduct.setOrder(initialOrder);
        orderRepository.save(initialOrder);

        // Проверяем, какие пользователи есть в БД перед тестом
        userRepository.findAll().forEach(user -> log.info("User in DB: {} with roles {}", user.getUsername(), user.getRoles()));

        // Вывод сохраненного заказа для проверки теста (необязательно)
        log.info("Saved order: ID={}, Status={}", initialOrder.getOrderId(), initialOrder.getStatus());

        // Данные для обновления заказа
        String updateRequestContent = """
        {
            "products": [
                {"name": "Product B", "price": 150.0, "quantity": 1}
            ]
        }
    """;

        // Act & Assert
        mockMvc.perform(put("/orders/" + initialOrder.getOrderId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateRequestContent))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(initialOrder.getOrderId().toString())) // Проверяем, что ID заказа не изменился
                .andExpect(jsonPath("$.products[0].name").value("Product B")) // Проверяем обновленные данные продукта
                .andExpect(jsonPath("$.products[0].price").value(150.0))
                .andExpect(jsonPath("$.products[0].quantity").value(1))
                .andExpect(jsonPath("$.totalPrice").value(150.0)); // Проверяем общую стоимость заказа
    }

    /**
     * Тест проверяет удаление заказа через DELETE-запрос.
     * Убедитесь, что заказ помечается как удалённый.
     */
    @Test
    @WithMockUser(username = "testuser")
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

        // Устанавливаем связь продукта с заказом и сохраняем в репозиторий
        product.setOrder(order);
        orderRepository.save(order);

        // Проверяем, какие пользователи есть в БД перед тестом
        userRepository.findAll().forEach(user -> log.info("User in DB: {} with roles {}", user.getUsername(), user.getRoles()));

        // Вывод сохраненного заказа для проверки теста (необязательно)
        log.info("Saved order: ID={}, Status={}", order.getOrderId(), order.getStatus());

        // Act & Assert
        mockMvc.perform(delete("/orders/" + order.getOrderId()))
                .andExpect(status().isNoContent());

        Order foundOrder = orderRepository.findById(order.getOrderId())
                .orElseThrow(() -> new AssertionError("Order not found in database"));

        assertTrue(foundOrder.isDeleted()); // Проверка флага isDeleted
    }

    /**
     * Тест проверяет получение заказа по его ID через GET-запрос.
     * Убедитесь, что возвращается корректный заказ.
     */
    @Test
    @WithMockUser(username = "testuser")
    void testGetOrderById() throws Exception {
        // Arrange
        Product product = ProductTestBuilder.builder()
                .name("Product A")
                .price(BigDecimal.valueOf(120.5))
                .quantity(2)
                .build()
                .toProduct();

        Order order = OrderTestBuilder.builder()
                .customerName("testuser")
                .products(List.of(product))
                .build()
                .toOrder();

        // Устанавливаем связь продукта с заказом и сохраняем в репозиторий
        product.setOrder(order);
        orderRepository.save(order);

        // Проверяем, какие пользователи есть в БД перед тестом
        userRepository.findAll().forEach(user -> log.info("User in DB: {} with roles {}", user.getUsername(), user.getRoles()));

        // Вывод сохраненного заказа для проверки теста (необязательно)
        log.info("Saved order: ID={}, Status={}", order.getOrderId(), order.getStatus());

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
     * Тест проверяет получение заказов с фильтрацией через GET-запрос.
     * Убедитесь, что возвращаются только те заказы, которые соответствуют фильтрам.
     */
    @ParameterizedTest
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @CsvSource({
            "CONFIRMED, 100.0, 200.0, 1", // Проверка одного заказа
            "PENDING, 50.0, 150.0, 1",    // Проверка другого заказа
            "CANCELLED, 50.0, 100.0, 0",  // Проверка, когда не должно быть результатов
            "null, null, 150.0, 2",       // Проверка всех заказов без фильтрации по статусу и нижней границы
            "null, 150.0, null, 2",       // Проверка всех заказов без фильтрации по статусу и верхней границы
            "null, null, null, 3"         // Проверка всех заказов без фильтрации
    })
    void testGetOrdersFiltered(String status, String minPrice, String maxPrice, int expectedCount) throws Exception {
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

        Product product3 = ProductTestBuilder.builder()
                .name("Product C")
                .price(BigDecimal.valueOf(150.0))
                .quantity(1)
                .build()
                .toProduct();

        Order order1 = OrderTestBuilder.builder()
                .customerName("user1")
                .status(Order.Status.CONFIRMED)
                .products(List.of(product1))
                .build()
                .toOrder();

        Order order2 = OrderTestBuilder.builder()
                .customerName("user2")
                .status(Order.Status.PENDING)
                .products(List.of(product2))
                .build()
                .toOrder();

        Order order3 = OrderTestBuilder.builder()
                .customerName("user3")
                .status(Order.Status.CANCELLED)
                .products(List.of(product3))
                .build()
                .toOrder();

        // Устанавливаем связь продуктов с заказами
        product1.setOrder(order1);
        product2.setOrder(order2);
        product3.setOrder(order3);

        // Вычисляем общую стоимость для каждого заказа
        order1.calculateTotalPrice();
        order2.calculateTotalPrice();
        order3.calculateTotalPrice();

        // Сохраняем заказы в репозиторий
        orderRepository.saveAll(List.of(order1, order2, order3));

        // Проверяем, какие пользователи есть в БД перед тестом
        userRepository.findAll().forEach(user -> log.info("User in DB: {} with roles {}", user.getUsername(), user.getRoles()));

        // Вывод всех сохраненных заказов для проверки теста (необязательно)
        orderRepository.findAll().forEach(order -> log.info("Saved order: ID={}, TotalPrice={}, Status={}",
                order.getOrderId(), order.getTotalPrice(), order.getStatus()));

        // Act & Assert
        mockMvc.perform(get("/orders")
                        .param("status", status.equals("null") ? "" : status) // Если статус null, не передаем параметр
                        .param("min_price", minPrice.equals("null") ? "" : minPrice) // Если статус null, не передаем параметр
                        .param("max_price", maxPrice.equals("null") ? "" : maxPrice)) // Если статус null, не передаем параметр
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(expectedCount)); // Проверяем количество заказов
    }
}
