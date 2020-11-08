package org.molgenis.js.magma;

import static java.util.Objects.requireNonNull;

/**
 * The JsMagmaScriptContext is expensive to create.
 *
 * <p>This thread-local cache allows high-level calls to reuse a single instance for a series of
 * calls without passing it as a parameter all the way down the call chain into the
 * JsMagmaScriptEvaluator.
 */
public class JsMagmaScriptContextHolder {

  private static final ThreadLocal<JsMagmaScriptContext> contextHolder = new ThreadLocal<>();

  public static JsMagmaScriptContext getContext() {
    return contextHolder.get();
  }

  static void clearContext() {
    contextHolder.remove();
  }

  public static void setContext(JsMagmaScriptContext context) {
    contextHolder.set(requireNonNull(context));
  }
}
