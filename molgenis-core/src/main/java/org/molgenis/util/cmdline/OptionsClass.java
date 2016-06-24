package org.molgenis.util.cmdline;

import java.lang.annotation.*;

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
};
