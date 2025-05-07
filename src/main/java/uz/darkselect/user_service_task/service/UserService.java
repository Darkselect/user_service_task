package uz.darkselect.user_service_task.service;

import uz.darkselect.user_service_task.dto.UserCreateRequestDto;
import uz.darkselect.user_service_task.dto.UserResponseDto;
import uz.darkselect.user_service_task.dto.UserUpdateRequestDto;

import java.util.UUID;

public interface UserService {
    UserResponseDto createUser(UserCreateRequestDto userCreateRequestDto);
    UserResponseDto findUserById(UUID userId);
    UserResponseDto updateUserById(UUID userId, UserUpdateRequestDto userUpdateRequestDto);
    void deleteUser(UUID userId);
}
