package org.molgenis.security.core.runas;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * If a method is annotated with this annotation it is run with role 'ROLE_SYSTEM'
 * <p>
 * The current security context will be replaced with one that contains a SystemSecurityToken
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RunAsSystem
{

}
