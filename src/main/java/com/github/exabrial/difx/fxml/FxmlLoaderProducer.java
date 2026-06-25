package com.github.exabrial.difx.fxml;

import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

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

@Dependent
public class FxmlLoaderProducer {
	private static final Logger log = LoggerFactory.getLogger(FxmlLoaderProducer.class);
	private static final AtomicLong viewCounter = new AtomicLong(0);

	@Inject
	private BeanManager beanManager;

	@Produces
	@Dependent
	DiFxFxmlLoader createFxmlLoader() {
		final FXMLLoader loader = new FXMLLoader();
		final BeanManager resolvingBeanManager = beanManager;
		final CreationalContext<?> controllerContext = beanManager.createCreationalContext(null);
		final String viewKey = "fxml-view-" + Long.toHexString(viewCounter.getAndIncrement());
		final Callback<Class<?>, Object> controllerFactory = (final Class<?> controllerType) -> resolveController(resolvingBeanManager,
				controllerContext, controllerType, viewKey);
		loader.setControllerFactory(controllerFactory);
		return new DiFxFxmlLoader(loader, controllerContext, viewKey);
	}

	void disposeFxmlLoader(@Disposes final DiFxFxmlLoader difxFxmlLoader) {
		log.debug("disposeFxmlLoader() disposing loader, controller context left alive for caller");
	}

	static final Object resolveController(final BeanManager beanManager, final CreationalContext<?> controllerContext,
			final Class<?> controllerType, final String viewKey) {
		final Set<Bean<?>> beans = beanManager.getBeans(controllerType);
		final Object controller;
		if (beans.isEmpty()) {
			log.error("resolveController() FXML controller is not a CDI bean, annotate it @Dependent or @FxmlScoped:{}", controllerType);
			throw new IllegalStateException("FXML controller is not a managed bean: " + controllerType.getName());
		} else {
			final Bean<?> bean = beanManager.resolve(beans);
			controller = beanManager.getReference(bean, controllerType, controllerContext);
			log.debug("resolveController() resolved controller:{} viewKey:{}", controllerType, viewKey);
		}
		return controller;
	}
}
