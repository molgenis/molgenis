package org.molgenis.js.magma;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Runs a method within a single JsMagmaScriptContext. You may nest methods that are annotated and
 * they'll run within the same context.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface WithJsMagmaScriptContext {}
