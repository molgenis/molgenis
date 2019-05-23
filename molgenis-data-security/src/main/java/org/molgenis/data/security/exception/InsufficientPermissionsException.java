package org.molgenis.data.security.exception;

import static java.util.Objects.requireNonNull;
import static org.molgenis.i18n.MessageSourceHolder.getMessageSource;
import static org.springframework.context.i18n.LocaleContextHolder.getLocale;

import java.util.ArrayList;
import java.util.List;
import org.springframework.security.acls.model.ObjectIdentity;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public class InsufficientPermissionsException extends PermissionDeniedException {
  private static final String ERROR_CODE = "DS26";
  private final String identifier;
  private final String type;
  private final List<String> roles;

  public InsufficientPermissionsException(ObjectIdentity objectIdentity, List<String> roles) {
    super(ERROR_CODE);
    requireNonNull(objectIdentity);
    this.identifier = objectIdentity.getIdentifier().toString();
    this.type = objectIdentity.getType();
    this.roles = requireNonNull(roles);
  }

  public InsufficientPermissionsException(String type, String identifier, List<String> roles) {
    super(ERROR_CODE);
    this.type = requireNonNull(type);
    this.identifier = requireNonNull(identifier);
    this.roles = requireNonNull(roles);
  }

  @Override
  public String getMessage() {
    return String.format("type:%s, identifier:%s, roles:%s", type, identifier, roles);
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    List<String> translatedRoles = new ArrayList<>();
    for (String role : roles) {
      String code = "role-" + role;
      translatedRoles.add(getMessageSource().getMessage(code, null, getLocale()));
    }
    return new Object[] {type, identifier, String.join(",", translatedRoles)};
  }
}
