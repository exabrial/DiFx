package com.github.exabrial.difx.demo;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * FXML controller for the Ping screen, resolved as a managed bean by the DiFx controller factory. It is {@code @Dependent}
 * so the factory hands JavaFX the real instance for {@code @FXML} field injection. It binds its label to the shared
 * view-model once, then publishes a {@link Ping} on click: synchronously (for {@code @Observes} on the FX thread) and
 * asynchronously (for {@code @ObservesAsync} off the FX thread).
 */
@Dependent
public class PingController {
	@Inject
	private Event<Ping> pingEvent;
	@Inject
	private PingViewModel viewModel;

	@FXML
	private Label statusLabel;

	@FXML
	public void initialize() {
		statusLabel.textProperty().bind(viewModel.statusProperty());
	}

	@FXML
	public void onPing() {
		final Ping ping = new Ping("ping-button");
		pingEvent.fire(ping);
		pingEvent.fireAsync(ping);
	}
}
