package uz.darkselect.user_service_task.service.cache;

import uz.darkselect.user_service_task.entity.UserCacheEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserCacheService {
    Optional<UserCacheEntity> findUserById(UUID id);
    void cacheUser(UserCacheEntity user);
    void cacheUsers(List<UserCacheEntity> users);
    void removeUserFromCache(UUID userId);
}
