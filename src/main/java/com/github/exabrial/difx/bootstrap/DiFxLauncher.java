package com.github.exabrial.difx.bootstrap;

import jakarta.enterprise.inject.se.SeContainer;
import jakarta.enterprise.inject.se.SeContainerInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.exabrial.difx.bootstrap.cdi.qualifier.StartupStage;

import javafx.application.Application;

/**
 * Entry point a DiFx application extends. Calling {@link #launch(String...)} bootstraps a CDI SE container, applies any customization
 * via {@link #configureContainer(SeContainerInitializer)}, then hands control to JavaFX by launching the {@link #applicationClass()}.
 * The container is owned for the entire JavaFX lifecycle and closed when the application exits. This class is instantiated with
 * {@code new}, before any container exists, so it is deliberately not a CDI bean.
 */
public abstract class DiFxLauncher {
	private static final Logger log = LoggerFactory.getLogger(DiFxLauncher.class);

	/**
	 * The JavaFX {@code Application} subclass to launch once the container is up. Return {@link DiFxApplication} (or a subclass) so the
	 * {@link StartupStage} event is fired to your managed beans.
	 */
	protected abstract Class<? extends Application> getApplicationClass();

	/**
	 * Hook to customize container startup before initialization: add or disable bean classes and packages, select alternatives, enable
	 * interceptors, disable discovery, set properties. The default applies nothing.
	 */
	protected void configureContainer(final SeContainerInitializer initializer) {
		log.trace("configureContainer() no customization applied to initializer:{}", initializer);
	}

	public final void launch(final String... arguments) {
		final Class<? extends Application> applicationClass = getApplicationClass();
		log.info("launch() bootstrapping CDI container for application:{}", applicationClass);
		try (SeContainer container = bootstrap()) {
			log.debug("launch() container initialized, handing off to JavaFX:{}", container);
			Application.launch(applicationClass, arguments);
		}
		log.debug("launch() application exited and CDI container closed");
	}

	SeContainer bootstrap() {
		final SeContainerInitializer initializer = SeContainerInitializer.newInstance();
		configureContainer(initializer);
		return initializer.initialize();
	}
}
