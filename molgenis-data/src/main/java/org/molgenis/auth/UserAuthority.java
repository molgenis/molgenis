package org.molgenis.auth;

public class UserAuthority extends Authority{
    public static final String ENTITY_NAME = "UserAuthority";

    String id;
    MolgenisUser molgenisUser;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public MolgenisUser getMolgenisUser() {
        return molgenisUser;
    }

    public void setMolgenisUser(MolgenisUser molgenisUser) {
        this.molgenisUser = molgenisUser;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
