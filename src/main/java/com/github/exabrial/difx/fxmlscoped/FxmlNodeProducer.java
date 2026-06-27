package com.github.exabrial.difx.fxmlscoped;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.exabrial.difx.fxmlscoped.context.FxmlScopeContext;

@Dependent
public class FxmlNodeProducer {
	private static final Logger log = LoggerFactory.getLogger(FxmlNodeProducer.class);

	@Inject
	private BeanManager beanManager;

	@Produces
	@Dependent
	FxmlNode produceFxmlNode() {
		final FxmlScopeContext context = (FxmlScopeContext) beanManager.getContext(FxmlScoped.class);
		final String nodeKey = context.currentNodeKey();
		final FxmlNode fxmlNode;
		if (nodeKey == null) {
			throw new IllegalStateException("FxmlNode can only be injected into @FxmlScoped beans; no @FxmlScoped node is active");
		} else {
			log.debug("produceFxmlNode() binding FxmlNode to nodeKey:{}", nodeKey);
			fxmlNode = new FxmlNode(context, nodeKey);
		}
		return fxmlNode;
	}
}
