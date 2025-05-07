package uz.darkselect.user_service_task.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uz.darkselect.user_service_task.dto.UserCreateRequestDto;
import uz.darkselect.user_service_task.dto.UserResponseDto;
import uz.darkselect.user_service_task.dto.UserUpdateRequestDto;
import uz.darkselect.user_service_task.service.UserService;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/users")
@Validated
public class UserController {
    private final UserService userService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public UserResponseDto createUser(@RequestBody @Valid UserCreateRequestDto userCreateRequestDto) {
        return userService.createUser(userCreateRequestDto);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{userId}")
    public UserResponseDto findUserById(@PathVariable UUID userId) {
        return userService.findUserById(userId);
    }


    @ResponseStatus(HttpStatus.OK)
    @PutMapping("/{userId}")
    public UserResponseDto updateUser(@PathVariable UUID userId, @RequestBody @Valid UserUpdateRequestDto userUpdateRequestDto) {
        return userService.updateUserById(userId, userUpdateRequestDto);
    }

    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping("/{userId}")
    public void deleteUser(@PathVariable UUID userId) {
        userService.deleteUser(userId);
    }
}
