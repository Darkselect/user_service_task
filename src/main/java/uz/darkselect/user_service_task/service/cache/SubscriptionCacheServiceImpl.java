package uz.darkselect.user_service_task.service.cache;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.darkselect.user_service_task.asspect.annotation.AllAspect;
import uz.darkselect.user_service_task.asspect.annotation.AspectAfterThrowing;
import uz.darkselect.user_service_task.asspect.annotation.LoggingAspectAfterMethod;
import uz.darkselect.user_service_task.asspect.annotation.LoggingAspectBeforeMethod;
import uz.darkselect.user_service_task.entity.SubscriptionCacheEntity;
import uz.darkselect.user_service_task.exception.SubscriptionNotFoundException;
import uz.darkselect.user_service_task.repository.SubscriptionRedisRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SubscriptionCacheServiceImpl implements SubscriptionCacheService {
    private final SubscriptionRedisRepository subscriptionRedisRepository;

    @AllAspect
    public Optional<SubscriptionCacheEntity> findSubscriptionById(UUID id) {
        try {
            return subscriptionRedisRepository.findById(id);
        } catch (SubscriptionNotFoundException e) {
            return Optional.empty();
        }
    }

    @LoggingAspectBeforeMethod
    @LoggingAspectAfterMethod
    @AspectAfterThrowing
    public void cacheSubscription(SubscriptionCacheEntity subscription) {
        if (subscription != null && subscription.getId() != null) {
            try {
                subscriptionRedisRepository.save(subscription);
            } catch (Exception e) {
                throw new RuntimeException(String.format("Failed to cache user: %s", subscription.getId()), e);
            }
        }
    }

    @LoggingAspectBeforeMethod
    @LoggingAspectAfterMethod
    @AspectAfterThrowing
    public void cacheSubscriptions(List<SubscriptionCacheEntity> subscriptions) {
        for (SubscriptionCacheEntity subscriptionFromCache : subscriptions) {
            if (subscriptionFromCache != null && subscriptionFromCache.getId() != null) {
                try {
                    subscriptionRedisRepository.save(subscriptionFromCache);
                } catch (Exception e) {
                    throw new RuntimeException(String.format("Failed to cache subscriptions: %s", subscriptionFromCache.getId()), e);
                }
            }
        }
    }

    @LoggingAspectBeforeMethod
    @LoggingAspectAfterMethod
    @AspectAfterThrowing
    public void removeSubscriptionFromCache(UUID subscriptionId) {
        try {
            subscriptionRedisRepository.deleteById(subscriptionId);
        } catch (Exception e) {
            throw new RuntimeException(String.format("Failed to remove user from cache: %s", subscriptionId), e);
        }
    }
}
