package uz.darkselect.user_service_task.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uz.darkselect.user_service_task.entity.User;
import uz.darkselect.user_service_task.entity.UserCacheEntity;
import uz.darkselect.user_service_task.exception.UserNotFoundException;
import uz.darkselect.user_service_task.mapper.UserCacheMapper;
import uz.darkselect.user_service_task.repository.UserRepository;
import uz.darkselect.user_service_task.service.cache.UserCacheServiceImpl;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserOperationServiceImplTest {

    @Mock
    private UserRepository userRepository;
    
    @Mock
    private UserCacheServiceImpl userCacheService;
    
    @Mock
    private UserCacheMapper userCacheMapper;
    
    @InjectMocks
    private UserOperationServiceImpl userOperationService;

    @Test
    void getUserFromDbAndCacheById_FromCache() {
        UUID userId = UUID.randomUUID();
        User cachedUser = new User();
        UserCacheEntity cachedUserEntity = new UserCacheEntity();

        when(userCacheService.findUserById(userId)).thenReturn(Optional.of(cachedUserEntity));
        when(userCacheMapper.toUserEntity(any())).thenReturn(cachedUser);

        User result = userOperationService.getUserFromDbAndCacheById(userId);

        assertEquals(cachedUser, result);
        verify(userRepository, never()).findById(any());
    }

    @Test
    void getUserFromDbAndCacheById_FromDatabase() {
        UUID userId = UUID.randomUUID();
        User dbUser = new User();
        UserCacheEntity cachedUserEntity = new UserCacheEntity();

        when(userCacheService.findUserById(userId)).thenReturn(Optional.empty());
        when(userRepository.findById(userId)).thenReturn(Optional.of(dbUser));
        when(userCacheMapper.toUserCache(dbUser)).thenReturn(cachedUserEntity);

        User result = userOperationService.getUserFromDbAndCacheById(userId);

        assertEquals(dbUser, result);
        verify(userCacheService).cacheUser(any());
    }

    @Test
    void getUserFromDbAndCacheById_UserNotFound() {
        UUID userId = UUID.randomUUID();
        when(userCacheService.findUserById(userId)).thenReturn(Optional.empty());
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> 
            userOperationService.getUserFromDbAndCacheById(userId)
        );
    }

    @Test
    void getUserFromDbAndCacheById_CacheError() {
        UUID userId = UUID.randomUUID();
        when(userCacheService.findUserById(userId)).thenThrow(new RuntimeException("Cache error"));

        assertThrows(RuntimeException.class, () ->
            userOperationService.getUserFromDbAndCacheById(userId)
        );
    }

    @Test
    void getUserFromDbAndCacheById_DatabaseError() {
        UUID userId = UUID.randomUUID();
        when(userCacheService.findUserById(userId)).thenReturn(Optional.empty());
        when(userRepository.findById(userId)).thenThrow(new RuntimeException("DB error"));

        assertThrows(RuntimeException.class, () ->
            userOperationService.getUserFromDbAndCacheById(userId)
        );
    }
}