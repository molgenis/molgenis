package org.molgenis.omx.auth;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.db.QueryRule.Operator;
import org.molgenis.framework.security.Login;
import org.molgenis.framework.server.MolgenisPermissionService;
import org.molgenis.framework.ui.MolgenisPlugin;
import org.molgenis.omx.core.MolgenisEntity;
import org.molgenis.util.Entity;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Permission service based on the OMX model
 */
public class OmxPermissionService implements MolgenisPermissionService
{
	private static final Logger logger = Logger.getLogger(OmxPermissionService.class);

	private enum EntityType
	{
		ENTITY, PLUGIN;
	};

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
		pluginName = pluginName + "Plugin"; // TODO remove line after removing molgenis UI framework
		try
		{
			MolgenisEntity molgenisEntity = MolgenisEntity.findByNameType(database, pluginName,
					EntityType.PLUGIN.toString());
			if (molgenisEntity == null) throw new RuntimeException(pluginName + " is not a plugin");
			return hasPermission(molgenisEntity, permission);
		}
		catch (DatabaseException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean hasPermissionOnPlugin(Class<? extends MolgenisPlugin> pluginClazz, Permission permission)
	{
		try
		{
			MolgenisEntity molgenisEntity = MolgenisEntity.findByClassName(database, pluginClazz.getName());
			if (molgenisEntity == null) throw new RuntimeException(pluginClazz.getName() + " is not a plugin");
			return hasPermission(molgenisEntity, permission);
		}
		catch (DatabaseException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setPermissionOnPlugin(String pluginName, Integer roleId, Permission permission)
	{
		setPermission(pluginName, EntityType.PLUGIN, permission, roleId);
	}

	@Override
	public void setPermissionOnPlugin(Class<? extends MolgenisPlugin> pluginClazz, Integer roleId, Permission permission)
	{
		setPermission(pluginClazz.getSimpleName(), EntityType.PLUGIN, permission, roleId);
	}

	@Override
	public boolean hasPermissionOnEntity(String entityName, Permission permission)
	{
		try
		{
			MolgenisEntity molgenisEntity = MolgenisEntity.findByNameType(database, entityName,
					EntityType.ENTITY.toString());
			if (molgenisEntity == null) throw new RuntimeException(entityName + " is not an entity");
			return hasPermission(molgenisEntity, permission);
		}
		catch (DatabaseException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean hasPermissionOnEntity(Class<? extends Entity> entityClazz, Permission permission)
	{
		try
		{
			MolgenisEntity molgenisEntity = MolgenisEntity.findByClassName(database, entityClazz.getName());
			if (molgenisEntity == null) throw new RuntimeException(entityClazz.getName() + " is not an entity");
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

	@Override
	public void setPermissionOnEntity(String entityName, Integer roleId, Permission permission)
	{
		setPermission(entityName, EntityType.ENTITY, permission, roleId);
	}

	@Override
	public void setPermissionOnEntity(Class<? extends Entity> entityClazz, Integer roleId, Permission permission)
	{
		setPermission(entityClazz.getSimpleName(), EntityType.ENTITY, permission, roleId);
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

	private void setPermission(String pluginOrEntityName, EntityType entityType, Permission permission, Integer roleId)
	{
		MolgenisEntity entity;
		MolgenisRole molgenisRole;
		try
		{
			entity = MolgenisEntity.findByNameType(database, pluginOrEntityName, entityType.toString());
			if (entity == null) throw new RuntimeException(pluginOrEntityName + " is not a "
					+ entityType.toString().toLowerCase());
			molgenisRole = MolgenisRole.findById(database, roleId);
			if (molgenisRole == null) throw new RuntimeException("unknown role id: " + roleId);
			database.beginTx();
		}
		catch (DatabaseException e)
		{
			throw new RuntimeException(e);
		}
		try
		{
			MolgenisPermission molgenisPermission = new MolgenisPermission();
			molgenisPermission.setIdentifier(UUID.randomUUID().toString());
			molgenisPermission.setName(molgenisRole.getName() + "_" + pluginOrEntityName + "_Permission");
			molgenisPermission.setEntity(entity);
			molgenisPermission.setRole(molgenisRole);
			// TODO remove toLowerCase after removing molgenis UI framework
			molgenisPermission.setPermission(permission.toString().toLowerCase());
			database.add(molgenisPermission);
			database.commitTx();
		}
		catch (Throwable t)
		{
			try
			{
				database.rollbackTx();
			}
			catch (DatabaseException e)
			{
			}
			throw new RuntimeException(t);
		}
	}
}
