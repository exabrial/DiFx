package com.github.exabrial.difx.fxml.model.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Maps a controller class to its FXML resource. The {@link #value()} is a resource path resolved relative to the controller's package;
 * when omitted it defaults to the controller's simple name plus {@code .fxml} in the same package.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface FxmlView {

	String value() default "";
}
