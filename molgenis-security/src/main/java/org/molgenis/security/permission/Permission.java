package org.molgenis.security.permission;

public class Permission {
  private String type;
  private String role;

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getRole() {
    return role;
  }

  public void setRole(String role) {
    this.role = role;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((role == null) ? 0 : role.hashCode());
    result = prime * result + ((type == null) ? 0 : type.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    Permission other = (Permission) obj;
    if (role == null) {
      if (other.role != null) return false;
    } else if (!role.equals(other.role)) return false;
    if (type == null) {
      return other.type == null;
    } else return type.equals(other.type);
  }

  @Override
  public String toString() {
    return "Permission [type=" + type + ", role=" + role + "]";
  }
}
