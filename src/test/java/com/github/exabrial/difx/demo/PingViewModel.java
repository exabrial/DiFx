package com.github.exabrial.difx.demo;

import jakarta.enterprise.context.ApplicationScoped;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Shared, injectable view-model. The controller binds widgets to these properties; presenters write them. Because it is
 * the shared state both sides hold, observers never need to touch the controller's {@code @FXML} nodes directly.
 */
@ApplicationScoped
public class PingViewModel {
	private final StringProperty status = new SimpleStringProperty("click Ping");

	public StringProperty statusProperty() {
		return status;
	}

	public void setStatus(final String value) {
		status.set(value);
	}
}
