package org.molgenis.framework.security;

import java.text.ParseException;
import java.util.List;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.ui.ScreenController;
import org.molgenis.util.Entity;
import org.molgenis.util.HandleRequestDelegationException;

/**
 * Simple authentication and authorization interface that enables MOLGENIS
 * developers to enhance their applications with authentication and
 * authorization.
 * <ul>
 * <li>Login/logout (authorization)
 * <li>Entity level security (hasReadPermission(class),
 * hasWritePermission(class))
 * <li>Instance level security (rowLevelSecurityFilter,
 * hasWritePermission(object))
 * </ul>
 * Developers can implement this interface to realize a variety of security
 * schemes.
 */
public interface Login
{
	/**
	 * Authenticate the user
	 * 
	 * @param db
	 *            database to login to
	 * @param name
	 *            of user
	 * @param password
	 *            of user
	 * @return true if succesfully authenticated
	 * @throws Exception
	 *             ,RedirectedException
	 */
	public boolean login(Database db, String name, String password) throws Exception, HandleRequestDelegationException;

	/**
	 * Un-authenticate the user. Now the user will be perceived as 'guest'.
	 */
	public void logout(Database db) throws Exception;

	/**
	 * Reloads all permission settings for this user from database.
	 * 
	 * @throws ParseException
	 * @throws DatabaseException
	 * @throws Exception
	 */
	public void reload(Database db) throws DatabaseException, ParseException, Exception;

	/**
	 * Indicates whether the current user has been authenticated. Otherwise the
	 * user is treated as anonymous guest.
	 * 
	 * @return true if authtenticated
	 */
	public boolean isAuthenticated();

	/**
	 * The name of the logged in user user.
	 * 
	 * @return the unique name of the user
	 */
	public String getUserName();

	/**
	 * The system id of this user.
	 * 
	 * @return numeric identifier of the user
	 */
	public Integer getUserId();

	/**
	 * Indicate if login is required. If true then the user will not be able to
	 * access MOLGENIS unless authenticated. If false then the user will always
	 * be able to login, but with 'guest' level user rights.
	 * 
	 * @return login required
	 */
	public boolean isLoginRequired();

	/**
	 * Indicates whether the user has permissions to read data from this class
	 * of entities (aka 'entity level security')
	 * 
	 * @return readpermission
	 * @throws DatabaseException
	 * @throws DatabaseException
	 */
	public boolean canRead(Class<? extends Entity> entityClass) throws DatabaseException;

	/**
	 * Indicates whether the user has permissions to read data from this class
	 * of entities (aka 'entity level security')
	 * 
	 * @return readpermission
	 * @throws DatabaseException
	 * @throws DatabaseException
	 */
	public boolean canRead(Entity entity) throws DatabaseException;

	/**
	 * Indicates whether the user has permissions to read data from this
	 * implementation of ScreenModel
	 * 
	 * @param screen
	 * @return read permission
	 * @throws DatabaseException
	 */
	public boolean canRead(ScreenController<?> screen) throws DatabaseException;

	/**
	 * Indicates whether the user has permissions to read data from this
	 * implementation of ScreenModel
	 * 
	 * @param screen
	 * @return read permission
	 * @throws DatabaseException
	 */
	// public boolean canRead(ScreenModel model) throws DatabaseException;

	/**
	 * Indicates whether the user has permissions to add, update, delete data
	 * for this entity class (aka 'entity level security')
	 * 
	 * @return editpermission
	 * @throws DatabaseException
	 */
	public boolean canWrite(Class<? extends Entity> entityClass) throws DatabaseException;

	/**
	 * Indicates whether the user has permissions to add, update, delete this
	 * particular entity instance*
	 * 
	 * @return editpermission
	 * @throws DatabaseException
	 */
	public boolean canWrite(Entity entity) throws DatabaseException;

	/**
	 * Creates a filter which can be used by find/count to only retrieve those
	 * records the user/group is allowed to view
	 */
	public QueryRule getRowlevelSecurityFilters(Class<? extends Entity> klazz);

	/**
	 * Get name of form/plugin to redirect to after login
	 * 
	 * @return name of form/plugin
	 */
	public String getRedirect();

	public void setAdmin(List<? extends Entity> entities, Database db) throws DatabaseException;

	public void setRedirect(String redirect);
}