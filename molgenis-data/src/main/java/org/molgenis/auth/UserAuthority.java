package org.molgenis.auth;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;

import java.util.LinkedHashSet;
import java.util.Set;

public class UserAuthority extends org.molgenis.data.support.AbstractEntity implements org.molgenis.data.Entity, org.molgenis.auth.Authority{
    public static final String ENTITY_NAME = "UserAuthority";
    public static final String MOLGENISUSER = "molgenisUser";
    public static final String ROLE = "role";

    String id;
    MolgenisUser molgenisUser;
    String role;

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

    @Override
    public Object get(String name)
    {
        if (name.equals(ROLE))
            return getRole();
        if (name.equals("id"))
            return getId();
        if (name.equals(MOLGENISUSER))
            return getMolgenisUser();
        return null;
    }

    @Override
    public void set(Entity entity)
    {
        set(entity, true);
    }

    public void set(org.molgenis.data.Entity entity, boolean strict)
    {
        //set Role
        // query formal name, else lowercase name
        if(entity.getString("role") != null) this.setRole(entity.getString("role"));
        else if(entity.getString("role") != null) this.setRole(entity.getString("role"));
        else if(strict) this.setRole(entity.getString("role")); // setting null is not an option due to function overloading
        if( entity.getString("userauthority_role") != null) this.setRole(entity.getString("userauthority_role"));
        else if( entity.getString("UserAuthority_role") != null) this.setRole(entity.getString("UserAuthority_role"));
        //set Id
        // query formal name, else lowercase name
        if(entity.getString("id") != null) this.setId(entity.getString("id"));
        else if(entity.getString("id") != null) this.setId(entity.getString("id"));
        else if(strict) this.setId(entity.getString("id")); // setting null is not an option due to function overloading
        if( entity.getString("userauthority_id") != null) this.setId(entity.getString("userauthority_id"));
        else if( entity.getString("UserAuthority_id") != null) this.setId(entity.getString("UserAuthority_id"));
        //set MolgenisUser
        // query formal name, else lowercase name
        if( entity.getEntity("molgenisUser", org.molgenis.auth.MolgenisUser.class) != null) {
            this.setMolgenisUser(entity.getEntity("molgenisUser", org.molgenis.auth.MolgenisUser.class));
        }
        else if( entity.getEntity("molgenisuser", org.molgenis.auth.MolgenisUser.class) != null) {
            this.setMolgenisUser(entity.getEntity("molgenisuser", org.molgenis.auth.MolgenisUser.class));
        }
        else if( entity.getEntity("UserAuthority_molgenisUser", org.molgenis.auth.MolgenisUser.class) != null) {
            this.setMolgenisUser(entity.getEntity("UserAuthority_molgenisUser", org.molgenis.auth.MolgenisUser.class));
        }
        else if( entity.getEntity("userauthority_molgenisuser", org.molgenis.auth.MolgenisUser.class) != null) {
            this.setMolgenisUser(entity.getEntity("UserAuthority_molgenisUser", org.molgenis.auth.MolgenisUser.class));
        }
    }

    @Override
    public String toString()
    {
        return this.toString(false);
    }

    public String toString(boolean verbose)
    {
        StringBuilder sb = new StringBuilder("UserAuthority(");
        sb.append("role='" + getRole()+"' ");
        sb.append("id='" + getId()+"' ");
        sb.append("molgenisUser='" + getMolgenisUser()+"'");
        sb.append(");");
        return sb.toString();
    }

    @Override
    public String getIdValue()
    {
        return getId();
    }


    @Override
    public Iterable<String> getAttributeNames()
    {
        Set<String> attributeNames = new LinkedHashSet<String>();
        for (AttributeMetaData attr : new UserAuthorityMetaData().getAttributes())
        {
            attributeNames.add(attr.getName());
        }

        return attributeNames;
    }

    @Override
    public void set(String attributeName, Object value)
    {
        if("role".equals(attributeName)) {
            this.setRole((String)value);
            return;
        }
        if("id".equals(attributeName)) {
            this.setId((String)value);
            return;
        }
        if("molgenisUser".equals(attributeName)) {
            org.molgenis.auth.MolgenisUser e = new org.molgenis.auth.MolgenisUser();
            e.set((Entity)value);
            this.setMolgenisUser(e);
            return;
        }
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        return true;
    }

    @Override
    public int hashCode()
    {
        int result = 1;
        return result;
    }

    @Override
    public org.molgenis.data.EntityMetaData getEntityMetaData()
    {
        return new UserAuthorityMetaData();
    }
}
