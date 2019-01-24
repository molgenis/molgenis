package org.molgenis.data;

import static java.util.Objects.requireNonNull;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;

@SuppressWarnings({"squid:MaximumInheritanceDepth"})
public class RepositoryAlreadyExistsException extends DataAlreadyExistsException {
  private static final String ERROR_CODE = "D10";

  private final String repositoryId;

  public RepositoryAlreadyExistsException(String repositoryId) {
    super(ERROR_CODE);
    this.repositoryId = requireNonNull(repositoryId);
  }

  @SuppressWarnings("unused")
  public RepositoryAlreadyExistsException(
      String repositoryId, @Nullable @CheckForNull Throwable cause) {
    super(ERROR_CODE, cause);
    this.repositoryId = requireNonNull(repositoryId);
  }

  @Override
  public String getMessage() {
    return String.format("repository:%s", repositoryId);
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[] {repositoryId};
  }
}
