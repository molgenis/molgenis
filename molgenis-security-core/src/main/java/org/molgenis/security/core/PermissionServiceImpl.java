package org.molgenis.security.core;

import static java.util.Objects.requireNonNull;

import java.util.Map;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Sid;
import org.springframework.stereotype.Component;

@Component
public class PermissionServiceImpl implements PermissionService {
  private MutableAclService mutableAclService;

  public PermissionServiceImpl(MutableAclService mutableAclService) {
    this.mutableAclService = requireNonNull(mutableAclService);
  }

  @Override
  public void grant(Map<ObjectIdentity, PermissionSet> objectPermissionMap, Sid sid) {
    objectPermissionMap.forEach(
        (objectIdentity, permissionSet) -> grant(objectIdentity, permissionSet, sid));
  }

  @Override
  public void grant(ObjectIdentity objectIdentity, PermissionSet permissionSet, Sid sid) {
    MutableAcl acl = (MutableAcl) mutableAclService.readAclById(objectIdentity);
    acl.insertAce(acl.getEntries().size(), permissionSet, sid, true);
    mutableAclService.updateAcl(acl);
  }
}
