package com.example.fphh.service;

import discord4j.core.event.domain.Event;
import reactor.core.publisher.Mono;

public interface DiscordService<T extends Event> {
  Class<T> getEventType();
  Mono<Void> execute(T event);
}
