package uz.darkselect.user_service_task.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import uz.darkselect.user_service_task.entity.Subscription;
import uz.darkselect.user_service_task.entity.SubscriptionCacheEntity;
import uz.darkselect.user_service_task.mapper.SubscriptionCacheMapper;
import uz.darkselect.user_service_task.repository.SubscriptionRepository;
import uz.darkselect.user_service_task.service.cache.SubscriptionCacheServiceImpl;

import java.util.List;
import java.util.UUID;

@Component
@Slf4j
public class SubscriptionCache extends AbstractCacheWarmup<Subscription, SubscriptionCacheEntity> {
    private final static String SUBSCRIPTION_CACHE = "subscription cache";

    private final SubscriptionCacheServiceImpl subscriptionCacheServiceImpl;
    private final SubscriptionRepository subscriptionRedisRepository;
    private final SubscriptionCacheMapper subscriptionCacheMapper;

    protected SubscriptionCache(ThreadPoolTaskExecutor taskExecutor, SubscriptionCacheServiceImpl subscriptionCacheServiceImpl,
                                SubscriptionRepository subscriptionRedisRepository, SubscriptionCacheMapper subscriptionCacheMapper) {
        super(taskExecutor);
        this.subscriptionCacheServiceImpl = subscriptionCacheServiceImpl;
        this.subscriptionRedisRepository = subscriptionRedisRepository;
        this.subscriptionCacheMapper = subscriptionCacheMapper;
    }


    @Override
    protected List<Subscription> fetchBatch(UUID lastId, int batchSize) {
        return subscriptionRedisRepository.findUsersByBatch(lastId, batchSize);
    }

    @Override
    protected UUID getLastId(List<Subscription> entities) {
        return entities.get(entities.size() - 1).getId();
    }

    @Override
    protected SubscriptionCacheEntity mapToCacheEntity(Subscription entity) {
        return subscriptionCacheMapper.toSubscriptionCache(entity);
    }

    @Override
    protected void saveCache(List<SubscriptionCacheEntity> cacheEntities) {
       subscriptionCacheServiceImpl.cacheSubscriptions(cacheEntities);
    }

    @Override
    protected String getCacheName() {
        return SUBSCRIPTION_CACHE;
    }
}
