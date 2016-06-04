package org.molgenis.security.core;

public interface SecureIdGenerator
{
	/**
	 * Generates a secure identifier.
	 */
	String generateId();

	/**
	 * Generates an 8 character password.
	 */
	String generatePassword();

	/**
	 * Generates an activation code.
	 */
	String generateActivationCode();
}
