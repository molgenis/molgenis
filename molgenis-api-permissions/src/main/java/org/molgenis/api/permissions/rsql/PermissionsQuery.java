package org.molgenis.api.permissions.rsql;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PermissionsQuery {
  private List<String> users;
  private List<String> roles;

  public PermissionsQuery() {
    this.users = new ArrayList<>();
    this.roles = new ArrayList<>();
  }

  public PermissionsQuery(List<String> users, List<String> roles) {
    this.users = users;
    this.roles = roles;
  }

  public List<String> getUsers() {
    return users;
  }

  public void setUsers(List<String> user) {
    this.users = user;
  }

  public List<String> getRoles() {
    return roles;
  }

  public void setRoles(List<String> role) {
    this.roles = role;
  }

  @Override
  public String toString() {
    return "PermissionsQuery{" + "users=" + users + ", roles=" + roles + '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PermissionsQuery that = (PermissionsQuery) o;
    return Objects.equals(users, that.users) && Objects.equals(roles, that.roles);
  }

  @Override
  public int hashCode() {
    return Objects.hash(users, roles);
  }
}
