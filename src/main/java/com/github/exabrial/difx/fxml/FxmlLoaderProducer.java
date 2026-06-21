package com.github.exabrial.difx.fxml;

import java.util.Set;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.Disposes;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.fxml.FXMLLoader;
import javafx.util.Callback;

/**
 * Produces {@link DiFxFxmlLoader} instances whose controller factory resolves {@code fx:controller} classes from the CDI container.
 * The holder is {@code @Dependent} and single-use; inject {@code Instance<DiFxFxmlLoader>} and obtain a fresh one per FXML load.
 */
@Dependent
public class FxmlLoaderProducer {
	private static final Logger log = LoggerFactory.getLogger(FxmlLoaderProducer.class);

	@Inject
	private BeanManager beanManager;

	@Produces
	@Dependent
	DiFxFxmlLoader createFxmlLoader() {
		final FXMLLoader loader = new FXMLLoader();
		final BeanManager resolvingBeanManager = beanManager;
		final CreationalContext<?> controllerContext = beanManager.createCreationalContext(null);
		final Callback<Class<?>, Object> controllerFactory = (final Class<?> controllerType) -> resolveController(resolvingBeanManager,
				controllerContext, controllerType);
		loader.setControllerFactory(controllerFactory);
		return new DiFxFxmlLoader(loader, controllerContext);
	}

	void disposeFxmlLoader(@Disposes final DiFxFxmlLoader difxFxmlLoader) {
		log.debug("disposeFxmlLoader() disposing loader, controller context left alive for caller");
	}

	static final Object resolveController(final BeanManager beanManager, final CreationalContext<?> controllerContext,
			final Class<?> controllerType) {
		final Set<Bean<?>> beans = beanManager.getBeans(controllerType);
		final Object controller;
		if (beans.isEmpty()) {
			log.error("resolveController() FXML controller is not a CDI bean, annotate it @Dependent:{}", controllerType);
			throw new IllegalStateException("FXML controller is not a managed bean: " + controllerType.getName());
		} else {
			final Bean<?> bean = beanManager.resolve(beans);
			controller = beanManager.getReference(bean, controllerType, controllerContext);
			log.debug("resolveController() resolved managed controller:{}", controllerType);
		}
		return controller;
	}
}
