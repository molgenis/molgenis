package org.molgenis.auth;

import com.google.auto.value.AutoValue;

@AutoValue
public class MolgenisGroupMember {
    public static final String ENTITY_NAME = "MolgenisGroupMember";

    String id;
    MolgenisUser molgenisUser;
    MolgenisGroup molgenisGroup;
}
