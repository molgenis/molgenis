package org.molgenis.framework.server;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TokenFactory implements Serializable
{
	private static final long serialVersionUID = -1669938881359317765L;
	private HashMap<String, Token> securityTokens;

	public TokenFactory()
	{
		this.securityTokens = new HashMap<String, Token>();
	}

	/**
	 * Create a new token and put it in the map of all tokens. Returns the token
	 * ID that was given out for convenience.
	 * 
	 * @param userName
	 * @param validUntil
	 * @return
	 */
	public String makeNewToken(String userName, Date validUntil)
	{
		// no tokens for anonymous
		if (userName.equals("anonymous"))
		{
			return null;
		}

		// create a new token and put it in the map under a uuid
		String uuid = UUID.randomUUID().toString();

		Date now = new Date();
		Token newToken = new Token(userName, now, validUntil);
		securityTokens.put(uuid, newToken);

		// cleanup invalid tokens
		invalidateTokens();

		return uuid;
	}

	/**
	 * Delete the token which has this ID.
	 * 
	 * @param uuid
	 * @throws Exception
	 */
	public void removeToken(String uuid) throws Exception
	{
		if (securityTokens.containsKey(uuid))
		{
			securityTokens.remove(uuid);
		}
		else
		{
			throw new Exception("The token you requested is not available");
		}
	}

	/**
	 * Get a list of token IDs that belong to this username.
	 * 
	 * @param userName
	 * @return
	 */
	public List<String> getTokenIdsForUser(String userName)
	{
		List<String> res = new ArrayList<String>();

		for (String uuid : securityTokens.keySet())
		{
			if (securityTokens.get(uuid).getUserName().equals(userName))
			{
				res.add(uuid);
			}
		}

		return res;
	}

	/**
	 * Remove tokens that are expired. Called automatically on 1) any 'token
	 * login' attempt to prevent use of bad tokens 2) creation of new tokens.
	 */
	public void invalidateTokens()
	{
		for (String uuid : securityTokens.keySet())
		{
			Date expiresAt = securityTokens.get(uuid).getExpiresAt();
			Date now = new Date();

			if (expiresAt.before(now))
			{
				securityTokens.remove(uuid);
			}
		}
	}

	/**
	 * Find out if there is a token for this ID.
	 * 
	 * @param uuid
	 * @return
	 */
	public boolean checkIfTokenExists(String uuid)
	{
		if (securityTokens.containsKey(uuid))
		{
			return true;
		}
		return false;
	}

	/**
	 * Get the token that belongs to this ID.
	 * 
	 * @param uuid
	 * @return
	 * @throws Exception
	 */
	public Token getToken(String uuid) throws Exception
	{
		if (!securityTokens.containsKey(uuid))
		{
			throw new Exception("The token you requested is not available");
		}

		return securityTokens.get(uuid);

	}

	/**
	 * Get all tokens for a username.
	 * 
	 * @param user
	 * @return
	 * @throws Exception
	 */
	public Map<String, Token> getAllTokens(String user) throws Exception
	{

		List<String> tokenIds = this.getTokenIdsForUser(user);
		Map<String, Token> tokenMap = new HashMap<String, Token>();

		for (String uuid : tokenIds)
		{
			Token t = this.getToken(uuid);
			tokenMap.put(uuid, t);
		}

		return tokenMap;
	}

	/**
	 * Helper function: print tokens on sysout.
	 */
	public void printTokens()
	{
		for (String uuid : securityTokens.keySet())
		{
			String user = securityTokens.get(uuid).getUserName();
			String created = securityTokens.get(uuid).getCreatedAt().toString();
			String expires = securityTokens.get(uuid).getExpiresAt().toString();
			System.out.println("TOKEN: " + uuid + " for user " + user + " created at " + created + " is valid until "
					+ expires);
		}
	}
}
