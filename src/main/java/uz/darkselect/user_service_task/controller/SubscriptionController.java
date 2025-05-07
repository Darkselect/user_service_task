package uz.darkselect.user_service_task.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uz.darkselect.user_service_task.dto.PopularSubscriptionProjection;
import uz.darkselect.user_service_task.dto.SubscriptionRequestDto;
import uz.darkselect.user_service_task.dto.SubscriptionResponseDto;
import uz.darkselect.user_service_task.service.SubscriptionService;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/subscriptions")
@Validated
public class SubscriptionController {
    private final SubscriptionService subscriptionService;

    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/users/{userId}/subscription")
    public SubscriptionResponseDto createSubscription(@PathVariable UUID userId, @RequestBody @Validated SubscriptionRequestDto subscriptionRequestDto) {
        return subscriptionService.createSubscription(userId, subscriptionRequestDto);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/users/{userId}/subscription")
    public SubscriptionResponseDto findUserSubscriptionsByUserId(@PathVariable UUID userId) {
        return subscriptionService.findUserSubscriptionsByUserId(userId);
    }

    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping("/users/{userId}/subscription/{subscriptionId}")
    public String deleteSubscription(@PathVariable UUID userId, @PathVariable UUID subscriptionId) {
        subscriptionService.deleteSubscription(userId, subscriptionId);
        return String.format("Successfully deleted subscription with id: %s, for user with id: %s", subscriptionId, userId);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/top")
    public List<PopularSubscriptionProjection> getTop3() {
        return subscriptionService.getTop3();
    }
}
