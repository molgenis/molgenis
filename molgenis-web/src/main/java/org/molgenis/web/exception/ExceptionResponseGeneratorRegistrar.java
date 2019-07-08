package org.molgenis.web.exception;

import static java.util.Objects.requireNonNull;

import java.util.Map;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Discovers and registers exception response generators with the exception response generator
 * registry.
 *
 * @see ExceptionResponseGenerator
 * @see ExceptionResponseGeneratorRegistry
 */
@Component
public class ExceptionResponseGeneratorRegistrar {
  private final ExceptionResponseGeneratorRegistry responseGeneratorRegistry;

  public ExceptionResponseGeneratorRegistrar(
      ExceptionResponseGeneratorRegistry responseGeneratorRegistry) {
    this.responseGeneratorRegistry = requireNonNull(responseGeneratorRegistry);
  }

  public void register(ApplicationContext applicationContext) {
    Map<String, ExceptionResponseGenerator> responseGeneratorMap =
        applicationContext.getBeansOfType(ExceptionResponseGenerator.class);
    responseGeneratorMap.values().forEach(this::registerResponseGenerator);
  }

  private void registerResponseGenerator(ExceptionResponseGenerator<?> responseGenerator) {
    responseGeneratorRegistry.registerExceptionResponseGenerator(responseGenerator);
  }
}
