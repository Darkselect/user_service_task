package uz.darkselect.user_service_task.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@RedisHash(value = "subscription_from_cache", timeToLive = 86400)
public class SubscriptionCacheEntity implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private UUID id;
    private UUID userId;
    private String serviceName;
    private LocalDateTime createdAt;
}
