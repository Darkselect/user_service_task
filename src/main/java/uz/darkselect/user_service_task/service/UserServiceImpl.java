package uz.darkselect.user_service_task.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.darkselect.user_service_task.asspect.annotation.AllAspect;
import uz.darkselect.user_service_task.asspect.annotation.LoggingAspectAfterMethod;
import uz.darkselect.user_service_task.asspect.annotation.LoggingAspectBeforeMethod;
import uz.darkselect.user_service_task.dto.UserCreateRequestDto;
import uz.darkselect.user_service_task.dto.UserResponseDto;
import uz.darkselect.user_service_task.dto.UserUpdateRequestDto;
import uz.darkselect.user_service_task.entity.User;
import uz.darkselect.user_service_task.entity.UserCacheEntity;
import uz.darkselect.user_service_task.exception.UserNotFoundException;
import uz.darkselect.user_service_task.mapper.UserCacheMapper;
import uz.darkselect.user_service_task.mapper.UserMapper;
import uz.darkselect.user_service_task.repository.UserRepository;
import uz.darkselect.user_service_task.service.cache.UserCacheService;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final UserCacheMapper userCacheMapper;
    private final UserCacheService userCacheService;

    @Transactional
    @AllAspect
    public UserResponseDto createUser(UserCreateRequestDto userCreateRequestDto) {
       User user = User.builder()
               .lastName(userCreateRequestDto.getLastName())
               .firstName(userCreateRequestDto.getFirstName())
               .middleName(userCreateRequestDto.getMiddleName())
               .bornDate(userCreateRequestDto.getBornDate())
               .phoneNumber(userCreateRequestDto.getPhoneNumber())
               .email(userCreateRequestDto.getEmail())
               .createdAt(LocalDateTime.now())
               .build();

       userRepository.save(user);
       return userMapper.toDto(user);
    }

    @Override
    @AllAspect
    public UserResponseDto findUserById(UUID userId) {
        return userCacheService.findUserById(userId)
                .map(userCacheMapper::toUserResponseDto)
                .orElseGet(() -> {
                    try {
                        User user = userRepository.findById(userId)
                                .orElseThrow(() -> new UserNotFoundException(
                                        String.format("User not found with id: %s", userId)
                                ));
                        UserCacheEntity userCacheEntity = userCacheMapper.toUserCache(user);
                        userCacheService.cacheUser(userCacheEntity);
                        return userCacheMapper.toUserResponseDto(userCacheEntity);
                    } catch (UserNotFoundException e) {
                        throw new RuntimeException("Internal server error", e);
                    }
                });
    }

    @Override
    @AllAspect
    public UserResponseDto updateUserById(UUID userId, UserUpdateRequestDto userUpdateRequestDto) {
        User user = userRepository.updateUserById(userId,
                userUpdateRequestDto.getLastName(),
                userUpdateRequestDto.getFirstName(),
                userUpdateRequestDto.getMiddleName(),
                userUpdateRequestDto.getBornDate(),
                userUpdateRequestDto.getPhoneNumber(),
                userUpdateRequestDto.getEmail()).orElseThrow(
                () -> new UserNotFoundException(String.format("User not found with id: %s", userId)));

        return userMapper.toDto(user);
    }

    @Override
    @LoggingAspectBeforeMethod
    @LoggingAspectAfterMethod
    public void deleteUser(UUID userId) {
       userRepository.deleteById(userId);
       userCacheService.removeUserFromCache(userId);
    }
}
