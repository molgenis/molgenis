package org.molgenis.web.exception;

import static java.lang.String.format;

import java.util.EnumMap;
import java.util.Map;
import org.molgenis.util.UnexpectedEnumException;
import org.springframework.stereotype.Component;

@Component
class ExceptionResponseGeneratorRegistryImpl implements ExceptionResponseGeneratorRegistry {
  private final Map<ExceptionResponseType, ExceptionResponseGenerator<?>> responseGeneratorMap;

  ExceptionResponseGeneratorRegistryImpl() {
    responseGeneratorMap = new EnumMap<>(ExceptionResponseType.class);
  }

  @Override
  public void registerExceptionResponseGenerator(
      ExceptionResponseGenerator exceptionResponseGenerator) {
    ExceptionResponseType type = exceptionResponseGenerator.getType();
    if (responseGeneratorMap.containsKey(type)) {
      throw new IllegalArgumentException(
          format("Duplicate exception response generator [%s]", type));
    }
    responseGeneratorMap.put(type, exceptionResponseGenerator);
  }

  @Override
  public ExceptionResponseGenerator getExceptionResponseGenerator(ExceptionResponseType type) {
    ExceptionResponseGenerator<?> exceptionResponseGenerator = responseGeneratorMap.get(type);
    if (exceptionResponseGenerator == null) {
      throw new UnexpectedEnumException(type);
    }
    return exceptionResponseGenerator;
  }
}
