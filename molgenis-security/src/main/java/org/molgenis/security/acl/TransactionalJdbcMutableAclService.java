package org.molgenis.security.acl;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.molgenis.security.core.SidUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.acls.jdbc.JdbcMutableAclService;
import org.springframework.security.acls.jdbc.LookupStrategy;
import org.springframework.security.acls.model.Acl;
import org.springframework.security.acls.model.AclCache;
import org.springframework.security.acls.model.AlreadyExistsException;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Sid;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/** {@link Transactional} {@link JdbcMutableAclService}. */
public class TransactionalJdbcMutableAclService extends JdbcMutableAclService {
  private final AclCache aclCache;

  public TransactionalJdbcMutableAclService(
      DataSource dataSource, LookupStrategy lookupStrategy, AclCache aclCache) {
    super(dataSource, lookupStrategy, aclCache);
    this.aclCache = requireNonNull(aclCache);
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
    Sid sid = SidUtils.createSecurityContextSid();

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

  /**
   * Same as {@link JdbcMutableAclService#updateAcl(MutableAcl)} except that it clears all cache as
   * a workaround for https://github.com/spring-projects/spring-security/issues/3330.
   */
  @Transactional
  @Override
  public MutableAcl updateAcl(MutableAcl acl) {
    Assert.notNull(acl.getId(), "Object Identity doesn't provide an identifier");

    // Delete this ACL's ACEs in the acl_entry table
    deleteEntries(retrieveObjectIdentityPrimaryKey(acl.getObjectIdentity()));

    // Create this ACL's ACEs in the acl_entry table
    createEntries(acl);

    // Change the mutable columns in acl_object_identity
    updateObjectIdentity(acl);

    // Clear all cache
    aclCache.clearCache();

    // Retrieve the ACL via superclass (ensures cache registration, proper retrieval
    // etc)
    return (MutableAcl) super.readAclById(acl.getObjectIdentity());
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
