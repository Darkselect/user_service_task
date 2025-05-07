package uz.darkselect.user_service_task.repository;

import org.springframework.data.repository.CrudRepository;
import uz.darkselect.user_service_task.entity.SubscriptionCacheEntity;

import java.util.UUID;

public interface SubscriptionRedisRepository extends CrudRepository<SubscriptionCacheEntity, UUID> {
}
