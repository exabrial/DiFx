package com.github.exabrial.difx.demo;

/**
 * Immutable domain event published when the Ping button is pressed. Carries only data, never a JavaFX {@code Node} or
 * {@code Event}, so it is safe to deliver to {@code @ObservesAsync} observers off the FX thread.
 */
public record Ping(String source) {
}
