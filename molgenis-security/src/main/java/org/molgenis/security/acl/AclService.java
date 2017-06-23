package org.molgenis.security.acl;

import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.jdbc.JdbcMutableAclService;
import org.springframework.security.acls.jdbc.LookupStrategy;
import org.springframework.security.acls.model.AclCache;
import org.springframework.security.acls.model.ObjectIdentity;

import javax.sql.DataSource;
import java.util.List;

/**
 * Acl service for object identities with String identifiers.
 *
 * @see JdbcMutableAclService
 */
public class AclService extends JdbcMutableAclService
{
	private static final String DEFAULT_SELECT_ACL_WITH_PARENT_SQL =
			"select obj.object_id_identity as obj_id, class.class as class "
					+ "from acl_object_identity obj, acl_object_identity parent, acl_class class "
					+ "where obj.parent_object = parent.id and obj.object_id_class = class.id "
					+ "and parent.object_id_identity = ? and parent.object_id_class = ("
					+ "select id FROM acl_class where acl_class.class = ?)";

	private String findChildrenSql = DEFAULT_SELECT_ACL_WITH_PARENT_SQL;

	public AclService(DataSource dataSource, LookupStrategy lookupStrategy, AclCache aclCache)
	{
		super(dataSource, lookupStrategy, aclCache);
	}

	@Override
	public List<ObjectIdentity> findChildren(ObjectIdentity parentIdentity)
	{
		Object[] args = { parentIdentity.getIdentifier(), parentIdentity.getType() };
		List<ObjectIdentity> objects = jdbcTemplate.query(findChildrenSql, args, (rs, rowNum) ->
		{
			String javaType = rs.getString("class");
			String identifier = rs.getString("obj_id");
			return new ObjectIdentityImpl(javaType, identifier);
		});

		if (objects.size() == 0)
		{
			return null;
		}

		return objects;
	}

	@Override
	public void setFindChildrenQuery(String findChildrenSql)
	{
		this.findChildrenSql = findChildrenSql;
		super.setFindChildrenQuery(findChildrenSql);
	}
}
