package uz.darkselect.user_service_task.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import uz.darkselect.user_service_task.entity.Subscription;
import uz.darkselect.user_service_task.entity.SubscriptionCacheEntity;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SubscriptionCacheMapper {
    @Mapping(target = "id", source = "id")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "serviceName", source = "serviceName")
    @Mapping(target = "createdAt", source = "createdAt")
    SubscriptionCacheEntity toSubscriptionCache(Subscription subscription);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "serviceName", source = "serviceName")
    @Mapping(target = "createdAt", source = "createdAt")
    Subscription toSubscriptionEntity(SubscriptionCacheEntity subscriptionCacheEntity);
}
