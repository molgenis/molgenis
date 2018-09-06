package org.molgenis.security.acl;

import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.jdbc.JdbcMutableAclService;
import org.springframework.security.acls.jdbc.LookupStrategy;
import org.springframework.security.acls.model.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/** {@link Transactional} {@link JdbcMutableAclService}. */
public class TransactionalJdbcMutableAclService extends JdbcMutableAclService {
  public TransactionalJdbcMutableAclService(
      DataSource dataSource, LookupStrategy lookupStrategy, AclCache aclCache) {
    super(dataSource, lookupStrategy, aclCache);
  }

  /**
   * Same as {@link JdbcMutableAclService#createAcl(ObjectIdentity)} except for duplicate key
   * checking which is handled by by the database for performance reasons.
   */
  @Transactional
  @Override
  public MutableAcl createAcl(ObjectIdentity objectIdentity) {
    Assert.notNull(objectIdentity, "Object Identity required");

    // Need to retrieve the current principal, in order to know who "owns" this ACL
    // (can be changed later on)
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    PrincipalSid sid = new PrincipalSid(auth);

    try {
      // Create the acl_object_identity row
      createObjectIdentity(objectIdentity, sid);
    } catch (DuplicateKeyException e) {
      throw new AlreadyExistsException("Object identity '" + objectIdentity + "' already exists");
    }
    // Retrieve the ACL via superclass (ensures cache registration, proper retrieval
    // etc)
    Acl acl = readAclById(objectIdentity);
    Assert.isInstanceOf(MutableAcl.class, acl, "MutableAcl should be been returned");

    return (MutableAcl) acl;
  }

  @Transactional
  @Override
  public void deleteAcl(ObjectIdentity objectIdentity, boolean deleteChildren) {
    super.deleteAcl(objectIdentity, deleteChildren);
  }

  @Transactional
  @Override
  public MutableAcl updateAcl(MutableAcl acl) {
    return super.updateAcl(acl);
  }

  @Transactional(readOnly = true)
  @Override
  public List<ObjectIdentity> findChildren(ObjectIdentity parentIdentity) {
    return super.findChildren(parentIdentity);
  }

  @Transactional(readOnly = true)
  @Override
  public Acl readAclById(ObjectIdentity object, List<Sid> sids) {
    return super.readAclById(object, sids);
  }

  @Transactional(readOnly = true)
  @Override
  public Acl readAclById(ObjectIdentity object) {
    return super.readAclById(object);
  }

  @Transactional(readOnly = true)
  @Override
  public Map<ObjectIdentity, Acl> readAclsById(List<ObjectIdentity> objects) {
    return super.readAclsById(objects);
  }

  @Transactional(readOnly = true)
  @Override
  public Map<ObjectIdentity, Acl> readAclsById(List<ObjectIdentity> objects, List<Sid> sids) {
    return super.readAclsById(objects, sids);
  }
}
