package org.molgenis.security.core;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.springframework.security.test.context.support.WithSecurityContext;

/** Akin to @WithMockUser but sets a SystemSecurityToken. */
@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockSystemUserSecurityContextFactory.class)
public @interface WithMockSystemUser {

  /**
   * If non-empty, creates an elevated SystemSecurityToken token.
   *
   * @return the original username
   */
  String originalUsername() default "";
}
