package org.molgenis.util.cmdline;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Option class ? Another kind of option
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface OptionsClass
{
	String name() default "";

	String version() default "0.0.0";

	String description() default "";
}
