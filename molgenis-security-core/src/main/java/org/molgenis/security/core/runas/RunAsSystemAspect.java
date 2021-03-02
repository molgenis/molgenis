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
 * Proxy that sets a RunAsSystemToken in the security context for the duration of a method. The
 * original authentication is stored in the RunAsSystemToken. If the current authentication is
 * already a SystemSecurityToken, this authentication is used instead of setting a new one. If there
 * is no original authentication, a normal SystemSecurityToken is used.
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

      if (auth != null) {
        if (!(auth instanceof SystemSecurityToken)) {
          // Elevate this non-system token to RunAsSystem
          setContext(createEmptyContext());
          getContext().setAuthentication(new RunAsSystemToken(auth));
        }
      } else {
        // Set a normal SystemSecurityToken because there is no original authentication to elevate
        setContext(createEmptyContext());
        getContext().setAuthentication(new SystemSecurityToken());
      }

      return runnable.run();
    } finally {
      // Set the original context back when method is finished
      setContext(origCtx);
    }
  }
}
