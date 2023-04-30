package com.example.fphh.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@Data
@PropertySource("classpath:discord.properties")
@ConfigurationProperties("discord.props")
public class DiscordClientProperties {
  private String token;
  private String generalTextChannelId;
}
