package org.molgenis.security.acl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.acls.jdbc.JdbcMutableAclService;
import org.springframework.security.acls.model.AclCache;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

import static java.util.Objects.requireNonNull;

/**
 * @see JdbcMutableAclService
 */
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
	private final Cache<String, Integer> aclClassCache;
	private static final Logger LOGGER = LoggerFactory.getLogger(MutableAclClassServiceImpl.class);

	public MutableAclClassServiceImpl(JdbcTemplate jdbcTemplate, AclCache aclCache)
	{
		this.jdbcTemplate = requireNonNull(jdbcTemplate);
		this.aclCache = requireNonNull(aclCache);
		this.aclClassCache = Caffeine.newBuilder().maximumSize(1000).build();
	}

	@Transactional
	@Override
	public void createAclClass(String type, Class<?> idType)
	{
		LOGGER.debug("Create AclClass for type {}.", type);
		jdbcTemplate.update(SQL_INSERT_INTO_ACL_CLASS, type, idType.getCanonicalName());
		aclClassCache.invalidate(type);
	}

	@Transactional
	@Override
	public void deleteAclClass(String type)
	{
		LOGGER.debug("Delete AclClass for type {}.", type);
		jdbcTemplate.update(SQL_DELETE_FROM_ACL_CLASS, type);
		aclClassCache.invalidate(type);
		aclCache.clearCache();
	}

	@Override
	public void clearCache()
	{
		LOGGER.debug("Invalidate cache.");
		aclClassCache.invalidateAll();
	}

	@SuppressWarnings("ConstantConditions")
	@Override
	public boolean hasAclClass(String type)
	{
		boolean result = aclClassCache.get(type,
				t -> jdbcTemplate.queryForObject(SQL_COUNT_ACL_CLASS, new Object[] { t }, Integer.class)) > 0;
		LOGGER.trace("hasAclClass({}): {}", type, result);
		return result;
	}

	@Override
	public Collection<String> getAclClassTypes()
	{
		return jdbcTemplate.queryForList(SQL_SELECT_ACL_CLASS, String.class);
	}
}
