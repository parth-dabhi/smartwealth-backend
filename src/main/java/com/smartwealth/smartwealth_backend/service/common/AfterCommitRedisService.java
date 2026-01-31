package com.smartwealth.smartwealth_backend.service.common;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class AfterCommitRedisService {

    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${idempotency.ttl}")
    private Duration idempotencyTtl;

    public void putAfterCommit(String redisKey, Object value) {
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        try {
                            redisTemplate
                                    .opsForValue()
                                    .set(redisKey, value, idempotencyTtl);
                        } catch (Exception ex) {
                            log.error(
                                    "Redis write failed after commit. key={}",
                                    redisKey,
                                    ex
                            );
                        }
                    }
                }
        );
    }

    public <T> T redisIdempotencyCheck(
            String redisKey,
            String idempotencyKey,
            Class<T> responseType
    ) {
        try {
            Object cached = redisTemplate.opsForValue().get(redisKey);

            if (cached != null) {
                if (!responseType.isInstance(cached)) {
                    throw new IllegalStateException(
                            "Cached response type mismatch. Expected: "
                                    + responseType.getName()
                                    + ", Found: "
                                    + cached.getClass().getName()
                    );
                }
                return responseType.cast(cached);
            }

            return null; // caller continues normal flow

        } catch (Exception ex) {
            log.error(
                    "Redis idempotency check failed. redisKey={}, idempotencyKey={}",
                    redisKey,
                    idempotencyKey,
                    ex
            );
            throw ex;
        }
    }

}

