package vn.ancu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Entry point của An Cư Map Backend.
 * Package gốc: vn.ancu — mọi class mới phải nằm trong package này.
 */
@SpringBootApplication
@EnableCaching
@EnableScheduling
public class AnCuApplication {

    public static void main(String[] args) {
        SpringApplication.run(AnCuApplication.class, args);
    }
}

