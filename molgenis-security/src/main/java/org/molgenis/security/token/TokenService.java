package org.molgenis.security.token;

import org.springframework.security.core.userdetails.UserDetails;

/**
 * Store and remove molgenis security tokens
 */
public interface TokenService
{

	/**
	 * Generates a token and associates it with a user. For now tokens will not expire.
	 * 
	 * @param user
	 * @return
	 */
	String generateAndStoreToken(String username);

	/**
	 * Find a user by a security token
	 * 
	 * @param token
	 * @return the user or null if not found or token is expired
	 */
	UserDetails findUserByToken(String token) throws UnknownTokenException;

	/**
	 * Remove a token from the store
	 * 
	 * @param token
	 * @return true if removed or false when the token is not found
	 */
	void removeToken(String token) throws UnknownTokenException;
}
