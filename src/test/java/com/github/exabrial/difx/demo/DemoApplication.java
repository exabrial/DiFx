package com.github.exabrial.difx.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.exabrial.difx.bootstrap.DiFxApplication;

import jakarta.inject.Inject;

/**
 * User-style application. It is self-injected by DiFx, so its own {@code @Inject} injection points resolve even though
 * the instance is not a contextual bean. {@code afterInjection} proves the injected collaborator is available.
 */
public class DemoApplication extends DiFxApplication {
	private static final Logger log = LoggerFactory.getLogger(DemoApplication.class);

	@Inject
	private GreetingMessage greetingMessage;

	@Override
	protected void afterInjection() {
		log.info("afterInjection() application injection points resolved, greeting:{}", greetingMessage.text());
	}
}
