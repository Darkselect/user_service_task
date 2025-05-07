package uz.darkselect.user_service_task.service.cache;

import uz.darkselect.user_service_task.entity.SubscriptionCacheEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SubscriptionCacheService {
    Optional<SubscriptionCacheEntity> findSubscriptionById(UUID id);
    void cacheSubscription(SubscriptionCacheEntity subscription);
    void cacheSubscriptions(List<SubscriptionCacheEntity> subscriptions);
    void removeSubscriptionFromCache(UUID subscriptionId);
}
