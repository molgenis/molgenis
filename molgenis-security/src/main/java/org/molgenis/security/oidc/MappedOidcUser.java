package org.molgenis.security.oidc;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nullable;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.util.Assert;

/**
 * {@link DefaultOidcUser} with overrides.
 *
 * <p><img src="{@docRoot}/doc-files/MappedOidcUser.png" width="640">
 */
class MappedOidcUser extends DefaultOidcUser {

  /** If present, this is the value for getName() */
  @Nullable private final String usernameOverride;
  /** The name of the key used to retrieve getEmail() */
  private final String emailAttributeKey;

  /**
   * Creates an OidcUser with override for the email and username attribute names.
   *
   * @param original the {@link OidcUser} to copy
   * @param emailAttributeKey the claim key to look up in {@link #getEmail()}
   * @param usernameAttributeKey the claim key to look up in {@link #getName()}
   */
  MappedOidcUser(OidcUser original, String emailAttributeKey, String usernameAttributeKey) {
    super(
        original.getAuthorities(),
        original.getIdToken(),
        original.getUserInfo(),
        usernameAttributeKey);
    this.usernameOverride = null;
    this.emailAttributeKey = requireNonNull(emailAttributeKey);
    Assert.hasText(emailAttributeKey, "emailAttributeKey cannot be empty");
    if (!original.getAttributes().containsKey(emailAttributeKey)) {
      throw new IllegalArgumentException(
          "Missing email attribute '" + emailAttributeKey + "' in attributes");
    }
  }

  /**
   * Creates an OidcUser that's a copy of an existing OidcUser but with overrides for {@link
   * #getAuthorities()} and {@link #getName()} and a custom attribute to use in {@link #getEmail()}.
   *
   * @param original the {@link OidcUser} to copy
   * @param authoritiesOverride overrides {@link #getAuthorities()}
   * @param usernameOverride overrides {@link #getName()}
   * @param emailAttributeKey name of the attribute to look up in {@link #getEmail()}
   */
  MappedOidcUser(
      OidcUser original,
      Collection<? extends GrantedAuthority> authoritiesOverride,
      String emailAttributeKey,
      String usernameOverride) {
    super(authoritiesOverride, original.getIdToken(), original.getUserInfo());
    this.usernameOverride = usernameOverride;
    this.emailAttributeKey = requireNonNull(emailAttributeKey);
  }

  /**
   * User permissions get assigned to the MOLGENIS username.
   *
   * <p>{@link org.springframework.security.acls.domain.PrincipalSid#PrincipalSid(Authentication)}
   * calls {@link Authentication#getName()} to check ACLs. {@link
   * AbstractAuthenticationToken#getName()} calls {@link AuthenticatedPrincipal#getName()} on its
   * {@link AbstractAuthenticationToken#getPrincipal()}
   *
   * <p>Our parent uses the nameAttributeKey but the MOLGENIS username cannot be derived from the
   * claims because the mapping between them is configurable.
   *
   * <p>Fixes: https://github.com/molgenis/molgenis/issues/8985
   */
  @Override
  public String getName() {
    return Optional.ofNullable(usernameOverride).orElse(super.getName());
  }

  @Override
  public String getEmail() {
    return getClaimAsString(emailAttributeKey);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    MappedOidcUser that = (MappedOidcUser) o;
    return Objects.equals(usernameOverride, that.usernameOverride)
        && emailAttributeKey.equals(that.emailAttributeKey);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), usernameOverride, emailAttributeKey);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("Name: [");
    sb.append(getName());
    sb.append("], Email: [");
    sb.append(getEmail());
    sb.append("], Granted Authorities: [");
    sb.append(getAuthorities());
    sb.append("], User Attributes: [");
    sb.append(getAttributes());
    sb.append("]");
    return sb.toString();
  }
}
