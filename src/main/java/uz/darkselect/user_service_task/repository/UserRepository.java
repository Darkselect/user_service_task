package uz.darkselect.user_service_task.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.darkselect.user_service_task.entity.User;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    @Query("SELECT u FROM User u WHERE u.id > :lastId ORDER BY u.id ASC")
    List<User> findUsersByBatch(@Param("lastId") UUID lastId, @Param("size") int size);

    @Query(nativeQuery = true, value = """
     UPDATE user_table
     SET last_name = COALESCE(:lastName, last_name),
         first_name = COALESCE(:firstName, first_name),
         middle_name = COALESCE(:middleName, middle_name),
         born_date = COALESCE(:bornDate, born_date), 
         phone_number = COALESCE(:phoneNumber, phone_number),
         email = COALESCE(:email, email),
         updated_at = NOW()
     WHERE id = :id
     RETURNING *
""")
    Optional<User> updateUserById(@Param("id") UUID id, @Param("lastName") String lastName,
                                  @Param("firstName") String firstName, @Param("middleName") String middleName,
                                  @Param("bornDate") LocalDate bornDate, @Param("phoneNumber") String phoneNumber, @Param("email") String email);
}
