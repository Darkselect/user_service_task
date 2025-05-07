package uz.darkselect.user_service_task.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class UserUpdateRequestDto {
    @NotBlank(message = "Last name is required")
    private String lastName;
    @NotBlank(message = "First name is required")
    private String firstName;
    @NotBlank(message = "Middle name is required")
    private String middleName;
    @NotNull(message = "Born date is required")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    private LocalDate bornDate;
    @NotBlank(message = "Phone number is required")
    private String phoneNumber;
    @NotNull(message = "Email is required")
    private String email;
}
