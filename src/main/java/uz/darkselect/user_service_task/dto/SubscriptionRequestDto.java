package uz.darkselect.user_service_task.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class SubscriptionRequestDto {
    @NotBlank(message = "Service name is required")
    private String serviceName;
}
