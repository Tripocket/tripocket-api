package pl.tripocket.tripocket_api.common.config;

import java.time.Duration;
import java.util.Map;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;
import pl.tripocket.tripocket_api.common.nbpclient.client.CachedNbpClient;
import pl.tripocket.tripocket_api.common.nbpclient.dto.NbpTable;

@Configuration
@EnableCaching
public class CacheConfig {

  @Bean
  RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
    RedisCacheConfiguration defaults =
        RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofHours(1))
            .disableCachingNullValues();

    RedisCacheConfiguration nbpConfig =
        defaults
            .entryTtl(Duration.ofHours(6))
            .serializeValuesWith(SerializationPair.fromSerializer(
                new JacksonJsonRedisSerializer<>(NbpTable.class)));

    return RedisCacheManager.builder(connectionFactory)
        .cacheDefaults(defaults)
        .withInitialCacheConfigurations(Map.of(
            CachedNbpClient.TABLE_A_CACHE, nbpConfig,
            CachedNbpClient.TABLE_B_CACHE, nbpConfig))
        .build();
  }
}
