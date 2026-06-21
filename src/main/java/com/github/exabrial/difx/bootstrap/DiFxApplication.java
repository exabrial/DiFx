package com.github.exabrial.difx.bootstrap;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.event.Event;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.enterprise.inject.spi.InjectionTarget;
import jakarta.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.exabrial.difx.bootstrap.cdi.qualifier.StartupStage;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Base class for the JavaFX {@code Application} the user extends. JavaFX instantiates it by reflection, so it is never a contextual
 * CDI bean (no proxy, no scope, no interceptors on the instance itself); instead its members are injected against the running
 * container in {@code init()}, so the user can freely use {@code @Inject} injection points. On the FX Application Thread,
 * {@code start} fires the primary {@code Stage} as a {@link StartupStage} event for a managed bean to observe.
 */
public abstract class DiFxApplication extends Application {
	private static final Logger log = LoggerFactory.getLogger(DiFxApplication.class);

	@Inject
	@StartupStage
	private Event<Stage> startupStageEventBus;

	@Override
	public final void init() throws Exception {
		final BeanManager beanManager = CDI.current().getBeanManager();
		log.debug("init() injecting members into application instance:{}", getClass());
		injectMembers(beanManager, this);
		afterInjection();
	}

	@Override
	public void start(final Stage primaryStage) {
		log.info("start() firing startup stage event on FX thread for primaryStage:{}", primaryStage);
		startupStageEventBus.fire(primaryStage);
	}

	/**
	 * Override to run initialization that depends on injected members, on the JavaFX launcher thread, after injection but before
	 * {@code start}. The default does nothing.
	 */
	protected void afterInjection() throws Exception {
		log.trace("afterInjection() no post-injection initialization defined");
	}

	static final <T> void injectMembers(final BeanManager beanManager, final T target) {
		@SuppressWarnings("unchecked")
		final AnnotatedType<T> annotatedType = (AnnotatedType<T>) beanManager.createAnnotatedType(target.getClass());
		final InjectionTarget<T> injectionTarget = beanManager.getInjectionTargetFactory(annotatedType).createInjectionTarget(null);
		final CreationalContext<T> creationalContext = beanManager.createCreationalContext(null);
		injectionTarget.inject(target, creationalContext);
		injectionTarget.postConstruct(target);
	}
}
