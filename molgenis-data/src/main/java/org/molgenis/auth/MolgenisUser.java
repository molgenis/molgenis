package org.molgenis.auth;

import com.google.auto.value.AutoValue;

@AutoValue
public class MolgenisUser {
    public static final String ENTITY_NAME = "MolgenisUser";

    String id;
    String username;
    String password_;
    String activationCode;
    boolean active;
    boolean superuser;
    String firstName;
    String middleNames;
    String lastName;
    String title;
    String affiliation;
    String department;
    String role;
    String address;
    String phone;
    String email;
    String fax;
    String tollFreePhone;
    String city;
    String country;
    boolean changePassword;
    String languageCode;
    String googleAccountId;
}
