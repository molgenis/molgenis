/**
 * @author Robert Wagner
 * @date Feb 2011
 * 
 * This class provides a secured login to the molgenis suite by implementing row and table level
 * security. 
 */
package org.molgenis.omx.auth;

import java.io.Serializable;
import java.text.ParseException;
import java.util.List;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.security.Login;
import org.molgenis.util.Entity;

@Deprecated
public class DatabaseLogin implements Login, Serializable
{

	@Override
	public boolean login(Database db, String name, String password) throws Exception
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void logout(Database db) throws Exception
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void reload(Database db) throws DatabaseException, ParseException, Exception
	{
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isAuthenticated()
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getUserName()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer getUserId()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isLoginRequired()
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean canRead(Class<? extends Entity> entityClass) throws DatabaseException
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean canRead(Entity entity) throws DatabaseException
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean canWrite(Class<? extends Entity> entityClass) throws DatabaseException
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean canWrite(Entity entity) throws DatabaseException
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public QueryRule getRowlevelSecurityFilters(Class<? extends Entity> klazz)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getRedirect()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setAdmin(List<? extends Entity> entities, Database db) throws DatabaseException
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void setRedirect(String redirect)
	{
		// TODO Auto-generated method stub

	}
}