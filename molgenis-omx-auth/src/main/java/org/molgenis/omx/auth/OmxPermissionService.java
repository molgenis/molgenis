package org.molgenis.omx.auth;

import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.db.QueryRule.Operator;
import org.molgenis.framework.security.Login;
import org.molgenis.framework.server.MolgenisPermissionService;
import org.molgenis.omx.core.MolgenisEntity;
import org.springframework.beans.factory.annotation.Autowired;

public class OmxPermissionService implements MolgenisPermissionService
{
	private static final Logger logger = Logger.getLogger(OmxPermissionService.class);

	private static final String ENTITY_TYPE_ENTITY = "ENTITY";
	private static final String ENTITY_TYPE_PLUGIN = "PLUGIN";

	private final Database database;
	private final Login login;

	@Autowired
	public OmxPermissionService(Database database, Login login)
	{
		if (database == null) throw new IllegalArgumentException("database is null");
		if (login == null) throw new IllegalArgumentException("login is null");
		this.database = database;
		this.login = login;
	}

	@Override
	public boolean hasPermissionOnPlugin(String pluginName, Permission permission)
	{
		pluginName = pluginName + "Plugin";
		try
		{
			MolgenisEntity molgenisEntity = MolgenisEntity.findByNameType(database, pluginName, ENTITY_TYPE_PLUGIN);
			if (molgenisEntity == null) throw new RuntimeException(pluginName + " is not a plugin");
			return hasPermission(molgenisEntity, permission);
		}
		catch (DatabaseException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean hasPermissionOnEntity(String entityName, Permission permission)
	{
		try
		{
			MolgenisEntity molgenisEntity = MolgenisEntity.findByNameType(database, entityName, ENTITY_TYPE_ENTITY);
			if (molgenisEntity == null) throw new RuntimeException(entityName + " is not an entity");
			return hasPermission(molgenisEntity, permission);
		}
		catch (DatabaseException e)
		{
			throw new RuntimeException(e);
		}
	}

	private boolean hasPermission(MolgenisEntity molgenisEntity, Permission permission) throws DatabaseException
	{
		switch (permission)
		{
			case OWN:
				return hasPermission(molgenisEntity,
						Arrays.<Permission> asList(Permission.READ, Permission.WRITE, Permission.OWN));
			case READ:
				return hasPermission(molgenisEntity, Arrays.<Permission> asList(Permission.READ));
			case WRITE:
				return hasPermission(molgenisEntity, Arrays.<Permission> asList(Permission.READ, Permission.WRITE));
			default:
				throw new RuntimeException("unknown permission: " + permission);
		}
	}

	private boolean hasPermission(MolgenisEntity molgenisEntity, List<Permission> permissionTypes)
			throws DatabaseException
	{
		// get current logged in user
		Integer userAndRoleId = login.getUserId();
		if (userAndRoleId == null)
		{
			logger.warn("no logged in user or anonymous user");
			try
			{
				boolean loginOk = login.login(database, Login.USER_ANONYMOUS_NAME, Login.USER_ANONYMOUS_PASSWORD);
				if (!loginOk) throw new RuntimeException("failed to login as anonymous user");
				userAndRoleId = login.getUserId();
			}
			catch (Exception e)
			{
				throw new RuntimeException(e);
			}
		}

		// super user has full permissions
		MolgenisUser user = database.findById(MolgenisUser.class, userAndRoleId);
		if (user.getSuperuser()) return true;

		// get role permissions
		List<MolgenisPermission> permissions = database.find(MolgenisPermission.class, new QueryRule(
				MolgenisPermission.ROLE_, Operator.EQUALS, userAndRoleId), new QueryRule(MolgenisPermission.ENTITY,
				Operator.EQUALS, molgenisEntity));

		if (permissions != null)
		{
			for (MolgenisPermission permission : permissions)
			{
				String permissionStr = permission.getPermission();
				for (Permission permissionType : permissionTypes)
				{
					if (permissionType.toString().equalsIgnoreCase(permissionStr)) return true;
				}
			}
		}
		return false;
	}
}
