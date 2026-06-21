package com.github.exabrial.difx.ui;

import java.util.concurrent.Executor;

/**
 * Executes work on the JavaFX Application Thread. Composes with standard async APIs:
 * {@code completionStage.thenAcceptAsync(action, uiExecutor)}.
 */
public interface UiExecutor extends Executor {
}
