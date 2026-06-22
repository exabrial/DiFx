package com.github.exabrial.difx.fxmlscoped.extension;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.AfterBeanDiscovery;
import jakarta.enterprise.inject.spi.BeforeBeanDiscovery;
import jakarta.enterprise.inject.spi.Extension;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tomitribe.microscoped.core.ScopeContext;

import com.github.exabrial.difx.fxmlscoped.FxmlScoped;

public class FxmlScopedExtension implements Extension {
	private static final Logger log = LoggerFactory.getLogger(FxmlScopedExtension.class);

	private final ScopeContext<String> fxmlScopeContext = new ScopeContext<>(FxmlScoped.class);

	void beforeBeanDiscovery(@Observes final BeforeBeanDiscovery bbd) {
		log.info("beforeBeanDiscovery() registering @FxmlScoped pseudo-scope");
		bbd.addScope(FxmlScoped.class, false, false);
	}

	void afterBeanDiscovery(@Observes final AfterBeanDiscovery abd) {
		log.info("afterBeanDiscovery() installing FxmlScoped ScopeContext");
		abd.addContext(fxmlScopeContext);
	}

	public ScopeContext<String> getFxmlScopeContext() {
		return fxmlScopeContext;
	}
}
