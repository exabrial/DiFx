package com.github.exabrial.difx.fxml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.spi.CreationalContext;
import javafx.scene.Parent;

/**
 * A loaded FXML view: the managed controller bean, its root {@link Parent} node, and the {@link CreationalContext}
 * against which the controller (and any nested {@code fx:include} controllers) was resolved, bundled so callers receive
 * everything from a single load.
 *
 * <p>
 * This is {@link AutoCloseable}: closing it releases the creational context, destroying the dependent controllers
 * created for this load (firing their {@code @PreDestroy} and deregistering any {@code @Observes} methods). Transient
 * views (dialogs, per-conversation panes) should be closed on teardown, ideally via try-with-resources. A view that
 * lives for the whole application run (the main shell) is simply never closed.
 */
public record FxControllerAndView<C>(C controller, Parent view, CreationalContext<?> creationalContext) implements AutoCloseable {
	private static final Logger log = LoggerFactory.getLogger(FxControllerAndView.class);

	@Override
	public void close() {
		log.debug("close() releasing creational context for controller:{}", controller);
		creationalContext.release();
	}
}
