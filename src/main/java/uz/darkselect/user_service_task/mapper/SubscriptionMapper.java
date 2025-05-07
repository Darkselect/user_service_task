package uz.darkselect.user_service_task.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import uz.darkselect.user_service_task.dto.SubscriptionResponseDto;
import uz.darkselect.user_service_task.entity.Subscription;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SubscriptionMapper {
    @Mapping(target = "id", source = "id")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "serviceName", source = "serviceName")
    @Mapping(target = "createdAt", source = "createdAt")
    SubscriptionResponseDto toDto(Subscription subscription);
}
