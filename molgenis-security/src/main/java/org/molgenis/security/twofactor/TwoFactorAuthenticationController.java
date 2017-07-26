package org.molgenis.security.twofactor;

import org.molgenis.auth.UserAuthorityFactory;
import org.molgenis.data.DataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/2fa")
public class TwoFactorAuthenticationController
{

	private DataService dataService;
	private UserAuthorityFactory userAuthorityFactory;

	@Autowired
	public TwoFactorAuthenticationController(DataService dataService, UserAuthorityFactory userAuthorityFactory)
	{
		this.dataService = dataService;
		this.userAuthorityFactory = userAuthorityFactory;
	}

	@RequestMapping(method = RequestMethod.GET)
	public String get2FA(@RequestParam String code)
	{
		if (code.equals("123456"))
		{
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();

			List<GrantedAuthority> updatedAuthorities = new ArrayList<>(auth.getAuthorities());
			updatedAuthorities.add(new SimpleGrantedAuthority("ROLE_TWO_FACTOR_AUTHENTICATED"));

			Authentication newAuth = new UsernamePasswordAuthenticationToken(auth.getPrincipal(), auth.getCredentials(),
					updatedAuthorities);

			SecurityContextHolder.getContext().setAuthentication(newAuth);
		}

		return "view-2fa";
	}
}
