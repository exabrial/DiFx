package com.github.exabrial.difx.ui;

import java.util.concurrent.Executor;

/**
 * Executes work on the JavaFX Application Thread. Because it is a {@link Executor}, it composes with the standard async APIs: for
 * example {@code completionStage.thenAcceptAsync(action, uiExecutor)} marshals a result back onto the FX thread. This is the single,
 * reusable thread-marshalling mechanism. Deciding which view-model state to write in response to which event is a separate,
 * application-level role (a Presenter), which injects a {@code UiExecutor} to do its thread hop.
 */
public interface UiExecutor extends Executor {
}
