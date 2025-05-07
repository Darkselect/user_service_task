package uz.darkselect.user_service_task.cache;


import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import uz.darkselect.user_service_task.entity.User;
import uz.darkselect.user_service_task.entity.UserCacheEntity;
import uz.darkselect.user_service_task.mapper.UserCacheMapper;
import uz.darkselect.user_service_task.repository.UserRepository;
import uz.darkselect.user_service_task.service.cache.UserCacheServiceImpl;

import java.util.List;
import java.util.UUID;

@Component
@Slf4j
public class UserCache extends AbstractCacheWarmup<User, UserCacheEntity> {
    private static final String USER_CACHE = "user cache";

    private final UserCacheServiceImpl userCacheServiceImpl;
    private final UserRepository userRepository;
    private final UserCacheMapper userCacheMapper;

    public UserCache(ThreadPoolTaskExecutor taskExecutor, UserCacheServiceImpl userCacheServiceImpl,
                     UserRepository userRepository, UserCacheMapper userCacheMapper) {
        super(taskExecutor);
        this.userCacheServiceImpl = userCacheServiceImpl;
        this.userRepository = userRepository;
        this.userCacheMapper = userCacheMapper;
    }

    @Override
    protected List<User> fetchBatch(UUID lastId, int batchSize) {
        return userRepository.findUsersByBatch(lastId, batchSize);
    }

    @Override
    protected UUID getLastId(List<User> entities) {
        return entities.get(entities.size() - 1).getId();
    }

    @Override
    protected UserCacheEntity mapToCacheEntity(User entity) {
        return userCacheMapper.toUserCache(entity);
    }

    @Override
    protected void saveCache(List<UserCacheEntity> cacheEntities) {
        userCacheServiceImpl.cacheUsers(cacheEntities);
    }

    @Override
    protected String getCacheName() {
        return USER_CACHE;
    }
}