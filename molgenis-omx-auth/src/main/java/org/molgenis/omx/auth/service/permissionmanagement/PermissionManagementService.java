/**
 * @author Jessica Lundberg
 * @date 04-02-2011
 * 
 * A service class for database interaction with PermissionManagementPlugin, 
 * which allows users to see their rights and also the rights of others on entities
 * they own. 
 */
package org.molgenis.omx.auth.service.permissionmanagement;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.Query;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.db.QueryRule.Operator;
import org.molgenis.omx.auth.MolgenisPermission;
import org.molgenis.omx.auth.MolgenisRole;
import org.molgenis.omx.auth.MolgenisUser;
import org.molgenis.omx.core.MolgenisEntity;

public class PermissionManagementService
{

	private Database db = null;
	private static PermissionManagementService permissionManagementService = null;

	public void setDb(Database db)
	{
		this.db = db;
	}

	// private constructor, use singleton instance
	private PermissionManagementService()
	{
	}

	/**
	 * Get an instance of PermissionManagementService
	 * 
	 * @param Database
	 *            object
	 * @return PermissionManagementService object
	 */
	public static PermissionManagementService getInstance()
	{
		if (permissionManagementService == null)
		{
			permissionManagementService = new PermissionManagementService();
		}

		return permissionManagementService;
	}

	/**
	 * Given a role id, finds all MolgenisPermissions for the role.
	 * 
	 * @param id
	 *            MolgenisRole id
	 * @return list of MolgenisPermissions for the given Role
	 * @throws DatabaseException
	 * @throws ParseException
	 */
	public List<MolgenisPermission> findPermissions(Integer id) throws DatabaseException, ParseException
	{
		Query<MolgenisPermission> q = db.query(MolgenisPermission.class);
		q.addRules(new QueryRule(MolgenisPermission.ROLE_, Operator.EQUALS, id));
		return q.find();
	}

	/**
	 * Given a role id and an entity id, searches for a MolgenisPermission on a
	 * particular entity for the given role.
	 * 
	 * @param roleId
	 *            role Id
	 * @param entityId
	 *            entity Id
	 * @return MolgenisPermission if one exists, otherwise null
	 * @throws DatabaseException
	 * @throws ParseException
	 */
	public MolgenisPermission findPermissions(Integer roleId, Integer entityId) throws DatabaseException,
			ParseException
	{
		Query<MolgenisPermission> q = db.query(MolgenisPermission.class);
		q.addRules(new QueryRule(MolgenisPermission.ROLE_, Operator.EQUALS, roleId));
		q.addRules(new QueryRule(MolgenisPermission.ENTITY, Operator.EQUALS, entityId));
		if (q.find() != null)
		{
			return q.find().get(0);
		}
		else
		{
			return null;
		}
	}

	/**
	 * Given a role id, entity id, and a permission type, check to see whether a
	 * MolgenisPermission exists
	 * 
	 * @param roleId
	 *            the role id
	 * @param entityId
	 *            the entity id
	 * @param permType
	 *            the type of permission (i.e. READ, WRITE, EXECUTE, or OWN)
	 * @return A MolgenisPermission if one exists, otherwise null
	 * @throws DatabaseException
	 * @throws ParseException
	 */
	public MolgenisPermission findPermissions(Integer roleId, Integer entityId, String permType)
			throws DatabaseException, ParseException
	{
		Query<MolgenisPermission> q = db.query(MolgenisPermission.class);
		q.addRules(new QueryRule(MolgenisPermission.ROLE_, Operator.EQUALS, roleId));
		q.addRules(new QueryRule(MolgenisPermission.ENTITY, Operator.EQUALS, entityId));
		q.addRules(new QueryRule(MolgenisPermission.PERMISSION, Operator.EQUALS, permType));

		if (!q.find().isEmpty())
		{
			return q.find().get(0);
		}
		else
		{
			return null;
		}
	}

	/**
	 * Given a role id and a permission type (i.e. READ, WRITE, EXECUTE, or
	 * OWN), return a list of MolgenisPermissions matching these parameters.
	 * 
	 * @param id
	 * @param permType
	 * @return
	 * @throws DatabaseException
	 * @throws ParseException
	 */
	public List<MolgenisPermission> findPermissions(Integer roleId, String permType) throws DatabaseException,
			ParseException
	{
		Query<MolgenisPermission> q = db.query(MolgenisPermission.class);
		q.addRules(new QueryRule(MolgenisPermission.ROLE_, Operator.EQUALS, roleId));
		q.addRules(new QueryRule(MolgenisPermission.PERMISSION, Operator.EQUALS, permType));
		return q.find();
	}

	/**
	 * Given a role id, return a list of MolgenisPermissions for all users,
	 * where the given roleId owns the entity in the matched
	 * MolgenisPermissions.
	 * 
	 * @param roleId
	 *            role id
	 * @param includeOwnership
	 *            whether to include the permission for ownership from the given
	 *            role
	 * @return List<MolgenisPermission> all matching Permissions
	 * @throws ParseException
	 * @throws DatabaseException
	 */
	public List<MolgenisPermission> findUserPermissions(Integer roleId, boolean includeOwnership)
			throws DatabaseException, ParseException
	{
		List<MolgenisPermission> entities = new ArrayList<MolgenisPermission>();
		List<MolgenisPermission> perms = findPermissions(roleId, "own");

		for (MolgenisPermission m : perms)
		{
			Query<MolgenisPermission> q = db.query(MolgenisPermission.class);
			q.addRules(new QueryRule(MolgenisPermission.ENTITY, Operator.EQUALS, m.getEntity_Id()));
			if (!includeOwnership)
			{
				q.addRules(new QueryRule(MolgenisPermission.PERMISSION, Operator.NOT, "own"));
			}
			entities.addAll(q.find());
		}

		return entities;
	}

	/**
	 * Given a role id and an entity id, return a list of MolgenisPermissions
	 * for all users, where the given roleId owns the entity in the matched
	 * MolgenisPermissions (and only the entity passed in).
	 * 
	 * @param roleId
	 *            role id
	 * @param entityId
	 *            entity id
	 * @return List<MolgenisPermission> matching permissions
	 * @throws ParseException
	 * @throws DatabaseException
	 */
	public List<MolgenisPermission> findUserPermissions(Integer roleId, Integer entityId) throws DatabaseException,
			ParseException
	{

		MolgenisPermission perm = findPermissions(roleId, entityId, "own");

		if (perm != null)
		{
			Query<MolgenisPermission> q = db.query(MolgenisPermission.class);
			q.addRules(new QueryRule(MolgenisPermission.ENTITY, Operator.EQUALS, perm.getEntity_Id()));
			return q.find();
		}
		return new ArrayList<MolgenisPermission>();
	}

	/**
	 * Finds permission based on a MolgenisPermission id
	 * 
	 * @param id
	 * @return
	 * @throws DatabaseException
	 * @throws ParseException
	 */
	public MolgenisPermission findPermission(Integer id) throws DatabaseException, ParseException
	{
		Query<MolgenisPermission> q = db.query(MolgenisPermission.class);
		q.addRules(new QueryRule(MolgenisPermission.ID, Operator.EQUALS, id));
		if (!q.find().isEmpty())
		{
			return q.find().get(0);
		}
		else
		{
			return null;
		}
	}

	/**
	 * Updates a given MolgenisPermission iff the given user is owner on that
	 * entity
	 * 
	 * @param userId
	 *            userId of user trying to update
	 * @param mp
	 *            MolgenisPermission to update
	 * @throws ParseException
	 * @throws DatabaseException
	 * @throws IOException
	 * 
	 */
	public void update(Integer userId, MolgenisPermission mp) throws DatabaseException, ParseException, IOException
	{

		MolgenisPermission perm = findPermissions(userId, mp.getEntity_Id(), "own");
		MolgenisUser user = (MolgenisUser) findRole(userId);
		if (perm != null || user.getSuperuser())
		{
			db.update(mp);
		}
		else
		{
			throw new DatabaseException("Sorry, you have insufficient rights to update this permission");
		}
	}

	/**
	 * Inserts a given MolgenisPermission iff the given user is owner on the
	 * entity for whom a permission is being made.
	 * 
	 * Note: Does not currently check for superusers
	 * 
	 * @param userId
	 *            userId of user trying to insert
	 * @param mp
	 *            MolgenisPermission to insert
	 * @throws ParseException
	 * @throws DatabaseException
	 * @throws IOException
	 * 
	 */
	public void insert(Integer userId, MolgenisPermission mp) throws DatabaseException, ParseException, IOException
	{
		if (exists(mp))
		{
			throw new DatabaseException("This permission already exists");
		}

		MolgenisPermission perm = findPermissions(userId, mp.getEntity_Id(), "own");
		MolgenisUser user = (MolgenisUser) findRole(userId);
		if (perm != null || user.getSuperuser())
		{
			db.add(mp);
		}
		else
		{
			throw new DatabaseException("Sorry, you have insufficient rights to add this permission");
		}
	}

	/**
	 * Check to see if a given permission exists
	 * 
	 * @param mp
	 * @return
	 * @throws DatabaseException
	 * @throws ParseException
	 */
	public boolean exists(MolgenisPermission mp) throws DatabaseException, ParseException
	{
		Query<MolgenisPermission> q = db.query(MolgenisPermission.class);
		q.addRules(new QueryRule(MolgenisPermission.ENTITY, Operator.EQUALS, mp.getEntity_Id()));
		q.addRules(new QueryRule(MolgenisPermission.PERMISSION, Operator.EQUALS, mp.getPermission()));
		q.addRules(new QueryRule(MolgenisPermission.ROLE_, Operator.EQUALS, mp.getRole_Id()));

		if (!q.find().isEmpty())
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	/**
	 * Find a MolgenisUser
	 * 
	 * @param id
	 *            UserId
	 * @return MolgenisUser
	 * @throws DatabaseException
	 * @throws ParseException
	 */
	public MolgenisRole findRole(Integer id) throws DatabaseException, ParseException
	{
		Query<MolgenisRole> q = db.query(MolgenisRole.class);
		q.addRules(new QueryRule(MolgenisRole.ID, Operator.EQUALS, id));
		if (!q.find().isEmpty())
		{
			return q.find().get(0);
		}
		else
		{
			throw new DatabaseException("Role with id " + id + " was not found.");
		}
	}

	/**
	 * Find a MolgenisEntity by id
	 * 
	 * @param id
	 *            EntityId
	 * @return MolgenisEntity
	 * @throws DatabaseException
	 * @throws ParseException
	 */
	public MolgenisEntity findEntity(Integer id) throws DatabaseException, ParseException
	{
		Query<MolgenisEntity> q = db.query(MolgenisEntity.class);
		q.addRules(new QueryRule(MolgenisEntity.ID, Operator.EQUALS, id));
		if (!q.find().isEmpty())
		{
			return q.find().get(0);
		}
		else
		{
			throw new DatabaseException("Entity with id " + id + " was not found.");
		}
	}

	/**
	 * Find all Molgenis Users
	 * 
	 * @param id
	 *            EntityId
	 * @return MolgenisEntity
	 * @throws DatabaseException
	 * @throws ParseException
	 */
	public List<MolgenisRole> findRoles() throws DatabaseException, ParseException
	{
		List<MolgenisRole> roleList = db.find(MolgenisRole.class);
		return roleList;

	}

	/**
	 * Find all Molgenis Users
	 * 
	 * @param id
	 *            EntityId
	 * @return MolgenisEntity
	 * @throws DatabaseException
	 * @throws ParseException
	 */
	public List<MolgenisEntity> findEntities() throws DatabaseException, ParseException
	{
		return db.find(MolgenisEntity.class);

	}

	/**
	 * Remove a permission based on permission id. RoleId is also required to
	 * check for sufficient rights.
	 * 
	 * @param roleId
	 *            MolgenisRole id
	 * @param permId
	 *            MolgenisPermission id
	 * @throws DatabaseException
	 * @throws IOException
	 * @throws ParseException
	 */
	public void remove(int roleId, int permId) throws DatabaseException, IOException, ParseException
	{
		MolgenisPermission perm = findPermission(permId);

		MolgenisPermission ownPerm = findPermissions(roleId, perm.getEntity_Id(), "own");

		if (ownPerm != null)
		{
			db.remove(perm);
		}
		else
		{
			throw new DatabaseException("Sorry, you have insufficient rights to remove this permission");
		}
	}

}
