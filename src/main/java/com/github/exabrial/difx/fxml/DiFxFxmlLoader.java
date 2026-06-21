package com.github.exabrial.difx.fxml;

import jakarta.enterprise.context.spi.CreationalContext;

import javafx.fxml.FXMLLoader;

/**
 * Bundles a single-use {@link FXMLLoader} with the {@link CreationalContext} against which every controller for one load (the
 * top-level controller and any nested {@code fx:include} controllers) is resolved. The view loader captures the creational context
 * before disposing this holder, so the dependent controllers can be destroyed together later by releasing that one context.
 */
record DiFxFxmlLoader(FXMLLoader fxmlLoader, CreationalContext<?> creationalContext) {
}
