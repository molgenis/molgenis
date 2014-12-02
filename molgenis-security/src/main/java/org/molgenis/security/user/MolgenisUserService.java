package org.molgenis.security.user;

import java.util.List;

import org.molgenis.auth.MolgenisGroup;
import org.molgenis.auth.MolgenisUser;

public interface MolgenisUserService
{
	/**
	 * Returns e-mail addresses of super users
	 * 
	 * @return
	 */
	List<String> getSuEmailAddresses();

	/**
	 * Returns the given user
	 */
	MolgenisUser getUser(String username);

	/**
	 * Returns the groups that the given user belongs to
	 * 
	 * @param username
	 * @return
	 */
	Iterable<MolgenisGroup> getUserGroups(String username);

	/**
	 * Update user
	 * 
	 * @param user
	 */
	void update(MolgenisUser user);
}
