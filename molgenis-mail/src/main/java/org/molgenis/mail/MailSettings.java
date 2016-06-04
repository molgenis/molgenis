package org.molgenis.mail;

interface MailSettings
{
	String getHost();

	int getPort();

	String getProtocol();

	String getUsername();

	String getPassword();

	boolean isAuth();

	boolean isStartTlsEnable();

	boolean isQuitWait();
}
