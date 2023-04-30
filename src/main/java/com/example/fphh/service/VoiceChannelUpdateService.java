package com.example.fphh.service;

import com.example.fphh.config.DiscordClientProperties;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.VoiceStateUpdateEvent;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.channel.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class VoiceChannelUpdateService implements DiscordService<VoiceStateUpdateEvent> {
  private final DiscordClientProperties discordClientProperties;
  @Override
  public Class<VoiceStateUpdateEvent> getEventType() {
    return VoiceStateUpdateEvent.class;
  }

  @Override
  public Mono<Void> execute(VoiceStateUpdateEvent event) {
    if (!event.isJoinEvent()) {
      return Mono.empty();
    }

    VoiceState vs = event.getCurrent();

    return vs.getGuild()
        .flatMap(guild -> {
          return Mono.zip(
              guild.getChannelById(Snowflake.of(discordClientProperties.getGeneralTextChannelId())),
              vs.getUser()
          );
        })
        .flatMap(tuple -> {
          // Channel map here: https://javadoc.io/doc/com.discord4j/discord4j-core/3.0.9/discord4j/core/object/entity/Channel.Type.html
          Channel.Type type = tuple.getT1().getType();
          if (type == Channel.Type.GUILD_TEXT) {
            TextChannel mc = (TextChannel) tuple.getT1();
            return mc.createMessage("Welcome back, Tarnished " + tuple.getT2().getUsername());
          } else {
            return Mono.empty();
          }
        })
        .then();
  }
}
