package uz.darkselect.user_service_task.repository;

import org.springframework.data.repository.CrudRepository;
import uz.darkselect.user_service_task.entity.UserCacheEntity;

import java.util.UUID;

public interface UserRedisRepository extends CrudRepository<UserCacheEntity, UUID> {
}
