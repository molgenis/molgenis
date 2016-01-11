package org.molgenis.auth;

import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

import com.google.auto.value.AutoValue;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;

@AutoValue
public class MolgenisToken extends org.molgenis.data.support.AbstractEntity implements org.molgenis.data.Entity{

    public static final String ENTITY_NAME = "MolgenisToken";
    public static final String TOKEN = "token";

    String id;
    MolgenisUser molgenisUser;
    String token;
    Date expirationDate;
    Date creationDate;
    String description;

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

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public Object get(String name)
    {
        name = name.toLowerCase();
        if (name.equals("id"))
            return getId();
        if (name.equals("molgenisuser"))
            return getMolgenisUser();
        if (name.equals(TOKEN))
            return getToken();
        if (name.equals("expirationdate"))
            return getExpirationDate();
        if (name.equals("creationdate"))
            return getCreationDate();
        if (name.equals("description"))
            return getDescription();
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
        if( entity.getString("molgenistoken_id") != null) this.setId(entity.getString("molgenistoken_id"));
        else if( entity.getString("MolgenisToken_id") != null) this.setId(entity.getString("MolgenisToken_id"));
        //set MolgenisUser
        // query formal name, else lowercase name
        if( entity.getEntity("molgenisUser", org.molgenis.auth.MolgenisUser.class) != null) {
            this.setMolgenisUser(entity.getEntity("molgenisUser", org.molgenis.auth.MolgenisUser.class));
        }
        else if( entity.getEntity("molgenisuser", org.molgenis.auth.MolgenisUser.class) != null) {
            this.setMolgenisUser(entity.getEntity("molgenisuser", org.molgenis.auth.MolgenisUser.class));
        }
        else if( entity.getEntity("MolgenisToken_molgenisUser", org.molgenis.auth.MolgenisUser.class) != null) {
            this.setMolgenisUser(entity.getEntity("MolgenisToken_molgenisUser", org.molgenis.auth.MolgenisUser.class));
        }
        else if( entity.getEntity("molgenistoken_molgenisuser", org.molgenis.auth.MolgenisUser.class) != null) {
            this.setMolgenisUser(entity.getEntity("MolgenisToken_molgenisUser", org.molgenis.auth.MolgenisUser.class));
        }
        //set Token
        // query formal name, else lowercase name
        if(entity.getString("token") != null) this.setToken(entity.getString("token"));
        else if(entity.getString("token") != null) this.setToken(entity.getString("token"));
        else if(strict) this.setToken(entity.getString("token")); // setting null is not an option due to function overloading
        if( entity.getString("molgenistoken_token") != null) this.setToken(entity.getString("molgenistoken_token"));
        else if( entity.getString("MolgenisToken_token") != null) this.setToken(entity.getString("MolgenisToken_token"));
        //set ExpirationDate
        // query formal name, else lowercase name
        if(entity.getTimestamp("expirationdate") != null) this.setExpirationDate(entity.getTimestamp("expirationdate"));
        else if(entity.getTimestamp("expirationDate") != null) this.setExpirationDate(entity.getTimestamp("expirationDate"));
        else if(strict) this.setExpirationDate(entity.getTimestamp("expirationdate")); // setting null is not an option due to function overloading
        if( entity.getTimestamp("molgenistoken_expirationdate") != null) this.setExpirationDate(entity.getTimestamp("molgenistoken_expirationdate"));
        else if( entity.getTimestamp("MolgenisToken_expirationDate") != null) this.setExpirationDate(entity.getTimestamp("MolgenisToken_expirationDate"));
        //set CreationDate
        // query formal name, else lowercase name
        if(entity.getTimestamp("creationdate") != null) this.setCreationDate(entity.getTimestamp("creationdate"));
        else if(entity.getTimestamp("creationDate") != null) this.setCreationDate(entity.getTimestamp("creationDate"));
        else if(strict) this.setCreationDate(entity.getTimestamp("creationdate")); // setting null is not an option due to function overloading
        if( entity.getTimestamp("molgenistoken_creationdate") != null) this.setCreationDate(entity.getTimestamp("molgenistoken_creationdate"));
        else if( entity.getTimestamp("MolgenisToken_creationDate") != null) this.setCreationDate(entity.getTimestamp("MolgenisToken_creationDate"));
        //set Description
        // query formal name, else lowercase name
        if(entity.getString("description") != null) this.setDescription(entity.getString("description"));
        else if(entity.getString("description") != null) this.setDescription(entity.getString("description"));
        else if(strict) this.setDescription(entity.getString("description")); // setting null is not an option due to function overloading
        if( entity.getString("molgenistoken_description") != null) this.setDescription(entity.getString("molgenistoken_description"));
        else if( entity.getString("MolgenisToken_description") != null) this.setDescription(entity.getString("MolgenisToken_description"));
    }

    @Override
    public String toString()
    {
        return this.toString(false);
    }

    public String toString(boolean verbose)
    {
        StringBuilder sb = new StringBuilder("MolgenisToken(");
        sb.append("id='" + getId()+"' ");
        sb.append("molgenisUser='" + getMolgenisUser()+"' ");
        sb.append("token='" + getToken()+"' ");
        sb.append("expirationDate='" + (getExpirationDate() == null ? "" : new java.text.SimpleDateFormat("MMMM d, yyyy, HH:mm:ss", java.util.Locale.US).format(getExpirationDate()))+"' ");
        sb.append("creationDate='" + (getCreationDate() == null ? "" : new java.text.SimpleDateFormat("MMMM d, yyyy, HH:mm:ss", java.util.Locale.US).format(getCreationDate()))+"' ");
        sb.append("description='" + getDescription()+"'");
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
        for (AttributeMetaData attr : new MolgenisTokenMetaData().getAttributes())
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
        if("token".equalsIgnoreCase(attributeName)) {
            this.setToken((String)value);
            return;
        }
        if("expirationDate".equalsIgnoreCase(attributeName)) {
            this.setExpirationDate((java.util.Date)value);
            return;
        }
        if("creationDate".equalsIgnoreCase(attributeName)) {
            this.setCreationDate((java.util.Date)value);
            return;
        }
        if("description".equalsIgnoreCase(attributeName)) {
            this.setDescription((String)value);
            return;
        }
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        MolgenisToken other = (MolgenisToken) obj;
        if (token == null)
        {
            if (other.token != null) return false;
        }
        else if (!token.equals(other.token)) return false;
        return true;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((token == null) ? 0 : token.hashCode());
        return result;
    }

    @Override
    public org.molgenis.data.EntityMetaData getEntityMetaData()
    {
        return new MolgenisTokenMetaData();
    }
}
