package org.molgenis.framework.security;

import java.util.List;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.server.TokenFactory;
import org.molgenis.util.Entity;

public class SimpleLogin implements Login
{
	public SimpleLogin()
	{

	}

	public SimpleLogin(Database db, TokenFactory tm)
	{

	}

	public SimpleLogin(Database db, String redirect, TokenFactory tm) throws Exception
	{

	}

	@Override
	public void reload(Database db)
	{
	}

	@Override
	public void logout(Database db)
	{
	}

	@Override
	public boolean isAuthenticated()
	{
		return true;
	}

	@Override
	public boolean canRead(Class<? extends Entity> entity)
	{
		return true;
	}

	@Override
	public boolean canWrite(Class<? extends Entity> entity)
	{
		return true;
	}

	public boolean hasRowEditRights(Entity entity)
	{
		return true;
	}

	public boolean hasRowReadRights(Entity entity)
	{
		return true;
	}

	public boolean isOwner(Entity entity)
	{
		return true;
	}

	public boolean isSelf(Entity entity)
	{
		return true;
	}

	public boolean lastSuperuser()
	{
		return false;
	}

	@Override
	public String getUserName()
	{
		return "";
	}

	@Override
	public Integer getUserId()
	{
		return 0;
	}

	@Override
	public boolean login(Database db, String name, String password)
	{
		return false;
	}

	@Override
	public boolean isLoginRequired()
	{
		return false;
	}

	// door Martijn erbij gezet 3 juli 2009
	@Override
	public boolean canWrite(Entity entity)
	{
		return true;
	}

	@Override
	public boolean canRead(Entity entity)
	{
		return true;
	}

	@Override
	public boolean canRead(org.molgenis.framework.ui.ScreenController<?> screen)
	{
		return true;
	}

	@Override
	public QueryRule getRowlevelSecurityFilters(Class<? extends Entity> klazz)
	{
		return null;
	}

	@Override
	public String getRedirect()
	{
		return null;
	}

	@Override
	public void setAdmin(List<? extends Entity> entities, Database db) throws DatabaseException
	{
	}

	@Override
	public void setRedirect(String redirect)
	{
	}
}
