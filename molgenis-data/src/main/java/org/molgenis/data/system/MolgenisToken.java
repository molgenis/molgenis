package org.molgenis.data.system;

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
}
