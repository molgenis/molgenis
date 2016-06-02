package org.molgenis.security.token;

import static java.util.Objects.requireNonNull;
import static org.molgenis.auth.MolgenisTokenMetaData.MOLGENIS_TOKEN;
import static org.molgenis.auth.MolgenisUserMetaData.MOLGENIS_USER;

import java.util.Date;

import org.apache.commons.lang3.time.DateUtils;
import org.molgenis.auth.MolgenisToken;
import org.molgenis.auth.MolgenisTokenFactory;
import org.molgenis.auth.MolgenisTokenMetaData;
import org.molgenis.auth.MolgenisUser;
import org.molgenis.auth.MolgenisUserMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.security.core.runas.RunAsSystem;
import org.molgenis.security.core.token.TokenService;
import org.molgenis.security.core.token.UnknownTokenException;
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
	private final MolgenisTokenFactory molgenisTokenFactory;

	public DataServiceTokenService(TokenGenerator tokenGenerator, DataService dataService,
			UserDetailsService userDetailsService, MolgenisTokenFactory molgenisTokenFactory)
	{
		this.tokenGenerator = requireNonNull(tokenGenerator);
		this.dataService = requireNonNull(dataService);
		this.userDetailsService = requireNonNull(userDetailsService);
		this.molgenisTokenFactory = requireNonNull(molgenisTokenFactory);
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
	 * Generates a token and associates it with a user.
	 * 
	 * Token expires in 2 hours
	 * 
	 * @param username
	 * @param description
	 * @return
	 */
	@Override
	@Transactional
	@RunAsSystem
	public String generateAndStoreToken(String username, String description)
	{
		MolgenisUser user = dataService
				.findOne(MOLGENIS_USER, new QueryImpl<MolgenisUser>().eq(MolgenisUserMetaData.USERNAME, username),
						MolgenisUser.class);

		if (user == null) throw new IllegalArgumentException("Unknown username [" + username + "]");

		String token = tokenGenerator.generateToken();

		MolgenisToken molgenisToken = molgenisTokenFactory.create();
		molgenisToken.setMolgenisUser(user);
		molgenisToken.setToken(token);
		molgenisToken.setDescription(description);
		molgenisToken.setExpirationDate(DateUtils.addHours(new Date(), 2));
		dataService.add(MOLGENIS_TOKEN, molgenisToken);

		return token;
	}

	@Override
	@Transactional
	@RunAsSystem
	public void removeToken(String token) throws UnknownTokenException
	{
		MolgenisToken molgenisToken = getMolgenisToken(token);
		dataService.delete(MOLGENIS_TOKEN, molgenisToken);
	}

	private MolgenisToken getMolgenisToken(String token) throws UnknownTokenException
	{
		MolgenisToken molgenisToken = dataService
				.findOne(MOLGENIS_TOKEN, new QueryImpl<MolgenisToken>().eq(MolgenisTokenMetaData.TOKEN, token),
						MolgenisToken.class);

		if ((molgenisToken == null)
				|| ((molgenisToken.getExpirationDate() != null) && new Date().after(molgenisToken.getExpirationDate())))
		{
			throw new UnknownTokenException("Invalid token");
		}

		return molgenisToken;
	}
}
