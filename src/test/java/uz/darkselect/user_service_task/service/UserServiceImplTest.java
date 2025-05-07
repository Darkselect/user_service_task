package uz.darkselect.user_service_task.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserCacheMapper userCacheMapper;

    @Mock
    private UserCacheService userCacheService;

    @InjectMocks
    private UserServiceImpl service;

    private UUID userId;
    private User user;
    private UserCacheEntity cacheEntity;
    private UserResponseDto responseDto;

    @BeforeEach
    void setup() {
        userId = UUID.randomUUID();
        user = User.builder()
                .id(userId)
                .lastName("Doe")
                .firstName("John")
                .middleName("M")
                .bornDate(LocalDate.of(1990,1,1))
                .phoneNumber("+1234567890")
                .email("john.doe@example.com")
                .createdAt(LocalDateTime.now())
                .build();

        cacheEntity = new UserCacheEntity(userId, "Doe","John","M",
                LocalDate.of(1990,1,1),"+1234567890","john.doe@example.com",
                user.getCreatedAt(), null);

        responseDto = new UserResponseDto();
    }

    @Test
    void createUser_Success() {
        UserCreateRequestDto dto = UserCreateRequestDto.builder()
                .lastName("Doe")
                .firstName("John")
                .middleName("M")
                .bornDate(LocalDate.of(1990,1,1))
                .phoneNumber("+1234567890")
                .email("john.doe@example.com")
                .build();

        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toDto(any(User.class))).thenReturn(responseDto);

        UserResponseDto result = service.createUser(dto);

        assertEquals(responseDto, result);
        verify(userRepository).save(any(User.class));
        verify(userMapper).toDto(any(User.class));
    }


    @Test
    void findUserById_FromCache() {
        when(userCacheService.findUserById(userId)).thenReturn(Optional.of(cacheEntity));
        when(userCacheMapper.toUserResponseDto(cacheEntity)).thenReturn(responseDto);

        UserResponseDto result = service.findUserById(userId);

        assertEquals(responseDto, result);
        verify(userRepository, never()).findById(any());
    }

    @Test
    void findUserById_FromDatabase() {
        when(userCacheService.findUserById(userId)).thenReturn(Optional.empty());
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userCacheMapper.toUserCache(user)).thenReturn(cacheEntity);
        when(userCacheMapper.toUserResponseDto(cacheEntity)).thenReturn(responseDto);

        UserResponseDto result = service.findUserById(userId);

        assertEquals(responseDto, result);
        verify(userCacheService).cacheUser(cacheEntity);
    }

    @Test
    void findUserById_NotFound() {
        when(userCacheService.findUserById(userId)).thenReturn(Optional.empty());
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> service.findUserById(userId));
    }

    @Test
    void updateUserById_Success() {
        UserUpdateRequestDto dto = UserUpdateRequestDto.builder()
                .lastName("Smith")
                .firstName("Jane")
                .middleName("A")
                .bornDate(LocalDate.of(1991,2,2))
                .phoneNumber("+0987654321")
                .email("jane.smith@example.com")
                .build();

        User updated = User.builder()
                .id(userId)
                .lastName("Smith")
                .firstName("Jane")
                .middleName("A")
                .bornDate(LocalDate.of(1991,2,2))
                .phoneNumber("+0987654321")
                .email("jane.smith@example.com")
                .createdAt(user.getCreatedAt())
                .build();

        UserResponseDto updatedDto = new UserResponseDto();

        when(userRepository.updateUserById(eq(userId),
                eq("Smith"), eq("Jane"), eq("A"),
                eq(LocalDate.of(1991,2,2)), eq("+0987654321"),
                eq("jane.smith@example.com")))
            .thenReturn(Optional.of(updated));

        when(userMapper.toDto(updated)).thenReturn(updatedDto);

        UserResponseDto result = service.updateUserById(userId, dto);

        assertEquals(updatedDto, result);
    }

    @Test
    void updateUserById_NotFound() {
        UserUpdateRequestDto dto = UserUpdateRequestDto.builder().build();
        when(userRepository.updateUserById(eq(userId),
                any(), any(), any(), any(), any(), any()))
            .thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
            () -> service.updateUserById(userId, dto));
    }

    @Test
    void deleteUser_Success() {
        User u = new User();
        u.setId(userId);
        service.deleteUser(userId);

        verify(userRepository).deleteById(userId);
        verify(userCacheService).removeUserFromCache(userId);
    }

    @Test
    void deleteUser_NotFound() {
        doThrow(new EmptyResultDataAccessException(1))
            .when(userRepository).deleteById(userId);

        assertThrows(EmptyResultDataAccessException.class,
            () -> service.deleteUser(userId));
    }
}
