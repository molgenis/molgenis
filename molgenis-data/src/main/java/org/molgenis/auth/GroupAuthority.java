package org.molgenis.auth;

public class GroupAuthority extends Authority{
    public static final String ENTITY_NAME = "GroupAuthority";

    String id;
    MolgenisGroup molgenisGroup;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public MolgenisGroup getMolgenisUser() {
        return molgenisGroup;
    }

    public void setMolgenisUser(MolgenisGroup molgenisGroup) {
        this.molgenisGroup = molgenisGroup;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
