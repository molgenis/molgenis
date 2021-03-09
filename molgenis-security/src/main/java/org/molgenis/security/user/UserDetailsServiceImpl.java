package org.molgenis.security.user;

import static java.util.Objects.requireNonNull;
import static org.molgenis.security.core.utils.SecurityUtils.AUTHORITY_USER;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import org.molgenis.data.security.auth.Role;
import org.molgenis.data.security.auth.RoleMembership;
import org.molgenis.data.security.auth.User;
import org.molgenis.data.security.permission.RoleMembershipService;
import org.molgenis.data.security.user.UserService;
import org.molgenis.security.core.SidUtils;
import org.molgenis.security.core.runas.RunAsSystem;
import org.molgenis.security.core.utils.SecurityUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class UserDetailsServiceImpl implements UserDetailsService {
  private final GrantedAuthoritiesMapper grantedAuthoritiesMapper;
  private final UserService userService;
  private final RoleMembershipService roleMembershipService;

  public UserDetailsServiceImpl(
      GrantedAuthoritiesMapper grantedAuthoritiesMapper,
      UserService userService,
      RoleMembershipService roleMembershipService) {
    this.grantedAuthoritiesMapper = requireNonNull(grantedAuthoritiesMapper);
    this.userService = requireNonNull(userService);
    this.roleMembershipService = requireNonNull(roleMembershipService);
  }

  @Override
  @RunAsSystem
  public UserDetails loadUserByUsername(String username) {
    User user = userService.getUser(username);
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

    if (user.isSuperuser()) {
      authorities.add(new SimpleGrantedAuthority(SecurityUtils.AUTHORITY_SU));
    }
    if (user.getUsername().equals(SecurityUtils.ANONYMOUS_USERNAME)) {
      authorities.add(new SimpleGrantedAuthority(SecurityUtils.AUTHORITY_ANONYMOUS));
    } else {
      authorities.add(new SimpleGrantedAuthority(AUTHORITY_USER));
    }

    roleMembershipService.getCurrentMemberships(user).stream()
        .map(RoleMembership::getRole)
        .map(Role::getName)
        .map(SidUtils::createRoleAuthority)
        .map(SimpleGrantedAuthority::new)
        .forEach(authorities::add);

    return grantedAuthoritiesMapper.mapAuthorities(authorities);
  }
}
