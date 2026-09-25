package rw.afriteck.pms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
public class PropertyManagementSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(PropertyManagementSystemApplication.class, args);
    }

}
