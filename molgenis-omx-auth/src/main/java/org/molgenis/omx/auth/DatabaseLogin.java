/**
 * @author Robert Wagner
 * @date Feb 2011
 * 
 * This class provides a secured login to the molgenis suite by implementing row and table level
 * security. 
 */
package org.molgenis.omx.auth;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.db.QueryRule.Operator;
import org.molgenis.framework.security.Login;
import org.molgenis.framework.server.TokenFactory;
import org.molgenis.omx.auth.service.MolgenisUserService;
import org.molgenis.omx.auth.util.PasswordHasher;
import org.molgenis.util.Entity;

public class DatabaseLogin implements Login, Serializable
{
	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(DatabaseLogin.class);

	private final TokenFactory tm;

	/**
	 * Specifies an enumeration for valid Permissions
	 * 
	 * @author Jessica
	 */
	enum Permission
	{
		read, write, own
	};

	/** The current user that has been authenticated (if any) */
	MolgenisUser user;
	/** The groups this user is member of */
	List<MolgenisGroup> groups = new ArrayList<MolgenisGroup>();

	/** Map to quickly retrieve a permission */
	Map<String, Permission> readMap = new TreeMap<String, Permission>();
	Map<String, Permission> writeMap = new TreeMap<String, Permission>();
	Map<String, Permission> executeMap = new TreeMap<String, Permission>();
	Map<String, Permission> ownMap = new TreeMap<String, Permission>();

	protected String redirect;

	public DatabaseLogin(TokenFactory tm)
	{
		logger.debug("DatabaseLogin()");
		this.tm = tm;
	}

	/**
	 * Constructor used to login anonymous
	 * 
	 * @param db
	 *            database to log in to.
	 * @throws Exception
	 */
	public DatabaseLogin(Database db, TokenFactory tm) throws Exception
	{
		this.tm = tm;
		this.login(db, USER_ANONYMOUS_NAME, USER_ANONYMOUS_PASSWORD);
	}

	public DatabaseLogin(Database db, String redirect, TokenFactory tm) throws Exception
	{
		this(db, tm);
		this.redirect = redirect;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Integer getUserId()
	{
		if (user != null) return user.getId();
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getUserName()
	{
		if (user != null) return user.getName();
		return null;
	}

	public String getFullUserName()
	{
		return user.getFirstName() + " " + user.getLastName();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Note: Anonymous is automatically logged in but does not count as being authenticated
	 */
	@Override
	public boolean isAuthenticated()
	{
		// return user != null;
		if (user == null) return false;
		else if (USER_ANONYMOUS_NAME.equals(user.getName())) return false;
		else return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean login(Database db, String name, String password) throws Exception
	{
		// username is required
		if (name == null || "".equals(name)) return false;

		// cleanup invalid tokens before we start
		this.tm.invalidateTokens();

		// if there is a token ID for the given username, login as this user
		if (this.tm.checkIfTokenExists(name))
		{
			String queryMe = this.tm.getToken(name).getUserName();
			List<MolgenisUser> users = db.query(MolgenisUser.class).eq(MolgenisUser.NAME, queryMe)
					.eq(MolgenisUser.ACTIVE, true).find();
			if (users.size() == 1)
			{
				user = users.get(0);
				this.reload(db);
				return true;
			}
			else
			{
				return false;
			}
		}

		// password is required
		if (password == null || "".equals(password)) return false;

		try
		{
			db.find(MolgenisUser.class);
		}
		catch (Exception e)
		{
			return false;
		}

		try
		{
			PasswordHasher md5 = new PasswordHasher();

			password = md5.toMD5(password); // encode password

			List<MolgenisUser> users = db.query(MolgenisUser.class).eq(MolgenisUser.NAME, name)
					.eq(MolgenisUser.PASSWORD_, password).eq(MolgenisUser.ACTIVE, true).find();

			if (users.size() == 1 && name.equals(users.get(0).getName()) && password.equals(users.get(0).getPassword()))
			{
				user = users.get(0);
				this.reload(db);
				return true;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw e;// TODO: What to do here?
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @throws Exception
	 */
	@Override
	public void logout(Database db) throws Exception
	{
		this.user = null;
		this.reload(db);
	}

	/**
	 * Reloads all permission settings for this user from database.
	 * 
	 * If user is null, anonymous is logged in. Note: calling reload refreshes the permissions cache map.
	 * 
	 * @throws Exception
	 * 
	 */
	@Override
	public void reload(Database db) throws Exception
	{
		if (this.user == null) this.login(db, USER_ANONYMOUS_NAME, USER_ANONYMOUS_PASSWORD);
		// return;

		// // get the groups this user is member of
		// if (currentPatient != null)
		// groups = db.query(MolgenisUserGroup.class).equals("members",
		// currentPaFtient.getId()).find();
		// else {
		MolgenisUserService service = MolgenisUserService.getInstance(db);
		List<Integer> groupdIds = service.findGroupIds(this.user);

		if (!groupdIds.isEmpty())
		{
			this.groups = db.query(MolgenisGroup.class).in(MolgenisGroup.ID, groupdIds).find();
		}
		// }

		// clear the permission maps
		this.readMap.clear();
		this.writeMap.clear();
		this.executeMap.clear();
		this.ownMap.clear();

		// get permissions for this user
		loadPermissions(db, user);

		for (MolgenisGroup group : groups)
		{
			loadPermissions(db, group);
		}

	}

	/**
	 * Reload the permissions map which contains all permissions, for the logged in user.
	 * 
	 * @param db
	 *            database to load permissions from
	 * @param role
	 *            the role to load permissions for
	 * @throws DatabaseException
	 * @throws ParseException
	 */
	private void loadPermissions(Database db, MolgenisRole role) throws DatabaseException, ParseException
	{
		MolgenisUserService service = MolgenisUserService.getInstance(db);

		List<Integer> roleIdList = service.findGroupIds(role);
		// System.out.println(">>>> roleIdList==" +roleIdList.toString());
		List<MolgenisPermission> permissions = db.query(MolgenisPermission.class)
				.in(MolgenisPermission.ROLE_, roleIdList).find();

		for (MolgenisPermission permission : permissions)
		{
			// System.out.println(">>> permission==" + permission + ", role==" +
			// role);
			if ("read".equals(permission.getPermission())) this.readMap.put(permission.getEntity_ClassName(),
					Permission.read);
			else if ("write".equals(permission.getPermission()))
			{
				this.readMap.put(permission.getEntity_ClassName(), Permission.read);
				this.writeMap.put(permission.getEntity_ClassName(), Permission.write);
			}
			else if ("own".equals(permission.getPermission()))
			{
				this.readMap.put(permission.getEntity_ClassName(), Permission.read);
				this.writeMap.put(permission.getEntity_ClassName(), Permission.write);
				this.ownMap.put(permission.getEntity_ClassName(), Permission.own);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString()
	{
		StringBuffer result = new StringBuffer();
		result.append("Login(user=" + this.getUserName() + " roles=");
		for (int i = 0; i < groups.size(); i++)
		{
			if (i > 0) result.append("," + groups.get(i).getName());
			else result.append(groups.get(i).getName());
		}
		for (String key : this.readMap.keySet())
			result.append(" " + key + "=" + this.readMap.get(key));
		for (String key : this.writeMap.keySet())
			result.append(" " + key + "=" + this.writeMap.get(key));
		result.append(")");

		return result.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isLoginRequired()
	{
		return false;
	}

	/**
	 * Deprecated.
	 */
	@Override
	public QueryRule getRowlevelSecurityFilters(Class<? extends Entity> klazz)
	{
		// this.user.getAllowedToView();

		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean canRead(Class<? extends Entity> entityClass) throws DatabaseException
	{
		// System.out.println("User name >>>>>>>>>>>>>" + this.user);
		// System.out.println("Screen name >>>>>>>>>>>>>" +
		// entityClass.getName() + "Classname >>>>>>>>>> " +
		// this.readMap.containsKey(entityClass.getName()));
		if (this.isAuthenticated() && this.user.getSuperuser()) return true;

		String className = entityClass.getName();

		// Who put this here and why?
		// This makes all tables like MolgenisUser and MolgenisGroup always
		// visible to all authenticated users
		if (className.startsWith("org.molgenis.omx.auth.Molgenis")) return true;

		if (this.readMap.containsKey(className)) return true;

		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean canWrite(Class<? extends Entity> entityClass) throws DatabaseException
	{
		if (this.isAuthenticated() && this.user.getSuperuser()) return true;

		String className = entityClass.getName();

		if (this.writeMap.containsKey(className)) return true;

		return false;
	}

	public boolean owns(Class<? extends Entity> entityClass) throws DatabaseException
	{
		if (this.isAuthenticated() && this.user.getSuperuser()) return true;

		String className = entityClass.getName();

		// Who put this here and why????
		// This makes all tables like MolgenisUser and MolgenisGroup seem as
		// owned all authenticated users,
		// so they can change them at will!
		if (className.startsWith("org.molgenis.omx.auth.Molgenis")) return true;

		if (this.ownMap.containsKey(className)) return true;

		return false;
	}

	/**
	 * Indicates whether the user has permissions to read data from this entity. Note: if row-level security is
	 * activated, only rows for which the user has been given permission will display.
	 * 
	 * @param Entity
	 *            the entity to get permission from
	 * @return boolean true if can read, false otherwise
	 * @throws DatabaseException
	 * @throws DatabaseException
	 */
	@Override
	public boolean canRead(Entity entity) throws DatabaseException
	{
		// System.out.println("User name >>>>>>>>>>>>>" + this.user);
		// System.out.println("Screen name >>>>>>>>>>>>>" +
		// entity.getClass().getName() + "Classname >>>>>>>>>> " +
		// this.readMap.containsKey(entity.getClass()));

		if (this.isAuthenticated() && this.user.getSuperuser()) return true;

		if (this.isImplementing(entity, "org.molgenis.omx.auth.Authorizable"))
		{
			if (!this.isAuthenticated()) return false;

			Integer id = (Integer) entity.get("canRead");

			if (this.getUserId().equals(id)) return true;

			// Write permission implicits read permission
			return this.canWrite(entity);
		}

		// Not implementing Authorizable -> apply table level security
		return this.canRead(entity.getClass());
	}

	/**
	 * Indicates whether the user has permissions to write (and read) data from this entity. Note: if row-level security
	 * is activated, only rows for which the user has been given permission will display as editable.
	 * 
	 * @param Entity
	 *            the entity to get permission from
	 * @return boolean true if can write, false otherwise
	 * @throws DatabaseException
	 * @throws DatabaseException
	 */
	@Override
	public boolean canWrite(Entity entity) throws DatabaseException
	{
		if (this.isAuthenticated() && this.user.getSuperuser()) return true;

		if (this.isImplementing(entity, "org.molgenis.omx.auth.Authorizable"))
		{
			if (!this.isAuthenticated())
			{
				entity.setReadonly(true);
				return false;
			}

			Integer id = (Integer) entity.get("canWrite");

			if (this.getUserId().equals(id)) return true;

			// Ownership implies write permission
			return this.owns(entity);
		}

		// Not implementing Authorizable -> apply table level security
		return this.canWrite(entity.getClass());
	}

	public boolean owns(Entity entity) throws DatabaseException
	{
		if (this.isAuthenticated() && this.user.getSuperuser()) return true;

		if (this.isImplementing(entity, "org.molgenis.omx.auth.Authorizable"))
		{
			if (!this.isAuthenticated()) return false;

			Integer id = (Integer) entity.get("owns_id");

			if (id == null) logger.error("owns shouldnt be null for " + entity);

			if (id != null && id.equals(this.getUserId())) return true;

			return false;
		}

		// Not implementing Authorizable -> apply table level security
		return this.owns(entity.getClass());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean canRead(org.molgenis.framework.ui.ScreenController<?> screen)
	{
		// System.out.println("User name >>>>>>>>>>>>>" + this.user);
		// System.out.println("Screen name >>>>>>>>>>>>>" +
		// screen.getClass().getName() + "Classname >>>>>>>>>> " +
		// this.readMap.containsKey(screen.getClass().getName()));
		if (this.isAuthenticated() && this.user.getSuperuser()) return true;

		String className = screen.getClass().getName();

		// if (className.equals("app.ui.UserLoginPlugin"))
		// return true;

		if (this.readMap.containsKey(className)) return true;

		return false;
	}

	// /**
	// * {@inheritDoc}
	// */
	// @Override
	// public boolean canRead(org.molgenis.framework.ui.ScreenModel model)
	// {
	// if (this.isAuthenticated() && this.user.getSuperuser())
	// return true;
	//
	// String className = model.getClass().getName();
	//
	// //if (className.equals("app.ui.UserLoginPlugin"))
	// // return true;
	// if (this.readMap.containsKey(className))
	// return true;
	//
	// return false;
	// }

	@Override
	public String getRedirect()
	{
		return this.redirect;
	}

	/**
	 * Helper method to check if an entity is implementing the authorizable interface, i.e. to check if we should
	 * implement row-level security.
	 * 
	 * @param entity
	 * @param interfaceName
	 * @return
	 */
	private boolean isImplementing(Entity entity, String interfaceName)
	{
		if (StringUtils.isEmpty(interfaceName)) return false;

		Class<?>[] interfaces = entity.getClass().getInterfaces();

		for (Class<?> interface_ : interfaces)
		{
			if (interface_.getName().equals(interfaceName)) return true;
		}

		return false;
	}

	@Override
	public void setAdmin(List<? extends Entity> entities, Database db) throws DatabaseException
	{
		// Set owner to 'admin' if not set by user
		for (Entity e : entities)
		{
			try
			{
				Class entityClass = e.getClass();
				Method getOwns = entityClass.getMethod("getOwns_Id");
				Object result = getOwns.invoke(e);
				if (result == null)
				{
					Integer adminId = db
							.find(MolgenisRole.class,
									new QueryRule(MolgenisRole.NAME, Operator.EQUALS, Login.USER_ADMIN_NAME)).get(0)
							.getId();
					Object arglist[] = new Object[1];
					arglist[0] = adminId;

					Class partypes[] = new Class[1];
					partypes[0] = Integer.class;
					Method setOwns = entityClass.getMethod("setOwns_Id", partypes);
					setOwns.invoke(e, arglist);
				}
			}
			catch (Exception e1)
			{
				e1.printStackTrace();
				throw new DatabaseException(e1);
			}
		}
	}

	@Override
	public void setRedirect(String redirect)
	{
		this.redirect = redirect;
	}
}
