package org.molgenis.data.security;

import static com.google.common.collect.ImmutableSet.copyOf;
import static com.google.common.collect.Sets.difference;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.molgenis.data.security.auth.RoleMetadata.*;
import static org.molgenis.security.core.SidUtils.ROLE_PREFIX;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.molgenis.data.DataService;
import org.molgenis.data.Fetch;
import org.molgenis.data.Query;
import org.molgenis.data.Sort;
import org.molgenis.data.security.auth.Role;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.security.core.SidUtils;
import org.molgenis.security.core.runas.RunAsSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

@Component
public class DataserviceRoleHierarchy implements RoleHierarchy {
  private static final Logger LOG = LoggerFactory.getLogger(DataserviceRoleHierarchy.class);
  private static final int PAGE_SIZE = 1000;

  private DataService dataService;

  public DataserviceRoleHierarchy(DataService dataService) {
    this.dataService = dataService;
  }

  @Override
  @RunAsSystem
  public Collection<? extends GrantedAuthority> getReachableGrantedAuthorities(
      Collection<? extends GrantedAuthority> authorities) {
    if (authorities == null || authorities.isEmpty()) {
      return AuthorityUtils.NO_AUTHORITIES;
    }

    Set<String> roleNames =
        authorities
            .stream()
            .map(GrantedAuthority::getAuthority)
            .filter(name -> name.startsWith(ROLE_PREFIX))
            .map(name -> name.substring(ROLE_PREFIX.length()))
            .collect(toSet());

    Multimap<String, String> allRoleInclusions = getAllRoleInclusions();
    Set<String> newlyDiscovered = roleNames;
    while (!newlyDiscovered.isEmpty()) {
      Set<String> included =
          newlyDiscovered
              .stream()
              .flatMap(role -> allRoleInclusions.get(role).stream())
              .collect(toSet());
      newlyDiscovered = copyOf(difference(included, roleNames));
      roleNames.addAll(newlyDiscovered);
    }

    Set<SimpleGrantedAuthority> reachableRoles =
        roleNames
            .stream()
            .map(SidUtils::createRoleAuthority)
            .map(SimpleGrantedAuthority::new)
            .collect(toSet());
    LOG.debug(
        "getReachableGrantedAuthorities() - From the roles {} one can reach {} in zero or more steps.",
        authorities,
        reachableRoles);
    return reachableRoles;
  }

  private Multimap<String, String> getAllRoleInclusions() {
    ImmutableMultimap.Builder<String, String> result = ImmutableMultimap.builder();
    for (Role role : getAllRoles()) {
      for (Role included : role.getIncludes()) {
        result.put(role.getName(), included.getName());
      }
    }
    return result.build();
  }

  private List<Role> getAllRoles() {
    ImmutableList.Builder<Role> result = new ImmutableList.Builder<>();
    int offSet = 0;
    List<Role> batch = getRolesBatch(offSet);
    while (batch.size() == PAGE_SIZE) {
      result.addAll(batch);
      batch = getRolesBatch(++offSet);
    }
    result.addAll(batch);
    return result.build();
  }

  private List<Role> getRolesBatch(int offSet) {
    Query<Role> query = QueryImpl.query();
    Fetch fetch = new Fetch();
    Fetch childFetch = new Fetch();
    childFetch.field(NAME);
    fetch.field(NAME).field(INCLUDES, childFetch);
    query.fetch(fetch);
    query.pageSize(PAGE_SIZE);
    query.offset(offSet);
    query.sort(new Sort(NAME));
    return dataService.findAll(ROLE, query, Role.class).collect(toList());
  }
}
