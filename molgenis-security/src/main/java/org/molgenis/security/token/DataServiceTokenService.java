package org.molgenis.security.token;

import java.util.Date;

import org.molgenis.data.DataService;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.omx.auth.MolgenisUser;
import org.molgenis.security.runas.RunAsSystem;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.transaction.annotation.Transactional;

/**
 * TokensService implementation that uses the DataService
 */
public class DataServiceTokenService implements TokenService
{
	private final TokenGenerator tokenGenerator;
	private final DataService dataService;
	private final UserDetailsService userDetailsService;

	public DataServiceTokenService(TokenGenerator tokenGenerator, DataService dataService,
			UserDetailsService userDetailsService)
	{
		this.tokenGenerator = tokenGenerator;
		this.dataService = dataService;
		this.userDetailsService = userDetailsService;
	}

	/**
	 * Find a user by a security token
	 * 
	 * @param token
	 * @return the user or null if not found or token is expired
	 */
	@Override
	@Transactional(readOnly = true)
	@RunAsSystem
	public UserDetails findUserByToken(String token) throws UnknownTokenException
	{
		MolgenisToken molgenisToken = getMolgenisToken(token);
		return userDetailsService.loadUserByUsername(molgenisToken.getMolgenisUser().getUsername());
	}

	/**
	 * Generates a token and associates it with a user. For now tokens will not expire.
	 * 
	 * @param user
	 * @return
	 */
	@Override
	@Transactional
	@RunAsSystem
	public String generateAndStoreToken(String username)
	{
		MolgenisUser user = dataService.findOne(MolgenisUser.ENTITY_NAME,
				new QueryImpl().eq(MolgenisUser.USERNAME, username), MolgenisUser.class);

		if (user == null) throw new IllegalArgumentException("Unknown username [" + username + "]");

		String token = tokenGenerator.generateToken();

		MolgenisToken molgenisToken = new MolgenisToken();
		molgenisToken.setMolgenisUser(user);
		molgenisToken.setToken(token);
		molgenisToken.setExpirationDate(null);// No expiration date
		dataService.add(MolgenisToken.ENTITY_NAME, molgenisToken);

		return token;
	}

	@Override
	@Transactional
	@RunAsSystem
	public void removeToken(String token) throws UnknownTokenException
	{
		MolgenisToken molgenisToken = getMolgenisToken(token);
		dataService.delete(MolgenisToken.ENTITY_NAME, molgenisToken);
	}

	private MolgenisToken getMolgenisToken(String token) throws UnknownTokenException
	{
		MolgenisToken molgenisToken = dataService.findOne(MolgenisToken.ENTITY_NAME,
				new QueryImpl().eq(MolgenisToken.TOKEN, token), MolgenisToken.class);

		if ((molgenisToken == null)
				|| ((molgenisToken.getExpirationDate() != null) && new Date().after(molgenisToken.getExpirationDate())))
		{
			throw new UnknownTokenException("Invalid token");
		}

		return molgenisToken;
	}
}
