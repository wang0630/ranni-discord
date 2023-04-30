package com.example.fphh.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;

import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.List;

@Configuration
public class MemoryCacheConfig {
  @Bean("dailyCheckCacheManager")
  public CacheManager DailyCheckCacheManager() {
    CaffeineCacheManager manager = new CaffeineCacheManager();
    Caffeine<Object, Object> cacheBuilder = Caffeine.newBuilder()
        .maximumSize(5)
        .expireAfterWrite(Duration.ofMinutes(60));

    manager.setCaffeine(cacheBuilder);
    manager.setCacheNames(List.of("dailyCheckCache"));
    return manager;
  }
}
