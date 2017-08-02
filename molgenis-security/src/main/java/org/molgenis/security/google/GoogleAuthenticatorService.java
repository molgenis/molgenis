package org.molgenis.security.google;

/**
 * Creates a QR-code URI to create an account in Google Authenticator App on your mobile device
 */
public interface GoogleAuthenticatorService
{
	/**
	 * <p>Generate URI for Google Authenticator</p>
	 *
	 * @return Google Authenticator URI
	 */
	String getGoogleAuthenticatorURI(String secretKey) throws IllegalStateException;
}
