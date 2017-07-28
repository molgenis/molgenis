package org.molgenis.security.google;

/**
 * @author tommy
 */
public interface GoogleAuthenticatorService
{
	/**
	 * <p>Genreate URI for Google Authenticator</p>
	 *
	 * @return Google Authenticator URI
	 */
	String getGoogleAuthenticatorURI(String secretKey);
}
