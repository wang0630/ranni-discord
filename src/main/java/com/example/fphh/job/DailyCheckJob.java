package com.example.fphh.job;

import com.example.fphh.config.DiscordClientProperties;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.object.command.Interaction;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.Button;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.core.spec.MessageCreateSpec;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class DailyCheckJob implements Runnable {
  private final GatewayDiscordClient gatewayClient;
  private final DiscordClientProperties discordClientProperties;

  @Qualifier("dailyCheckCacheManager")
  private final CacheManager dailyCheckCacheManager;

  public static String CONFIRM_BUTTON_ID_PREFIX = "dailyCheckConfirm";
  public static String DENY_BUTTON_ID_PREFIX = "dailyCheckDeny";

  @PostConstruct
  public void post() {
    this.run();
  }

  @Override
  //@Scheduled
  public void run() {
    Snowflake channelId = Snowflake.of(discordClientProperties.getGeneralTextChannelId());
    Button confirm = Button.primary(CONFIRM_BUTTON_ID_PREFIX, "Confirm");
    Button deny = Button.danger(DENY_BUTTON_ID_PREFIX, "0");

    Mono<Message> mono1 = gatewayClient.getChannelById(channelId)
        .ofType(TextChannel.class)
        .flatMap(textChannel -> {
          return textChannel.createMessage(
              MessageCreateSpec
                  .builder()
                  .content("菈妮來點名～ 哪個小壞壞不上線？ :heart:")
                  .addComponent(ActionRow.of(confirm, deny))
                  .build()
          );
        });

    mono1.then(execute()).subscribe();
  }

  public Mono<Void> execute() {
    return gatewayClient.on(ButtonInteractionEvent.class, e -> {

      Interaction interaction = e.getInteraction();
      Snowflake id = interaction.getUser().getId();
      String username = interaction.getUser().getUsername();

      Boolean answer = dailyCheckCacheManager.getCache("dailyCheckCache").get(id.asString(), Boolean.class);
      if (answer != null) {
        return answer ?
            e.reply().withContent("你今天回答過囉～ 等一下見, " + username) :
            e.reply().withContent("再不來菈妮要把你變不見了喔 :heart: " + username);
      }

      if (e.getCustomId().equals(CONFIRM_BUTTON_ID_PREFIX)) {
        dailyCheckCacheManager.getCache("dailyCheckCache").put(id.asString(), true);
        return e.reply().withContent("菈妮愛你喔, " + username);
      } else if (e.getCustomId().equals(DENY_BUTTON_ID_PREFIX)) {
        dailyCheckCacheManager.getCache("dailyCheckCache").put(id.asString(), false);
        return e.reply().withContent("君を大嫌い, " + username);
      }

      return Mono.empty();
    }).timeout(Duration.ofMinutes(30)).then();
  }
}
