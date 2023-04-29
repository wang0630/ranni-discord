package com.example.fphh;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@Slf4j
@SpringBootApplication
public class FphhApplication {

  public static void main(String[] args) {
    new SpringApplicationBuilder(FphhApplication.class)
        .build()
        .run(args);
  }

}
