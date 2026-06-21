package com.github.exabrial.difx.fxml;

import java.io.IOException;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

/**
 * Loads an FXML view by its controller class, with the controller (and any nested controllers) resolved from the CDI
 * container. The FXML resource is located by {@link FxmlView} or, by default, the controller's simple name plus
 * {@code .fxml} in the same package. Returns the managed controller and its root node together.
 */
@ApplicationScoped
public class DiFxViewLoader {
	private static final Logger log = LoggerFactory.getLogger(DiFxViewLoader.class);

	@Inject
	private Instance<DiFxFxmlLoader> fxmlLoaders;

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
			controllerAndView = new FxControllerAndView<>(controller, view, difxFxmlLoader.creationalContext());
		} finally {
			fxmlLoaders.destroy(difxFxmlLoader);
		}
		return controllerAndView;
	}

	static String fxmlResourceName(final Class<?> controllerType) {
		final FxmlView fxmlView = controllerType.getAnnotation(FxmlView.class);
		final String resourceName;
		if (fxmlView != null && !fxmlView.value().isEmpty()) {
			resourceName = fxmlView.value();
		} else {
			resourceName = controllerType.getSimpleName() + ".fxml";
		}
		return resourceName;
	}

	static Parent load(final FXMLLoader loader, final URL location) {
		final Parent view;
		try {
			view = loader.load();
		} catch (final IOException e) {
			throw new IllegalStateException("Failed to load FXML: " + location, e);
		}
		return view;
	}
}
