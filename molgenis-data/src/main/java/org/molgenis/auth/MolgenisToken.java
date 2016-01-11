package org.molgenis.auth;

import java.util.Date;

import com.google.auto.value.AutoValue;
import org.molgenis.auth.MolgenisUser;

@AutoValue
public class MolgenisToken {
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


}
