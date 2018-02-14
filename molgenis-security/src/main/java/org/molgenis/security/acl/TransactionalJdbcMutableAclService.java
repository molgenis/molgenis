package org.molgenis.security.acl;

import org.springframework.security.acls.jdbc.JdbcMutableAclService;
import org.springframework.security.acls.jdbc.LookupStrategy;
import org.springframework.security.acls.model.*;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

/**
 * {@link Transactional} {@link JdbcMutableAclService}.
 */
public class TransactionalJdbcMutableAclService extends JdbcMutableAclService
{
	public TransactionalJdbcMutableAclService(DataSource dataSource, LookupStrategy lookupStrategy, AclCache aclCache)
	{
		super(dataSource, lookupStrategy, aclCache);
	}

	@Transactional
	@Override
	public MutableAcl createAcl(ObjectIdentity objectIdentity)
	{
		return super.createAcl(objectIdentity);
	}

	@Transactional
	@Override
	public void deleteAcl(ObjectIdentity objectIdentity, boolean deleteChildren)
	{
		super.deleteAcl(objectIdentity, deleteChildren);
	}

	@Transactional
	@Override
	public MutableAcl updateAcl(MutableAcl acl)
	{
		return super.updateAcl(acl);
	}

	@Transactional(readOnly = true)
	@Override
	public List<ObjectIdentity> findChildren(ObjectIdentity parentIdentity)
	{
		return super.findChildren(parentIdentity);
	}

	@Transactional(readOnly = true)
	@Override
	public Acl readAclById(ObjectIdentity object, List<Sid> sids)
	{
		return super.readAclById(object, sids);
	}

	@Transactional(readOnly = true)
	@Override
	public Acl readAclById(ObjectIdentity object)
	{
		return super.readAclById(object);
	}

	@Transactional(readOnly = true)
	@Override
	public Map<ObjectIdentity, Acl> readAclsById(List<ObjectIdentity> objects)
	{
		return super.readAclsById(objects);
	}

	@Transactional(readOnly = true)
	@Override
	public Map<ObjectIdentity, Acl> readAclsById(List<ObjectIdentity> objects, List<Sid> sids)
	{
		return super.readAclsById(objects, sids);
	}
}
