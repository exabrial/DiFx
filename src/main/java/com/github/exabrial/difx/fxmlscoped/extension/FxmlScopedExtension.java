package com.github.exabrial.difx.fxmlscoped.extension;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.AfterBeanDiscovery;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.BeforeBeanDiscovery;
import jakarta.enterprise.inject.spi.EventContext;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.ObserverMethod;
import jakarta.enterprise.inject.spi.ProcessObserverMethod;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.exabrial.difx.fxmlscoped.FxmlScoped;
import com.github.exabrial.difx.fxmlscoped.context.FxmlScopeContext;

public class FxmlScopedExtension implements Extension {
	private static final Logger log = LoggerFactory.getLogger(FxmlScopedExtension.class);

	private final FxmlScopeContext fxmlScopeContext = new FxmlScopeContext();
	private final Map<ObserverKey, List<ObserverMethod<?>>> observerTable = new HashMap<>();

	void beforeBeanDiscovery(@Observes final BeforeBeanDiscovery beforeBeanDiscovery) {
		log.info("beforeBeanDiscovery() registering @FxmlScoped pseudo-scope");
		beforeBeanDiscovery.addScope(FxmlScoped.class, false, false);
	}

	@SuppressWarnings("unused")
	<T, X> void processObserverMethod(@Observes final ProcessObserverMethod<T, X> processObserverMethod) {
		final ObserverMethod<T> observerMethod = processObserverMethod.getObserverMethod();
		final Class<?> beanClass = observerMethod.getBeanClass();
		if (beanClass.isAnnotationPresent(FxmlScoped.class)) {
			final ObserverKey key = new ObserverKey(observerMethod.getObservedType(), observerMethod.getObservedQualifiers(),
					observerMethod.isAsync());
			observerTable.computeIfAbsent(key, (final ObserverKey absentKey) -> new ArrayList<>()).add(observerMethod);
			processObserverMethod.veto();
			log.debug("processObserverMethod() captured and vetoed @FxmlScoped observer beanClass:{} observedType:{} async:{}", beanClass,
					observerMethod.getObservedType(), observerMethod.isAsync());
		}
	}

	void afterBeanDiscovery(@Observes final AfterBeanDiscovery afterBeanDiscovery, final BeanManager beanManager) {
		log.info("afterBeanDiscovery() installing FxmlScopeContext and synthetic @FxmlScoped event routers count:{}",
				observerTable.size());
		afterBeanDiscovery.addContext(fxmlScopeContext);
		for (final Map.Entry<ObserverKey, List<ObserverMethod<?>>> entry : observerTable.entrySet()) {
			final ObserverKey key = entry.getKey();
			final List<ObserverMethod<?>> originals = entry.getValue();
			afterBeanDiscovery.addObserverMethod().observedType(key.observedType()).qualifiers(key.qualifiers()).async(key.async())
					.notifyWith((final EventContext<Object> eventContext) -> dispatch(beanManager, originals, eventContext));
		}
	}

	public FxmlScopeContext getFxmlScopeContext() {
		return fxmlScopeContext;
	}

	private void dispatch(final BeanManager beanManager, final List<ObserverMethod<?>> originals,
			final EventContext<Object> eventContext) {
		for (final ObserverMethod<?> original : originals) {
			deliverToLiveInstances(beanManager, original, eventContext);
		}
	}

	@SuppressWarnings("unused")
	private void deliverToLiveInstances(final BeanManager beanManager, final ObserverMethod<?> original,
			final EventContext<Object> eventContext) {
		final Class<?> beanClass = original.getBeanClass();
		final Set<Bean<?>> beans = beanManager.getBeans(beanClass);
		final Bean<?> bean = beanManager.resolve(beans);
		if (bean == null) {
			log.warn("deliverToLiveInstances() no bean resolved for @FxmlScoped observer beanClass:{}", beanClass);
		} else {
			fxmlScopeContext.forEachLiveInstance(bean, (final Object instance) -> notifyOne(original, eventContext));
		}
	}

	@SuppressWarnings("unchecked")
	private static void notifyOne(final ObserverMethod<?> original, final EventContext<Object> eventContext) {
		((ObserverMethod<Object>) original).notify(eventContext);
	}

	private static record ObserverKey(Type observedType, Set<Annotation> qualifiers, boolean async) {
	}
}
