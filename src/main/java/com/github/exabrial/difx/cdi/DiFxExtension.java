package com.github.exabrial.difx.cdi;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.BeforeBeanDiscovery;
import jakarta.enterprise.inject.spi.Extension;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.exabrial.difx.fxml.DiFxViewLoader;
import com.github.exabrial.difx.fxml.FxmlLoaderProducer;
import com.github.exabrial.difx.ui.FxUiExecutor;

/**
 * Portable CDI extension that registers DiFx's beans: {@link FxUiExecutor}, {@link FxmlLoaderProducer}, and {@link DiFxViewLoader}.
 * Discovered via {@code META-INF/services}. DiFx's jar uses {@code bean-discovery-mode="none"}, so these types are registered here
 * rather than by classpath scanning.
 */
public class DiFxExtension implements Extension {
	private static final Logger log = LoggerFactory.getLogger(DiFxExtension.class);

	void observeBeforeBeanDiscovery(@Observes final BeforeBeanDiscovery event, final BeanManager beanManager) {
		log.info("registerDiFxBeans() registering DiFx beans with the container...");
		addType(event, beanManager, FxUiExecutor.class);
		addType(event, beanManager, FxmlLoaderProducer.class);
		addType(event, beanManager, DiFxViewLoader.class);
	}

	static final <T> void addType(final BeforeBeanDiscovery event, final BeanManager beanManager, final Class<T> type) {
		event.addAnnotatedType(beanManager.createAnnotatedType(type), type.getName());
	}
}
