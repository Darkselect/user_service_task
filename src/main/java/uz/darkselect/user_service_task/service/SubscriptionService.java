package uz.darkselect.user_service_task.service;

import uz.darkselect.user_service_task.dto.PopularSubscriptionProjection;
import uz.darkselect.user_service_task.dto.SubscriptionRequestDto;
import uz.darkselect.user_service_task.dto.SubscriptionResponseDto;

import java.util.List;
import java.util.UUID;

public interface SubscriptionService {
    SubscriptionResponseDto createSubscription(UUID userId, SubscriptionRequestDto subscriptionRequestDto);
    SubscriptionResponseDto findUserSubscriptionsByUserId(UUID userId);
    void deleteSubscription(UUID userId, UUID subscriptionId);
    List<PopularSubscriptionProjection> getTop3();
}
