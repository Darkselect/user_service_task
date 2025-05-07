package uz.darkselect.user_service_task.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import uz.darkselect.user_service_task.dto.PopularSubscriptionProjection;
import uz.darkselect.user_service_task.dto.SubscriptionRequestDto;
import uz.darkselect.user_service_task.dto.SubscriptionResponseDto;
import uz.darkselect.user_service_task.exception.SubscriptionNotFoundException;
import uz.darkselect.user_service_task.service.SubscriptionService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SubscriptionController.class)
public class SubscriptionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SubscriptionService subscriptionService;

    @Autowired
    private ObjectMapper objectMapper;

    private SubscriptionRequestDto requestDto;
    private SubscriptionResponseDto responseDto;
    private PopularSubscriptionProjection projection;
    private UUID userId;
    private UUID subscriptionId;
    private UUID planId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        subscriptionId = UUID.randomUUID();
        planId = UUID.randomUUID();

        requestDto = SubscriptionRequestDto.builder()
                .serviceName("Premium Plan")
                .build();

        responseDto = SubscriptionResponseDto.builder()
                .id(subscriptionId)
                .userId(userId)
                .serviceName("Premium Plan")
                .createdAt(LocalDateTime.of(2025, 1, 1, 12, 0))
                .build();

        projection = new PopularSubscriptionProjection() {
            @Override
            public String getServiceName() {
                return "Test name";
            }
        };
    }


    @Test
    void createSubscription_ValidRequest_ShouldReturnOk() throws Exception {
        when(subscriptionService.createSubscription(eq(userId), any(SubscriptionRequestDto.class)))
                .thenReturn(responseDto);

        mockMvc.perform(post("/api/v1/subscriptions/users/{userId}/subscription", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(subscriptionId.toString()))
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.serviceName").value("Premium Plan"))
                .andExpect(jsonPath("$.createdAt").value("2025-01-01T12:00:00"));

        verify(subscriptionService).createSubscription(eq(userId), any(SubscriptionRequestDto.class));
    }

    @Test
    void findUserSubscriptionsByUserId_ExistingUser_ShouldReturnOk() throws Exception {
        when(subscriptionService.findUserSubscriptionsByUserId(userId)).thenReturn(responseDto);

        mockMvc.perform(get("/api/v1/subscriptions/users/{userId}/subscription", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(subscriptionId.toString()))
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.serviceName").value("Premium Plan"))
                .andExpect(jsonPath("$.createdAt").value("2025-01-01T12:00:00"));

        verify(subscriptionService).findUserSubscriptionsByUserId(userId);
    }

    @Test
    void deleteSubscription_ExistingSubscription_ShouldReturnOk() throws Exception {
        doNothing().when(subscriptionService).deleteSubscription(userId, subscriptionId);

        mockMvc.perform(delete("/api/v1/subscriptions/users/{userId}/subscription/{subscriptionId}", userId, subscriptionId))
                .andExpect(status().isOk())
                .andExpect(content().string(String.format("Successfully deleted subscription with id: %s, for user with id: %s", subscriptionId, userId)));

        verify(subscriptionService).deleteSubscription(userId, subscriptionId);
    }

    @Test
    void getTop3_ShouldReturnOk() throws Exception {
        when(subscriptionService.getTop3()).thenReturn(List.of(projection));

        mockMvc.perform(get("/api/v1/subscriptions/top"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].serviceName").value("Test name"));

        verify(subscriptionService).getTop3();
    }


    @Test
    void createSubscription_InvalidRequest_ShouldReturnBadRequest() throws Exception {
        SubscriptionRequestDto invalidDto = SubscriptionRequestDto.builder()
                .serviceName("")
                .build();

        mockMvc.perform(post("/api/v1/subscriptions/users/{userId}/subscription", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

        verify(subscriptionService, never()).createSubscription(eq(userId), any(SubscriptionRequestDto.class));
    }

    @Test
    void createSubscription_NonExistingUser_ShouldReturnNotFound() throws Exception {
        when(subscriptionService.createSubscription(eq(userId), any(SubscriptionRequestDto.class)))
                .thenThrow(new SubscriptionNotFoundException("User not found"));

        mockMvc.perform(post("/api/v1/subscriptions/users/{userId}/subscription", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isNotFound());

        verify(subscriptionService).createSubscription(eq(userId), any(SubscriptionRequestDto.class));
    }

    @Test
    void findUserSubscriptionsByUserId_NonExistingUser_ShouldReturnNotFound() throws Exception {
        when(subscriptionService.findUserSubscriptionsByUserId(userId))
                .thenThrow(new SubscriptionNotFoundException("Subscription not found"));

        mockMvc.perform(get("/api/v1/subscriptions/users/{userId}/subscription", userId))
                .andExpect(status().isNotFound());

        verify(subscriptionService).findUserSubscriptionsByUserId(userId);
    }

    @Test
    void deleteSubscription_NonExistingSubscription_ShouldReturnNotFound() throws Exception {
        doThrow(new SubscriptionNotFoundException("Subscription not found"))
                .when(subscriptionService).deleteSubscription(userId, subscriptionId);

        mockMvc.perform(delete("/api/v1/subscriptions/users/{userId}/subscription/{subscriptionId}", userId, subscriptionId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Subscription not found"));

        verify(subscriptionService).deleteSubscription(userId, subscriptionId);
    }

    @Test
    void getTop3_EmptyList_ShouldReturnOkWithEmptyList() throws Exception {
        when(subscriptionService.getTop3()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/subscriptions/top"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(subscriptionService).getTop3();
    }
}