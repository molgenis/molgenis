package org.molgenis.security.user;

import java.util.List;

import org.molgenis.framework.db.DatabaseException;
import org.molgenis.omx.auth.MolgenisUser;

public interface MolgenisUserService
{
	/**
	 * Returns e-mail addresses of super users
	 * 
	 * @return
	 * @throws DatabaseException
	 */
	List<String> getSuEmailAddresses();

	/*
	 * Returns the currently logged in user
	 */
	MolgenisUser getCurrentUser();
}
