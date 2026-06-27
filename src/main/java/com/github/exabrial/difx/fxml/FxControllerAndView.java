package com.github.exabrial.difx.fxml;

import jakarta.enterprise.context.spi.CreationalContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.exabrial.difx.fxmlscoped.context.FxmlScopeContext;

import javafx.scene.Parent;

public record FxControllerAndView<C>(C controller, Parent view, CreationalContext<?> creationalContext, String viewKey,
		FxmlScopeContext scopeContext) implements AutoCloseable {
	private static final Logger log = LoggerFactory.getLogger(FxControllerAndView.class);

	@Override
	public void close() {
		log.debug("close() destroying @FxmlScoped context and releasing creational context for viewKey:{} controller:{}", viewKey,
				controller);
		if (scopeContext != null && viewKey != null) {
			scopeContext.destroy(viewKey);
		}
		creationalContext.release();
	}
}
