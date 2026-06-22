package com.github.exabrial.difx.fxmlscoped;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.enterprise.inject.Stereotype;

/**
 * CDI pseudo-scope for FXML controllers. No proxy, so FXMLLoader can write FXML fields directly. Unlike Dependent, CDI event observers
 * fire on the original instance. Scope is active from FXML load until the view is closed.
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD, ElementType.FIELD })
@Stereotype
public @interface FxmlScoped {
}
