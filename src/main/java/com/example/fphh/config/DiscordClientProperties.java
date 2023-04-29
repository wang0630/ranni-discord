package com.example.fphh.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
@ConfigurationProperties("discord.props")
public class DiscordClientProperties {
  private String token;
}
