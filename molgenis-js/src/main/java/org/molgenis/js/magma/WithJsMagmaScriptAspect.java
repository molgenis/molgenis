package org.molgenis.js.magma;

import static java.util.Objects.requireNonNull;
import static org.molgenis.js.magma.JsMagmaScriptContextHolder.clearContext;
import static org.molgenis.js.magma.JsMagmaScriptContextHolder.getContext;
import static org.molgenis.js.magma.JsMagmaScriptContextHolder.setContext;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.molgenis.js.graal.GraalScriptEngine;
import org.springframework.stereotype.Component;

/** Proxy that keeps a {@link JsMagmaScriptContext} around for the duration of the call. */
@Aspect
@Component
public class WithJsMagmaScriptAspect {

  private final GraalScriptEngine engine;

  public WithJsMagmaScriptAspect(GraalScriptEngine engine) {
    this.engine = requireNonNull(engine);
  }

  @SuppressWarnings("java:S00112") // generic exceptions should never be thrown
  @Around("@annotation(org.molgenis.js.magma.WithJsMagmaScriptContext)")
  public Object aroundAdvice(ProceedingJoinPoint joinPoint) throws Throwable {
    return withJsMagmaScriptContext(engine, joinPoint::proceed);
  }

  public interface RunnableWithJsContext<T, U extends Throwable> {
    T run() throws U;
  }

  public static <T, U extends Throwable> T withJsMagmaScriptContext(
      GraalScriptEngine engine, RunnableWithJsContext<T, U> runnable) throws U {
    JsMagmaScriptContext context = getContext();
    boolean createAndClose = context == null;
    if (createAndClose) {
      context = new JsMagmaScriptContext(engine.createContext());
      setContext(context);
    }
    try {
      context.enter();
      return runnable.run();
    } finally {
      context.leave();
      if (createAndClose) {
        context.close();
        clearContext();
      }
    }
  }
}
