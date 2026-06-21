package com.github.exabrial.difx.fxml;

import jakarta.enterprise.context.spi.CreationalContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.scene.Parent;

/**
 * A loaded FXML view: the managed controller, its root node, and the CreationalContext. AutoCloseable â closing releases the
 * creational context and destroys the dependent controllers created for this load. Transient views should be closed on teardown; a
 * view that lives for the whole application run is simply never closed.
 */
public record FxControllerAndView<C>(C controller, Parent view, CreationalContext<?> creationalContext) implements AutoCloseable {
	private static final Logger log = LoggerFactory.getLogger(FxControllerAndView.class);

	@Override
	public void close() {
		log.debug("close() releasing creational context for controller:{}", controller);
		creationalContext.release();
	}
}
