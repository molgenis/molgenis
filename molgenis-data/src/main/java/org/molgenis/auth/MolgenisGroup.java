package org.molgenis.auth;

import com.google.auto.value.AutoValue;

@AutoValue
public class MolgenisGroup {
    public static final String ENTITY_NAME = "MolgenisGroup";

    String id;
    String name;
    boolean active;
}
