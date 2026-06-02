package com.scalelink.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis Configuration
 *
 * WHAT IS RedisTemplate?
 * RedisTemplate is Spring's abstraction for interacting with Redis.
 * It handles connection management, serialization (converting Java objects
 * to bytes and back), and provides a clean API for Redis operations.
 *
 * WHY CUSTOM CONFIGURATION?
 * The default RedisTemplate uses Java serialization, which stores data
 * in a binary format that's not human-readable. We configure it to use:
 * - StringRedisSerializer for keys → keys are readable strings in Redis
 * - JSON serializer for values → values are readable JSON in Redis
 *
 * This means when you look at Redis data (using redis-cli or a GUI),
 * you'll see readable data instead of garbled binary.
 */
@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Keys stored as plain strings: "url:aB3x7Kp"
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        // Values stored as JSON: {"originalUrl": "https://..."}
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());

        template.afterPropertiesSet();
        return template;
    }
}
