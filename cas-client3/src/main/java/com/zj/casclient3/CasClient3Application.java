package com.zj.casclient3;

import net.unicon.cas.client.configuration.EnableCasClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@EnableCasClient
@SpringBootApplication
public class CasClient3Application {

    public static void main(String[] args) {
        SpringApplication.run(CasClient3Application.class, args);
    }

}
