package com.example.fphh.config;

import com.example.fphh.service.DiscordService;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.Event;
import discord4j.gateway.intent.Intent;
import discord4j.gateway.intent.IntentSet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DiscordClientConfig {
    private final DiscordClientProperties discordClientProperties;

    @Bean
    public <T extends Event> GatewayDiscordClient gatewayDiscordClient(List<DiscordService<T>> services) {
      GatewayDiscordClient gatewayDiscordClient = DiscordClientBuilder.create(discordClientProperties.getToken())
          .build()
          .gateway()
          // Enable Guild member intent
          .setEnabledIntents(IntentSet.of(Intent.GUILD_MEMBERS, Intent.DIRECT_MESSAGES, Intent.GUILD_MESSAGES, Intent.GUILD_VOICE_STATES))
          .login()
          .block();

      // Register all services
      for (DiscordService<T> discordService: services) {
        log.info(discordService.getClass().getName());
        gatewayDiscordClient
            .on(discordService.getEventType())
            .flatMap(discordService::execute)
            .subscribe();
      }

      return gatewayDiscordClient;
    }
}
