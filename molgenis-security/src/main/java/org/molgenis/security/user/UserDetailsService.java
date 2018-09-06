package org.molgenis.security.user;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.security.auth.RoleMembershipMetadata.ROLE_MEMBERSHIP;
import static org.molgenis.data.security.auth.RoleMembershipMetadata.USER;
import static org.molgenis.security.core.utils.SecurityUtils.AUTHORITY_USER;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import org.molgenis.data.DataService;
import org.molgenis.data.security.auth.Role;
import org.molgenis.data.security.auth.RoleMembership;
import org.molgenis.data.security.auth.User;
import org.molgenis.data.security.auth.UserMetaData;
import org.molgenis.security.core.SidUtils;
import org.molgenis.security.core.runas.RunAsSystem;
import org.molgenis.security.core.utils.SecurityUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class UserDetailsService
    implements org.springframework.security.core.userdetails.UserDetailsService {
  private final DataService dataService;
  private final GrantedAuthoritiesMapper grantedAuthoritiesMapper;

  public UserDetailsService(
      DataService dataService, GrantedAuthoritiesMapper grantedAuthoritiesMapper) {
    this.dataService = requireNonNull(dataService);
    this.grantedAuthoritiesMapper = requireNonNull(grantedAuthoritiesMapper);
  }

  @Override
  @RunAsSystem
  public UserDetails loadUserByUsername(String username) {
    User user =
        dataService
            .query(UserMetaData.USER, User.class)
            .eq(UserMetaData.USERNAME, username)
            .findOne();
    if (user == null) {
      throw new UsernameNotFoundException("unknown user '" + username + "'");
    }

    Collection<? extends GrantedAuthority> authorities = getAuthorities(user);
    return new org.springframework.security.core.userdetails.User(
        user.getUsername(), user.getPassword(), user.isActive(), true, true, true, authorities);
  }

  @RunAsSystem
  public Collection<? extends GrantedAuthority> getAuthorities(User user) {
    Set<GrantedAuthority> authorities = new LinkedHashSet<>();

    if (user.isSuperuser() != null && user.isSuperuser()) {
      authorities.add(new SimpleGrantedAuthority(SecurityUtils.AUTHORITY_SU));
    }
    if (user.getUsername().equals(SecurityUtils.ANONYMOUS_USERNAME)) {
      authorities.add(new SimpleGrantedAuthority(SecurityUtils.AUTHORITY_ANONYMOUS));
    } else {
      authorities.add(new SimpleGrantedAuthority(AUTHORITY_USER));
    }

    dataService
        .query(ROLE_MEMBERSHIP, RoleMembership.class)
        .eq(USER, user)
        .findAll()
        .filter(RoleMembership::isCurrent)
        .map(RoleMembership::getRole)
        .map(Role::getName)
        .map(SidUtils::createRoleAuthority)
        .map(SimpleGrantedAuthority::new)
        .forEach(authorities::add);

    return grantedAuthoritiesMapper.mapAuthorities(authorities);
  }
}
