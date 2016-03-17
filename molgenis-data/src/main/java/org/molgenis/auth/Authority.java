package org.molgenis.auth;

public interface Authority extends org.molgenis.data.Entity
{

	String ROLE = "role";

	public String getRole();

	public void setRole(String role);
}
