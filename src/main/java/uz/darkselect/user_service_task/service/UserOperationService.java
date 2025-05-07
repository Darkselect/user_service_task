package uz.darkselect.user_service_task.service;

import uz.darkselect.user_service_task.entity.User;

import java.util.UUID;

public interface UserOperationService {
    User getUserFromDbAndCacheById(UUID userId);
}
