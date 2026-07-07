package com.chargesafe.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * 缓存配置（Caffeine 本地缓存）
 *
 * <p>用于统计类高频只读接口的缓存，降低数据库压力。缓存会在数据写操作后由
 * {@code @CacheEvict} 主动失效，默认 TTL 5 分钟兜底。
 */
@Configuration
@EnableCaching
public class CacheConfig {

    public static final String CACHE_STATISTICS = "statistics";

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(CACHE_STATISTICS);
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .initialCapacity(50)
                .maximumSize(200)
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .recordStats());
        return cacheManager;
    }
}
