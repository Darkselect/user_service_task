package uz.darkselect.user_service_task.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import uz.darkselect.user_service_task.dto.UserCreateRequestDto;
import uz.darkselect.user_service_task.dto.UserResponseDto;
import uz.darkselect.user_service_task.dto.UserUpdateRequestDto;
import uz.darkselect.user_service_task.exception.UserNotFoundException;
import uz.darkselect.user_service_task.service.UserService;

import java.time.LocalDate;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private UserCreateRequestDto createRequestDto;
    private UserUpdateRequestDto updateRequestDto;
    private UserResponseDto responseDto;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        createRequestDto = UserCreateRequestDto.builder()
                .lastName("Doe")
                .firstName("John")
                .middleName("A")
                .bornDate(LocalDate.of(1990, 1, 1))
                .phoneNumber("+1234567890")
                .email("john.doe@example.com")
                .build();

        updateRequestDto = UserUpdateRequestDto.builder()
                .lastName("Smith")
                .firstName("Jane")
                .middleName("B")
                .bornDate(LocalDate.of(1995, 2, 2))
                .phoneNumber("+0987654321")
                .email("jane.smith@example.com")
                .build();

        responseDto = UserResponseDto.builder()
                .id(userId)
                .lastName("Doe")
                .firstName("John")
                .middleName("A")
                .bornDate(LocalDate.of(1990, 1, 1))
                .phoneNumber("+1234567890")
                .email("john.doe@example.com")
                .build();
    }


    @Test
    void createUser_ValidRequest_ShouldReturnCreated() throws Exception {
        when(userService.createUser(any(UserCreateRequestDto.class))).thenReturn(responseDto);

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));

        verify(userService).createUser(any(UserCreateRequestDto.class));
    }

    @Test
    void findUserById_ExistingId_ShouldReturnOk() throws Exception {
        when(userService.findUserById(userId)).thenReturn(responseDto);

        mockMvc.perform(get("/api/v1/users/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.firstName").value("John"));

        verify(userService).findUserById(userId);
    }

    @Test
    void updateUser_ValidRequest_ShouldReturnOk() throws Exception {
        when(userService.updateUserById(eq(userId), any(UserUpdateRequestDto.class))).thenReturn(responseDto);

        mockMvc.perform(put("/api/v1/users/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.firstName").value("John"));

        verify(userService).updateUserById(eq(userId), any(UserUpdateRequestDto.class));
    }

    @Test
    void deleteUser_ExistingId_ShouldReturnOk() throws Exception {
        doNothing().when(userService).deleteUser(userId);

        mockMvc.perform(delete("/api/v1/users/{userId}", userId))
                .andExpect(status().isOk());

        verify(userService).deleteUser(userId);
    }


    @Test
    void createUser_InvalidRequest_ShouldReturnBadRequest() throws Exception {
        UserCreateRequestDto invalidDto = UserCreateRequestDto.builder()
                .lastName("")
                .firstName("John")
                .middleName("A")
                .bornDate(null)
                .phoneNumber("+1234567890")
                .email("invalid-email")
                .build();

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).createUser(any(UserCreateRequestDto.class));
    }

    @Test
    void findUserById_NonExistingId_ShouldReturnNotFound() throws Exception {
        when(userService.findUserById(userId)).thenThrow(new UserNotFoundException("User not found"));

        mockMvc.perform(get("/api/v1/users/{userId}", userId))
                .andExpect(status().isNotFound());

        verify(userService).findUserById(userId);
    }

    @Test
    void updateUser_InvalidRequest_ShouldReturnBadRequest() throws Exception {
        UserUpdateRequestDto invalidDto = UserUpdateRequestDto.builder()
                .lastName("")
                .firstName("Jane")
                .middleName("B")
                .bornDate(null)
                .phoneNumber("+0987654321")
                .email("invalid-email")
                .build();

        mockMvc.perform(put("/api/v1/users/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).updateUserById(eq(userId), any(UserUpdateRequestDto.class));
    }

    @Test
    void updateUser_NonExistingId_ShouldReturnNotFound() throws Exception {
        when(userService.updateUserById(eq(userId), any(UserUpdateRequestDto.class)))
                .thenThrow(new UserNotFoundException("User not found"));

        mockMvc.perform(put("/api/v1/users/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequestDto)))
                .andExpect(status().isNotFound());

        verify(userService).updateUserById(eq(userId), any(UserUpdateRequestDto.class));
    }

    @Test
    void deleteUser_NonExistingId_ShouldReturnNotFound() throws Exception {
        doThrow(new UserNotFoundException("User not found")).when(userService).deleteUser(userId);

        mockMvc.perform(delete("/api/v1/users/{userId}", userId))
                .andExpect(status().isNotFound());

        verify(userService).deleteUser(userId);
    }
}