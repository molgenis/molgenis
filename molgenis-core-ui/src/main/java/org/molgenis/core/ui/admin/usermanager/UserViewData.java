package org.molgenis.core.ui.admin.usermanager;

import java.util.Objects;
import org.molgenis.data.security.auth.User;

public class UserViewData {
  private final String id;
  private final String username;
  private String firstName;
  private String middleName;
  private String lastName;
  private String fullName; // first, middle, last name
  private Boolean active;
  private Boolean superuser;

  UserViewData(User mu) {
    this(mu.getId(), mu.getUsername());
    firstName = (null == mu.getFirstName() ? "" : mu.getFirstName());
    middleName = (null == mu.getMiddleNames() ? "" : mu.getMiddleNames());
    lastName = (null == mu.getLastName() ? "" : mu.getLastName());

    fullName = firstName + ' ' + middleName + ' ' + lastName;

    this.active = mu.isActive();
    this.superuser = mu.isSuperuser();
  }

  UserViewData(String id, final String username) {
    if (null == id) {
      throw new IllegalArgumentException("id is null");
    }
    if (null == username) {
      throw new IllegalArgumentException("username is null");
    }
    this.id = id;
    this.username = username;
  }

  public String getId() {
    return id;
  }

  public String getUsername() {
    return username;
  }

  public String getFullName() {
    return fullName;
  }

  public Boolean isActive() {
    return this.active;
  }

  public Boolean isSuperuser() {
    return this.superuser;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof UserViewData)) return false;
    UserViewData that = (UserViewData) o;
    return Objects.equals(getId(), that.getId())
        && Objects.equals(getUsername(), that.getUsername())
        && Objects.equals(firstName, that.firstName)
        && Objects.equals(middleName, that.middleName)
        && Objects.equals(lastName, that.lastName)
        && Objects.equals(getFullName(), that.getFullName())
        && Objects.equals(active, that.active)
        && Objects.equals(superuser, that.superuser);
  }

  @Override
  public int hashCode() {

    return Objects.hash(
        getId(), getUsername(), firstName, middleName, lastName, getFullName(), active, superuser);
  }
}
