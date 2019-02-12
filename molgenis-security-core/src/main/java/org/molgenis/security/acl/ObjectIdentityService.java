package org.molgenis.security.acl;

import java.util.List;
import java.util.Set;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Sid;

public interface ObjectIdentityService {
  Integer getNrOfObjectIdentities(String classId);

  public List<ObjectIdentity> getObjectIdentities(String classId);

  Integer getNrOfObjectIdentities(String classId, Set<Sid> sids);

  public List<ObjectIdentity> getObjectIdentities(String classId, Integer limit, Integer offset);

  public List<ObjectIdentity> getObjectIdentities(
      String classId, Set<Sid> sids, Integer limit, Integer offset);

  public List<ObjectIdentity> getObjectIdentities(String classId, Set<Sid> sids);
}
