package org.molgenis.data;

import static java.util.Objects.requireNonNull;
import static org.molgenis.i18n.MessageSourceHolder.getMessageSource;
import static org.springframework.context.i18n.LocaleContextHolder.getLocale;

import org.molgenis.i18n.BadRequestException;

/**
 * @see Repository
 * @see RepositoryCapability
 */
@SuppressWarnings("squid:MaximumInheritanceDepth")
public class RepositoryNotCapableException extends BadRequestException {
  private static final String ERROR_CODE = "D11";

  private final String repositoryId;
  private final RepositoryCapability repositoryCapability;

  public RepositoryNotCapableException(
      String repositoryId, RepositoryCapability repositoryCapability) {
    super(ERROR_CODE);
    this.repositoryId = requireNonNull(repositoryId);
    this.repositoryCapability = requireNonNull(repositoryCapability);
  }

  @Override
  public String getMessage() {
    return String.format("repository:%s capability:%s", repositoryId, repositoryCapability);
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    String code = "repository-capability-" + repositoryCapability.toString().toLowerCase();
    String capabilityMessage = getMessageSource().getMessage(code, null, getLocale());
    return new Object[] {repositoryId, capabilityMessage};
  }
}
