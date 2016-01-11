package org.molgenis.auth;

public interface Authority extends org.molgenis.data.Entity
{
    public String getRole();
    public void setRole(String role);
}
