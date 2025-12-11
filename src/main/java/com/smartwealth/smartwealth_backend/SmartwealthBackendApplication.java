package com.smartwealth.smartwealth_backend;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class SmartwealthBackendApplication {

    private static final Logger log = LoggerFactory.getLogger(SmartwealthBackendApplication.class);

    public static void main(String[] args) {
        log.info("Shri Harivansh, Radhe Radhe");

        SpringApplication.run(SmartwealthBackendApplication.class, args);
	}
}
