package com.github.exabrial.difx.fxml;

import jakarta.enterprise.context.spi.CreationalContext;

import javafx.fxml.FXMLLoader;

record DiFxFxmlLoader(FXMLLoader fxmlLoader, CreationalContext<?> creationalContext, String viewKey) {
}
