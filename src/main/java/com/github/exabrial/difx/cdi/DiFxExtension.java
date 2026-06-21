package com.github.exabrial.difx.cdi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.exabrial.difx.fxml.DiFxViewLoader;
import com.github.exabrial.difx.fxml.FxmlLoaderProducer;
import com.github.exabrial.difx.ui.FxUiExecutor;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.BeforeBeanDiscovery;
import jakarta.enterprise.inject.spi.Extension;

/**
 * Portable CDI extension that registers DiFx's own beans with the container: the {@link FxUiExecutor}, the {@link FxmlLoaderProducer},
 * and the {@link DiFxViewLoader}. Registration goes exclusively through this extension, which the container discovers via
 * {@code META-INF/services}, so a consuming application gets DiFx's beans without scanning or listing any DiFx package -- and it works
 * even when the application disables bean discovery. DiFx's own jar is marked {@code bean-discovery-mode="none"} so these types are
 * registered exactly once, here, rather than also being picked up by classpath scanning.
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
