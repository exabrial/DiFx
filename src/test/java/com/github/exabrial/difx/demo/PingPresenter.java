package com.github.exabrial.difx.demo;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.ObservesAsync;
import jakarta.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.exabrial.difx.ui.UiExecutor;

/**
 * Presenter: observes the {@link Ping} domain event and writes the shared view-model. Demonstrates both delivery modes. The
 * {@code @Observes} method runs on the thread that fired synchronously (the FX thread), so it may write the view-model directly. The
 * {@code @ObservesAsync} method runs on a background executor, does its work off the FX thread, then marshals the view-model write
 * back through {@link UiExecutor}.
 */
@ApplicationScoped
public class PingPresenter {
	private static final Logger log = LoggerFactory.getLogger(PingPresenter.class);

	@Inject
	private PingViewModel viewModel;
	@Inject
	private UiExecutor uiExecutor;

	public void onPingSynchronous(@Observes final Ping ping) {
		final String firingThread = Thread.currentThread().getName();
		log.info("onPingSynchronous() @Observes delivered on thread:{}", firingThread);
		viewModel.setStatus("@Observes on " + firingThread);
	}

	public void onPingAsynchronous(@ObservesAsync final Ping ping) {
		final String workerThread = Thread.currentThread().getName();
		log.info("onPingAsynchronous() @ObservesAsync delivered off the FX thread, on thread:{}", workerThread);
		sleepBriefly();
		uiExecutor.execute(() -> {
			log.info("onPingAsynchronous() view-model write marshalled back to FX thread:{}", Thread.currentThread().getName());
			viewModel.setStatus("@ObservesAsync ran on " + workerThread + ", UI updated via UiExecutor");
		});
	}

	static void sleepBriefly() {
		try {
			Thread.sleep(700);
		} catch (final InterruptedException interruptedException) {
			Thread.currentThread().interrupt();
		}
	}
}
