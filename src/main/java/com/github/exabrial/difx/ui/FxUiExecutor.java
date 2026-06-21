package com.github.exabrial.difx.ui;

import jakarta.enterprise.context.ApplicationScoped;
import javafx.application.Platform;

/**
 * {@link UiExecutor} backed by {@link Platform#runLater}. If the caller is already on the FX Application Thread the
 * command runs inline, so callers never need to check the thread themselves.
 */
@ApplicationScoped
public class FxUiExecutor implements UiExecutor {

	@Override
	public void execute(final Runnable command) {
		if (Platform.isFxApplicationThread()) {
			command.run();
		} else {
			Platform.runLater(command);
		}
	}
}
