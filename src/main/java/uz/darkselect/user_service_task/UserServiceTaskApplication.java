package uz.darkselect.user_service_task;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy
public class UserServiceTaskApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserServiceTaskApplication.class, args);
    }

}
