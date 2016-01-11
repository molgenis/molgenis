package org.molgenis.auth;

import com.google.auto.value.AutoValue;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;

import java.util.LinkedHashSet;
import java.util.Set;

@AutoValue
public class MolgenisGroupMember  extends org.molgenis.data.support.AbstractEntity implements org.molgenis.data.Entity{
    public static final String ENTITY_NAME = "MolgenisGroupMember";
    public static final String MOLGENISUSER = "molgenisuser";
    public static final String MOLGENISGROUP = "molgenisgroup";

    String id;
    MolgenisUser molgenisUser;
    MolgenisGroup molgenisGroup;

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

    public MolgenisGroup getMolgenisGroup() {
        return molgenisGroup;
    }

    public void setMolgenisGroup(MolgenisGroup molgenisGroup) {
        this.molgenisGroup = molgenisGroup;
    }

    @Override
    public Object get(String name)
    {
        name = name.toLowerCase();
        if (name.equals("id"))
            return getId();
        if (name.equals(MOLGENISUSER))
            return getMolgenisUser();
        if (name.equals(MOLGENISGROUP))
            return getMolgenisGroup();
        return null;
    }

    @Override
    public void set(Entity entity)
    {
        set(entity, true);
    }

    public void set(org.molgenis.data.Entity entity, boolean strict)
    {
        //set Id
        // query formal name, else lowercase name
        if(entity.getString("id") != null) this.setId(entity.getString("id"));
        else if(entity.getString("id") != null) this.setId(entity.getString("id"));
        else if(strict) this.setId(entity.getString("id")); // setting null is not an option due to function overloading
        if( entity.getString("molgenisgroupmember_id") != null) this.setId(entity.getString("molgenisgroupmember_id"));
        else if( entity.getString("MolgenisGroupMember_id") != null) this.setId(entity.getString("MolgenisGroupMember_id"));
        //set MolgenisUser
        // query formal name, else lowercase name
        if( entity.getEntity("molgenisUser", org.molgenis.auth.MolgenisUser.class) != null) {
            this.setMolgenisUser(entity.getEntity("molgenisUser", org.molgenis.auth.MolgenisUser.class));
        }
        else if( entity.getEntity("molgenisuser", org.molgenis.auth.MolgenisUser.class) != null) {
            this.setMolgenisUser(entity.getEntity("molgenisuser", org.molgenis.auth.MolgenisUser.class));
        }
        else if( entity.getEntity("MolgenisGroupMember_molgenisUser", org.molgenis.auth.MolgenisUser.class) != null) {
            this.setMolgenisUser(entity.getEntity("MolgenisGroupMember_molgenisUser", org.molgenis.auth.MolgenisUser.class));
        }
        else if( entity.getEntity("molgenisgroupmember_molgenisuser", org.molgenis.auth.MolgenisUser.class) != null) {
            this.setMolgenisUser(entity.getEntity("MolgenisGroupMember_molgenisUser", org.molgenis.auth.MolgenisUser.class));
        }
        //set MolgenisGroup
        // query formal name, else lowercase name
        if( entity.getEntity("molgenisGroup", org.molgenis.auth.MolgenisGroup.class) != null) {
            this.setMolgenisGroup(entity.getEntity("molgenisGroup", org.molgenis.auth.MolgenisGroup.class));
        }
        else if( entity.getEntity("molgenisgroup", org.molgenis.auth.MolgenisGroup.class) != null) {
            this.setMolgenisGroup(entity.getEntity("molgenisgroup", org.molgenis.auth.MolgenisGroup.class));
        }
        else if( entity.getEntity("MolgenisGroupMember_molgenisGroup", org.molgenis.auth.MolgenisGroup.class) != null) {
            this.setMolgenisGroup(entity.getEntity("MolgenisGroupMember_molgenisGroup", org.molgenis.auth.MolgenisGroup.class));
        }
        else if( entity.getEntity("molgenisgroupmember_molgenisgroup", org.molgenis.auth.MolgenisGroup.class) != null) {
            this.setMolgenisGroup(entity.getEntity("MolgenisGroupMember_molgenisGroup", org.molgenis.auth.MolgenisGroup.class));
        }
    }

    @Override
    public String toString()
    {
        return this.toString(false);
    }

    public String toString(boolean verbose)
    {
        StringBuilder sb = new StringBuilder("MolgenisGroupMember(");
        sb.append("id='" + getId()+"' ");
        sb.append("molgenisUser='" + getMolgenisUser()+"' ");
        sb.append("molgenisGroup='" + getMolgenisGroup()+"'");
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
        for (AttributeMetaData attr : new MolgenisGroupMemberMetaData().getAttributes())
        {
            attributeNames.add(attr.getName());
        }

        return attributeNames;
    }

    @Override
    public void set(String attributeName, Object value)
    {
        if("id".equalsIgnoreCase(attributeName)) {
            this.setId((String)value);
            return;
        }
        if("molgenisUser".equalsIgnoreCase(attributeName)) {
            org.molgenis.auth.MolgenisUser e = new org.molgenis.auth.MolgenisUser();
            e.set((Entity)value);
            this.setMolgenisUser(e);
            return;
        }
        if("molgenisGroup".equalsIgnoreCase(attributeName)) {
            org.molgenis.auth.MolgenisGroup e = new org.molgenis.auth.MolgenisGroup();
            e.set((Entity)value);
            this.setMolgenisGroup(e);
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
        return new MolgenisGroupMemberMetaData();
    }
}
