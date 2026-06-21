package com.github.exabrial.difx.bootstrap;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.enterprise.util.AnnotationLiteral;
import jakarta.inject.Qualifier;

/**
 * Qualifies the CDI event carrying the JavaFX primary {@code Stage}, fired once the container is up and the FX
 * Application Thread is running. Observe it to build and show the first window:
 * {@code void onStartup(@Observes @StartupStage Stage primaryStage)}.
 */
@Qualifier
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD, ElementType.TYPE })
public @interface StartupStage {

	/** Literal for firing the event programmatically through a {@code BeanManager}. */
	AnnotationLiteral<StartupStage> LITERAL = new AnnotationLiteral<StartupStage>() {
		private static final long serialVersionUID = 1L;
	};
}
