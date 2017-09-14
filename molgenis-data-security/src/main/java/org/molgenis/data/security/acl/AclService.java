package org.molgenis.data.security.acl;

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
	private static final String CLASS_EXISTS_PRIMARY_KEY_SQL = "select exists(select id from acl_class where class=?)";
	private static final String SELECT_CLASS_PRIMARY_KEY_SQL = "select id from acl_class where class=?";
	private static final String INSERT_CLASS_SQL = "INSERT INTO acl_class (class) values ('%s') ON CONFLICT DO NOTHING";
	private static final String DELETE_CLASS_SQL = "DELETE FROM acl_class WHERE class='%s'";

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
		setClassPrimaryKeyQuery(SELECT_CLASS_PRIMARY_KEY_SQL);
	}

	public void createClass(String type)
	{
		jdbcTemplate.execute(String.format(INSERT_CLASS_SQL, type));
	}

	public void deleteClass(String type)
	{
		jdbcTemplate.execute(String.format(DELETE_CLASS_SQL, type));
	}

	public boolean hasClass(String classId)
	{
		Boolean exists = jdbcTemplate.queryForObject(CLASS_EXISTS_PRIMARY_KEY_SQL, Boolean.class, classId);
		return exists != null ? exists : false;
	}

	public String getInheritanceAttribute(String classId)
	{
		return null;
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
