package uz.darkselect.user_service_task.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import uz.darkselect.user_service_task.dto.PopularSubscriptionProjection;
import uz.darkselect.user_service_task.dto.SubscriptionRequestDto;
import uz.darkselect.user_service_task.dto.SubscriptionResponseDto;
import uz.darkselect.user_service_task.entity.Subscription;
import uz.darkselect.user_service_task.entity.SubscriptionCacheEntity;
import uz.darkselect.user_service_task.entity.User;
import uz.darkselect.user_service_task.mapper.SubscriptionCacheMapper;
import uz.darkselect.user_service_task.mapper.SubscriptionMapper;
import uz.darkselect.user_service_task.repository.SubscriptionRepository;
import uz.darkselect.user_service_task.service.cache.SubscriptionCacheService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceImplTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private UserOperationService userOperationService;

    @Mock
    private SubscriptionMapper subscriptionMapper;

    @Mock
    private SubscriptionCacheMapper subscriptionCacheMapper;

    @Mock
    private SubscriptionCacheService subscriptionCacheService;

    @InjectMocks
    private SubscriptionServiceImpl subscriptionService;

    @Test
    void createSubscription_Success() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        SubscriptionRequestDto request = new SubscriptionRequestDto("Netflix");
        SubscriptionResponseDto expected = new SubscriptionResponseDto();

        when(userOperationService.getUserFromDbAndCacheById(userId)).thenReturn(user);
        when(subscriptionMapper.toDto(any())).thenReturn(expected);

        SubscriptionResponseDto result = subscriptionService.createSubscription(userId, request);

        assertNotNull(result);
        verify(subscriptionRepository).save(any());
    }

    @Test
    void createSubscription_UserNotFound() {
        UUID userId = UUID.randomUUID();
        when(userOperationService.getUserFromDbAndCacheById(userId)).thenThrow(new RuntimeException());

        assertThrows(RuntimeException.class, () -> 
            subscriptionService.createSubscription(userId, new SubscriptionRequestDto("Netflix"))
        );
    }

    @Test
    void findUserSubscriptions_FromCache() {
        UUID userId = UUID.randomUUID();
        SubscriptionCacheEntity cacheEntity = new SubscriptionCacheEntity();
        Subscription subscription = new Subscription();
        SubscriptionResponseDto expected = new SubscriptionResponseDto();

        User user = new User();
        user.setId(userId);
        when(userOperationService.getUserFromDbAndCacheById(userId)).thenReturn(user);

        when(subscriptionCacheService.findSubscriptionById(userId))
                .thenReturn(Optional.of(cacheEntity));

        when(subscriptionCacheMapper.toSubscriptionEntity(cacheEntity))
                .thenReturn(subscription);
        when(subscriptionMapper.toDto(subscription))
                .thenReturn(expected);

        SubscriptionResponseDto result = subscriptionService.findUserSubscriptionsByUserId(userId);

        assertEquals(expected, result);
        verify(subscriptionRepository, never()).findSubscriptionByUserId(any());
    }

    @Test
    void findUserSubscriptions_FromDatabase() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        Subscription subscription = new Subscription();
        SubscriptionCacheEntity cacheEntity = new SubscriptionCacheEntity();
        SubscriptionResponseDto expected = new SubscriptionResponseDto();

        when(userOperationService.getUserFromDbAndCacheById(userId))
                .thenReturn(user);

        when(subscriptionCacheService.findSubscriptionById(userId))
                .thenReturn(Optional.empty());
        when(subscriptionRepository.findSubscriptionByUserId(userId))
                .thenReturn(subscription);
        when(subscriptionCacheMapper.toSubscriptionCache(subscription))
                .thenReturn(cacheEntity);

        when(subscriptionMapper.toDto(subscription))
                .thenReturn(expected);

        SubscriptionResponseDto result =
                subscriptionService.findUserSubscriptionsByUserId(userId);

        assertEquals(expected, result);
        verify(subscriptionCacheService).cacheSubscription(cacheEntity);
    }


    @Test
    void findUserSubscriptions_NotFound() {
        UUID userId = UUID.randomUUID();
        when(userOperationService.getUserFromDbAndCacheById(userId)).thenReturn(new User());
        when(subscriptionCacheService.findSubscriptionById(userId)).thenReturn(Optional.empty());
        when(subscriptionRepository.findSubscriptionByUserId(userId)).thenReturn(null);

        assertThrows(RuntimeException.class, () ->
            subscriptionService.findUserSubscriptionsByUserId(userId)
        );
    }

    @Test
    void deleteSubscription_Success() {
        UUID userId = UUID.randomUUID();
        UUID subscriptionId = UUID.randomUUID();

        User user = new User();
        user.setId(userId);
        when(userOperationService.getUserFromDbAndCacheById(userId))
                .thenReturn(user);

        subscriptionService.deleteSubscription(userId, subscriptionId);

        verify(subscriptionRepository)
                .deleteByUserAndSubscriptionId(userId, subscriptionId);
    }


    @Test
    void deleteSubscription_UserNotFound() {
        UUID userId = UUID.randomUUID();
        when(userOperationService.getUserFromDbAndCacheById(userId)).thenThrow(new RuntimeException());

        assertThrows(RuntimeException.class, () ->
            subscriptionService.deleteSubscription(userId, UUID.randomUUID())
        );
    }

    @Test
    void getTop3_Success() {
        List<PopularSubscriptionProjection> expected = List.of(
            new PopularSubscriptionProjectionImpl("Spotify"),
            new PopularSubscriptionProjectionImpl("Netflix")
        );

        when(subscriptionRepository.getTop3(PageRequest.of(0, 3))).thenReturn(expected);

        List<PopularSubscriptionProjection> result = subscriptionService.getTop3();

        assertEquals(2, result.size());
        assertEquals("Spotify", result.get(0).getServiceName());
    }

    @Test
    void getTop3_EmptyResult() {
        when(subscriptionRepository.getTop3(PageRequest.of(0, 3))).thenReturn(List.of());

        List<PopularSubscriptionProjection> result = subscriptionService.getTop3();

        assertTrue(result.isEmpty());
    }

    private static class PopularSubscriptionProjectionImpl implements PopularSubscriptionProjection {
        private final String serviceName;

        public PopularSubscriptionProjectionImpl(String serviceName) {
            this.serviceName = serviceName;
        }

        @Override public String getServiceName() { return serviceName; }
    }
}