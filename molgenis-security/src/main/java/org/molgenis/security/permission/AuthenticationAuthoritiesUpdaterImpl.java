package org.molgenis.security.permission;

import org.molgenis.security.core.runas.SystemSecurityToken;
import org.molgenis.security.token.RestAuthenticationToken;
import org.molgenis.security.twofactor.auth.RecoveryAuthenticationToken;
import org.molgenis.security.twofactor.auth.TwoFactorAuthenticationToken;
import org.springframework.security.access.intercept.RunAsUserToken;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;
import org.springframework.stereotype.Component;

import java.util.List;

import static java.lang.String.format;

@Component
public class AuthenticationAuthoritiesUpdaterImpl implements AuthenticationAuthoritiesUpdater
{
	@Override
	public Authentication updateAuthentication(Authentication authentication, List<GrantedAuthority> updatedAuthorities)
	{
		Authentication newAuthentication;
		if (authentication instanceof TwoFactorAuthenticationToken)
		{
			TwoFactorAuthenticationToken twoFactorAuthenticationToken = (TwoFactorAuthenticationToken) authentication;
			newAuthentication = new TwoFactorAuthenticationToken(authentication.getPrincipal(),
					authentication.getCredentials(), updatedAuthorities,
					twoFactorAuthenticationToken.getVerificationCode(), twoFactorAuthenticationToken.getSecretKey());
		}
		else if (authentication instanceof SystemSecurityToken)
		{
			newAuthentication = authentication;
		}
		else if (authentication instanceof RestAuthenticationToken)
		{
			RestAuthenticationToken restAuthenticationToken = (RestAuthenticationToken) authentication;
			newAuthentication = new RestAuthenticationToken(authentication.getPrincipal(),
					authentication.getCredentials(), updatedAuthorities, restAuthenticationToken.getToken());
		}
		else if (authentication instanceof RecoveryAuthenticationToken)
		{
			RecoveryAuthenticationToken recoveryAuthenticationToken = (RecoveryAuthenticationToken) authentication;
			newAuthentication = new RecoveryAuthenticationToken(authentication.getPrincipal(),
					authentication.getCredentials(), updatedAuthorities, recoveryAuthenticationToken.getRecoveryCode());
		}
		else if (authentication instanceof UsernamePasswordAuthenticationToken)
		{
			newAuthentication = new UsernamePasswordAuthenticationToken(authentication.getPrincipal(),
					authentication.getCredentials(), updatedAuthorities);
		}
		else if (authentication instanceof RunAsUserToken)
		{
			RunAsUserToken runAsUserToken = (RunAsUserToken) authentication;
			newAuthentication = new RunAsUserTokenDecorator(runAsUserToken, updatedAuthorities);
		}
		else if (authentication instanceof AnonymousAuthenticationToken)
		{
			AnonymousAuthenticationToken anonymousAuthenticationToken = (AnonymousAuthenticationToken) authentication;
			newAuthentication = new AnonymousAuthenticationTokenDecorator(anonymousAuthenticationToken,
					updatedAuthorities);
		}
		else
		{
			throw new SessionAuthenticationException(
					format("Unknown authentication type '%s'", authentication.getClass().getSimpleName()));
		}
		return newAuthentication;
	}

	private static final class AnonymousAuthenticationTokenDecorator extends AnonymousAuthenticationToken
	{
		private final AnonymousAuthenticationToken anonymousAuthenticationToken;

		private AnonymousAuthenticationTokenDecorator(AnonymousAuthenticationToken anonymousAuthenticationToken,
				List<GrantedAuthority> authorities)
		{
			super("dummyKey", anonymousAuthenticationToken.getPrincipal(), authorities);
			this.anonymousAuthenticationToken = anonymousAuthenticationToken;
		}

		@Override
		public int getKeyHash()
		{
			return anonymousAuthenticationToken.getKeyHash();
		}

		@Override
		public boolean equals(Object obj)
		{
			return super.equals(obj);
		}

		@Override
		public int hashCode()
		{
			return super.hashCode();
		}
	}

	private static final class RunAsUserTokenDecorator extends RunAsUserToken
	{
		private final RunAsUserToken runAsUserTokenString;

		private RunAsUserTokenDecorator(RunAsUserToken runAsUserToken, List<GrantedAuthority> authorities)
		{
			super("dummyKey", runAsUserToken.getPrincipal(), runAsUserToken.getCredentials(), authorities,
					runAsUserToken.getOriginalAuthentication());
			this.runAsUserTokenString = runAsUserToken;
		}

		@Override
		public int getKeyHash()
		{
			return runAsUserTokenString.getKeyHash();
		}

		@Override
		public boolean equals(Object obj)
		{
			return super.equals(obj);
		}

		@Override
		public int hashCode()
		{
			return super.hashCode();
		}
	}
}
