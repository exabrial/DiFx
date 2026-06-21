package com.github.exabrial.difx.fxml;

import jakarta.enterprise.context.spi.CreationalContext;

import javafx.fxml.FXMLLoader;

/**
 * Bundles a single-use {@link FXMLLoader} with the {@link CreationalContext} used to resolve its controllers.
 */
record DiFxFxmlLoader(FXMLLoader fxmlLoader, CreationalContext<?> creationalContext) {
}
