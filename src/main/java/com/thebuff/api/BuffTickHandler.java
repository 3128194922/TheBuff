package com.thebuff.api;

@FunctionalInterface
public interface BuffTickHandler {
    void handle(BuffContext context);
}
