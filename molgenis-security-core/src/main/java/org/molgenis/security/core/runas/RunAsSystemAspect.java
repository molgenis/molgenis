package org.molgenis.security.core.runas;

import static org.springframework.security.core.context.SecurityContextHolder.createEmptyContext;
import static org.springframework.security.core.context.SecurityContextHolder.getContext;
import static org.springframework.security.core.context.SecurityContextHolder.setContext;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Component;

/**
 * Proxy that sets a SystemSecurityToken in the security context for the duration of a method. The
 * original authentication is stored in the SystemSecurityToken. If the current authentication is
 * already a SystemSecurityToken, this authentication is used instead of setting a new one.
 */
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
@Aspect
@Component
public class RunAsSystemAspect {

  @SuppressWarnings("java:S00112") // generic exceptions should never be thrown
  @Around("@annotation(RunAsSystem)")
  public Object aroundAdvice(ProceedingJoinPoint joinPoint) throws Throwable {
    return runAsSystem((RunnableAsSystem<Object, Throwable>) joinPoint::proceed);
  }

  public static void runAsSystem(Runnable runnable) {
    runAsSystem(
        () -> {
          runnable.run();
          return null;
        });
  }

  public static <T, X extends Throwable> T runAsSystem(RunnableAsSystem<T, X> runnable) throws X {
    // Remember the original context
    SecurityContext origCtx = getContext();
    try {
      var auth = origCtx.getAuthentication();
      if (!(auth instanceof SystemSecurityToken)) {
        // Set a SystemSecurityToken
        setContext(createEmptyContext());
        getContext().setAuthentication(SystemSecurityToken.createElevated(auth));
      }
      return runnable.run();
    } finally {
      // Set the original context back when method is finished
      setContext(origCtx);
    }
  }
}
