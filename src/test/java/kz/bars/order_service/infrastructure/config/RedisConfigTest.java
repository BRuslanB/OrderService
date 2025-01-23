package kz.bars.order_service.infrastructure.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.support.NoOpCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("test") // Конфигурация активируется только в тестовом профиле
@SuppressWarnings("unused") // Подавляет предупреждения о неиспользуемых методах
public class RedisConfigTest {

    /**
     * Конфигурация CacheManager для тестов.
     *
     * Этот бин отключает использование кэша в тестах, заменяя его на NoOpCacheManager.
     * Это упрощает тестирование, так как данные всегда извлекаются из базы данных
     * или других источников, а не из кэша.
     *
     * @return NoOpCacheManager, который не кэширует данные.
     */
    @Bean
    public CacheManager cacheManager() {
        return new NoOpCacheManager(); // Отключение кэша для тестов
    }
}
