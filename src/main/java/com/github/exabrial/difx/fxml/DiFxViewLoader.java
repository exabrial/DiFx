package com.github.exabrial.difx.fxml;

import java.io.IOException;
import java.net.URL;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tomitribe.microscoped.core.ScopeContext;

import com.github.exabrial.difx.fxml.model.annotation.FxmlView;
import com.github.exabrial.difx.fxmlscoped.FxmlScoped;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

@ApplicationScoped
public class DiFxViewLoader {
	private static final Logger log = LoggerFactory.getLogger(DiFxViewLoader.class);

	@Inject
	private Instance<DiFxFxmlLoader> fxmlLoaders;
	@Inject
	private BeanManager beanManager;

	@SuppressWarnings("unchecked")
	public <C> FxControllerAndView<C> load(final Class<C> controllerType) {
		final String resourceName = fxmlResourceName(controllerType);
		final URL location = controllerType.getResource(resourceName);
		if (location == null) {
			log.error("load() FXML resource not found for controller:{} resource:{}", controllerType, resourceName);
			throw new IllegalStateException("FXML not found: " + resourceName + " for " + controllerType.getName());
		}
		final DiFxFxmlLoader difxFxmlLoader = fxmlLoaders.get();
		final FxControllerAndView<C> controllerAndView;
		try {
			final FXMLLoader loader = difxFxmlLoader.fxmlLoader();
			loader.setLocation(location);
			final Parent view = load(loader, location);
			final C controller = controllerType.cast(loader.getController());
			log.debug("load() loaded view for controller:{}", controllerType);
			ScopeContext<String> scopeContext = null;
			try {
				scopeContext = (ScopeContext<String>) beanManager.getContext(FxmlScoped.class);
			} catch (final Exception ignored) {
				// scope not active â controller is @Dependent, not @FxmlScoped
			}
			controllerAndView = new FxControllerAndView<>(controller, view, difxFxmlLoader.creationalContext(), difxFxmlLoader.viewKey(),
					scopeContext);
		} finally {
			fxmlLoaders.destroy(difxFxmlLoader);
		}
		return controllerAndView;
	}

	static final Parent load(final FXMLLoader loader, final URL location) {
		final Parent view;
		try {
			view = loader.load();
		} catch (final IOException ioException) {
			throw new IllegalStateException("Failed to load FXML: " + location, ioException);
		}
		return view;
	}

	static final String fxmlResourceName(final Class<?> controllerType) {
		final FxmlView fxmlView = controllerType.getAnnotation(FxmlView.class);
		final String resourceName;
		if (fxmlView != null && !fxmlView.value().isEmpty()) {
			resourceName = fxmlView.value();
		} else {
			resourceName = controllerType.getSimpleName() + ".fxml";
		}
		return resourceName;
	}
}
