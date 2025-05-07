package uz.darkselect.user_service_task.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.darkselect.user_service_task.dto.PopularSubscriptionProjection;
import uz.darkselect.user_service_task.entity.Subscription;

import java.util.List;
import java.util.UUID;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {
    @Query("SELECT s FROM Subscription s WHERE s.user.id = :userId")
    Subscription findSubscriptionByUserId(UUID userId);

    @Modifying
    @Query("DELETE FROM Subscription s WHERE s.user.id = :userId AND s.id = :subscriptionId")
    void deleteByUserAndSubscriptionId(
            @Param("userId") UUID userId,
            @Param("subscriptionId") UUID subscriptionId);

    @Query("""
                SELECT s.serviceName   AS serviceName,
                       COUNT(s)        AS count
                  FROM Subscription s
              GROUP BY s.serviceName
              ORDER BY COUNT(s) DESC
            """)
    List<PopularSubscriptionProjection> getTop3(Pageable pageable);

    @Query("SELECT s FROM Subscription s WHERE s.id > :lastId ORDER BY s.id ASC")
    List<Subscription> findUsersByBatch(@Param("lastId") UUID lastId, @Param("size") int size);
}
