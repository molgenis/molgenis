package org.molgenis.data.security;

import org.molgenis.data.security.auth.Group;
import org.springframework.security.acls.domain.ObjectIdentityImpl;

public class GroupIdentity extends ObjectIdentityImpl {
  public static final String GROUP = "group";

  public GroupIdentity(String groupName) {
    super(GROUP, groupName);
  }

  public GroupIdentity(Group group) {
    this(group.getName());
  }
}
