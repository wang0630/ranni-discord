package com.example.fphh.job;

import com.example.fphh.config.DiscordClientProperties;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.object.command.Interaction;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.Button;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.spec.MessageCreateSpec;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
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
    //this.run();
  }

  @Override
  @Scheduled(cron = "0 30 21 * * *", zone = "GMT+8")
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

    mono1.then(this.replyDailyCheckButton()).subscribe();
  }

  private Mono<Void> replyDailyCheckButton() {
    return gatewayClient.on(ButtonInteractionEvent.class, e -> {

      Interaction interaction = e.getInteraction();
      Snowflake id = interaction.getUser().getId();
      String username = interaction.getUser().getUsername();

      Boolean answer = dailyCheckCacheManager.getCache("dailyCheckCache").get(id.asString(), Boolean.class);
      if (answer != null) {
        return answer ?
            e.reply()
                .withContent("你今天回答過囉～ 等一下見, " + username)
                 //.withEphemeral(Boolean.TRUE)
                .then(this.pinDailyCheckMessage()) :
            e.reply()
                .withContent("再不來菈妮要把你變不見了喔 :heart: " + username)
                //.withEphemeral(Boolean.TRUE)
                .then(this.pinDailyCheckMessage());
      }

      if (e.getCustomId().equals(CONFIRM_BUTTON_ID_PREFIX)) {
        dailyCheckCacheManager.getCache("dailyCheckCache").put(id.asString(), true);
        return e.reply()
            .withContent("菈妮愛你喔, " + username)
            .then(this.pinDailyCheckMessage());
      } else if (e.getCustomId().equals(DENY_BUTTON_ID_PREFIX)) {
        dailyCheckCacheManager.getCache("dailyCheckCache").put(id.asString(), false);
        return e.reply()
            .withContent("君を大嫌い, " + username)
            .then(this.pinDailyCheckMessage());
      }

      return Mono.empty();
    }).timeout(Duration.of(1, ChronoUnit.HOURS)).then();
  }

  private Mono<Void> pinDailyCheckMessage() {
    Snowflake channelId = Snowflake.of(discordClientProperties.getGeneralTextChannelId());
    Snowflake guildId = Snowflake.of(discordClientProperties.getGuildId());

    return gatewayClient.getChannelById(channelId).ofType(TextChannel.class)
        .flatMapMany(textChannel -> {
          return textChannel.getPinnedMessages();
        })
        .flatMap(message -> {
          return message.delete();
        })
        .then(Mono.zip(gatewayClient.getGuildMembers(guildId).collectList(),
                gatewayClient.getChannelById(channelId).ofType(TextChannel.class))
            .flatMap(tuple -> {
              List<Member> members = tuple.getT1();
              TextChannel textChannel = tuple.getT2();

              Cache cache = dailyCheckCacheManager.getCache("dailyCheckCache");

              if (cache == null) return Mono.empty();
              log.error("Ranni 開心說");
              List<String> messages = new ArrayList<>();

              for (Member member: members) {
                if (member.isBot()) continue;

                Optional<Boolean> isChecked = Optional.ofNullable(cache.get(member.getId().asString(), Boolean.class));
                String display = isChecked.orElse(Boolean.FALSE) ? "Yes" : "No";
                messages.add(member.getUsername() + ": " + display);
              }

              // Join with new line
              String finalMessage = "Ranni 開心說 :heart: \n" + String.join("\n", messages);

              return textChannel.createMessage(finalMessage);
            })
            .flatMap(message -> message.pin())
            .then());
  }
}
