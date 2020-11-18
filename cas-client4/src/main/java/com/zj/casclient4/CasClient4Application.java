package com.zj.casclient4;

import net.unicon.cas.client.configuration.EnableCasClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableCasClient
@SpringBootApplication
public class CasClient4Application {

    public static void main(String[] args) {
        SpringApplication.run(CasClient4Application.class, args);
    }

}
