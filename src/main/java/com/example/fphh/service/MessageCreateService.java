package com.example.fphh.service;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Slf4j
@Component
public class MessageCreateService implements DiscordService<MessageCreateEvent> {
  @Override
  public Class<MessageCreateEvent> getEventType() {
    return MessageCreateEvent.class;
  }

  @Override
  public Mono<Void> execute(MessageCreateEvent event) {
    Message m = event.getMessage();
    Optional<User> sender = m.getAuthor();
    log.error(m.getContent());
    return Mono.just(m)
        .filter(message -> message.getAuthor().map(user -> !user.isBot()).orElse(false))
        .filter(message -> message.getContent().equalsIgnoreCase("good morning ranni"))
        .flatMap(Message::getChannel)
        .flatMap(channel -> channel.createMessage("Hello " + sender.get().getUsername()))
        .then();
  }
}
