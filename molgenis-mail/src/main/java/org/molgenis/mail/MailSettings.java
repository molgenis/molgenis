package org.molgenis.mail;

import java.nio.charset.Charset;

interface MailSettings
{
	String getHost();

	int getPort();

	String getProtocol();

	String getUsername();

	String getPassword();

	Charset getDefaultEncoding();
}
