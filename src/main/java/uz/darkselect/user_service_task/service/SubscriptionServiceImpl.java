package uz.darkselect.user_service_task.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.darkselect.user_service_task.asspect.annotation.AllAspect;
import uz.darkselect.user_service_task.dto.PopularSubscriptionProjection;
import uz.darkselect.user_service_task.dto.SubscriptionRequestDto;
import uz.darkselect.user_service_task.dto.SubscriptionResponseDto;
import uz.darkselect.user_service_task.entity.Subscription;
import uz.darkselect.user_service_task.entity.User;
import uz.darkselect.user_service_task.mapper.SubscriptionCacheMapper;
import uz.darkselect.user_service_task.mapper.SubscriptionMapper;
import uz.darkselect.user_service_task.repository.SubscriptionRepository;
import uz.darkselect.user_service_task.service.cache.SubscriptionCacheService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {
    private final SubscriptionRepository subscriptionRepository;
    private final UserOperationService userOperationService;
    private final SubscriptionMapper subscriptionMapper;
    private final SubscriptionCacheMapper subscriptionCacheMapper;
    private final SubscriptionCacheService subscriptionCacheService;

    @AllAspect
    @Transactional
    public SubscriptionResponseDto createSubscription(UUID userId, SubscriptionRequestDto subscriptionRequestDto) {
        User user = userOperationService.getUserFromDbAndCacheById(userId);
        Subscription subscription = Subscription.builder()
                .serviceName(subscriptionRequestDto.getServiceName())
                .user(user)
                .createdAt(LocalDateTime.now())
                .build();

        subscriptionRepository.save(subscription);
        return subscriptionMapper.toDto(subscription);
    }

    @AllAspect
    public SubscriptionResponseDto findUserSubscriptionsByUserId(UUID userId) {
        User user = userOperationService.getUserFromDbAndCacheById(userId);

        return subscriptionCacheService.findSubscriptionById(user.getId())
                .map(subscriptionCacheMapper::toSubscriptionEntity)
                .map(subscriptionMapper::toDto)
                .orElseGet(() -> {
                    Subscription subscription = subscriptionRepository.findSubscriptionByUserId(userId);
                    subscriptionCacheService.cacheSubscription(
                            subscriptionCacheMapper.toSubscriptionCache(subscription)
                    );
                    return subscriptionMapper.toDto(subscription);
                });
    }

    @AllAspect
    @Transactional
    public void deleteSubscription(UUID userId, UUID subscriptionId) {
        User user = userOperationService.getUserFromDbAndCacheById(userId);
        subscriptionRepository.deleteByUserAndSubscriptionId(user.getId(), subscriptionId);
    }

    @AllAspect
    public List<PopularSubscriptionProjection> getTop3() {
        return subscriptionRepository.getTop3(PageRequest.of(0, 3));
    }
}
