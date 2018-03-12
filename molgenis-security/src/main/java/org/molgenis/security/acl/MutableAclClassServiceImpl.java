package org.molgenis.security.acl;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.acls.jdbc.JdbcMutableAclService;
import org.springframework.security.acls.model.AclCache;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

import static java.util.Objects.requireNonNull;

/**
 * @see JdbcMutableAclService
 */
@Component
public class MutableAclClassServiceImpl implements MutableAclClassService
{
	/**
	 * @see JdbcMutableAclService#DEFAULT_INSERT_INTO_ACL_CLASS_WITH_ID
	 */
	private static final String SQL_INSERT_INTO_ACL_CLASS = "insert into acl_class (class, class_id_type) values (?, ?)";
	private static final String SQL_DELETE_FROM_ACL_CLASS = "delete from acl_class where class=?";
	private static final String SQL_COUNT_ACL_CLASS = "select count(*) from acl_class WHERE class = ?";
	private static final String SQL_SELECT_ACL_CLASS = "select class from acl_class";

	private final JdbcTemplate jdbcTemplate;
	private final AclCache aclCache;

	MutableAclClassServiceImpl(JdbcTemplate jdbcTemplate, AclCache aclCache)
	{
		this.jdbcTemplate = requireNonNull(jdbcTemplate);
		this.aclCache = requireNonNull(aclCache);
	}

	@Transactional
	@Override
	public void createAclClass(String type, Class<?> idType)
	{
		jdbcTemplate.update(SQL_INSERT_INTO_ACL_CLASS, type, idType.getCanonicalName());
	}

	@Transactional
	@Override
	public void deleteAclClass(String type)
	{
		jdbcTemplate.update(SQL_DELETE_FROM_ACL_CLASS, type);
		aclCache.clearCache();
	}

	@SuppressWarnings("ConstantConditions")
	@Transactional(readOnly = true)
	@Override
	public boolean hasAclClass(String type)
	{
		return jdbcTemplate.queryForObject(SQL_COUNT_ACL_CLASS, new Object[] { type }, Integer.class) > 0;
	}

	@Transactional(readOnly = true)
	@Override
	public Collection<String> getAclClassTypes()
	{
		return jdbcTemplate.queryForList(SQL_SELECT_ACL_CLASS, String.class);
	}
}
