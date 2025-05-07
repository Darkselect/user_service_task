package uz.darkselect.user_service_task.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.darkselect.user_service_task.asspect.annotation.AllAspect;
import uz.darkselect.user_service_task.entity.User;
import uz.darkselect.user_service_task.exception.UserNotFoundException;
import uz.darkselect.user_service_task.mapper.UserCacheMapper;
import uz.darkselect.user_service_task.repository.UserRepository;
import uz.darkselect.user_service_task.service.cache.UserCacheServiceImpl;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserOperationServiceImpl implements UserOperationService {
    private final UserRepository userRepository;
    private final UserCacheServiceImpl userCacheService;
    private final UserCacheMapper userCacheMapper;

    @Transactional
    @AllAspect
    public User getUserFromDbAndCacheById(UUID userId) {
        return userCacheService.findUserById(userId)
                .map(userCacheMapper::toUserEntity)
                .orElseGet(() -> {
                    try {
                        User user = userRepository.findById(userId)
                                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
                        userCacheService.cacheUser(userCacheMapper.toUserCache(user));
                        return user;
                    } catch (UserNotFoundException e) {
                        throw e;
                    } catch (Exception e) {
                        throw new RuntimeException("Internal server error", e);
                    }
                });
    }
}
