package org.molgenis.data;

import static java.util.Objects.requireNonNull;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;

@SuppressWarnings("java:MaximumInheritanceDepth")
public class EntityTypeReferencedException extends DataConstraintViolationException {
  private static final String ERROR_CODE = "D19";

  private final Map<String, Set<String>> entityTypeDependencyMap;

  public EntityTypeReferencedException(
      Map<String, Set<String>> entityTypeDependencyMap, @Nullable @CheckForNull Throwable cause) {
    super(ERROR_CODE, cause);
    this.entityTypeDependencyMap = requireNonNull(entityTypeDependencyMap);
  }

  @Override
  public String getMessage() {
    return "dependencies:"
        + entityTypeDependencyMap.entrySet().stream()
            .map(entry -> entry.getKey() + '=' + String.join(",", entry.getValue()))
            .collect(Collectors.joining(";"));
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    String message =
        entityTypeDependencyMap.entrySet().stream()
            .map(entry -> entry.getKey() + " -> [" + String.join(", ", entry.getValue()) + "]")
            .collect(Collectors.joining(", "));
    return new Object[] {message};
  }
}
