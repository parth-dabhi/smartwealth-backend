package com.smartwealth.smartwealth_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
public class SmartwealthBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartwealthBackendApplication.class, args);
	}
}
