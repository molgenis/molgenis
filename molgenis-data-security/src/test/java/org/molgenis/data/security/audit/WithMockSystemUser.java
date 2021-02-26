package org.molgenis.data.security.audit;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.springframework.security.test.context.support.WithSecurityContext;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockSystemUserSecurityContextFactory.class)
public @interface WithMockSystemUser {
  String originalUsername() default "";
}
