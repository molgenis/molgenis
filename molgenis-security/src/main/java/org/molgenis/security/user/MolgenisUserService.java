package org.molgenis.security.user;

import java.util.List;

import org.molgenis.omx.auth.MolgenisUser;

public interface MolgenisUserService
{
	/**
	 * Returns e-mail addresses of super users
	 * 
	 * @return
	 */
	List<String> getSuEmailAddresses();

	/**
	 * Returns the currently logged in user
	 */
	MolgenisUser getUser(String username);

	/**
	 * Update user
	 * 
	 * @param user
	 */
	void update(MolgenisUser user);
}
