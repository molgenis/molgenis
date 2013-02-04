package org.molgenis.util;

import java.io.ByteArrayOutputStream;

import org.molgenis.util.SimpleEmailService.EmailException;

public interface EmailService
{

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.molgenis.util.email.EmailService#getSmtpFromAddress()
	 */
	public String getSmtpFromAddress();

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.molgenis.util.email.EmailService#setSmtpFromAddress(java.lang.String)
	 */
	public void setSmtpFromAddress(String smtpFromAddress);

	public boolean email(String subject, String body, String toEmail, boolean deObf) throws EmailException;
	
	/* (non-Javadoc)
	 * @see org.molgenis.util.email.EmailService#sendEmail(java.lang.String, java.lang.String, java.lang.String)
	 */
	public abstract boolean email(String subject, String body, String toEmail) throws EmailException;

	public boolean email(String subject, String body, String toEmail, boolean deObf, String sender) throws EmailException;
	
	public boolean email(String subject, String body, String toEmail, String fileAttachment, boolean deObf) throws EmailException;
	
	public boolean email(String subject, String body, String toEmail, String fileAttachment, ByteArrayOutputStream outputStream, boolean deObf) throws EmailException;
	
	public boolean email(String subject, String body, String toEmail, String fileAttachment, ByteArrayOutputStream outputStream, boolean deObf, String sender) throws EmailException;
	/* (non-Javadoc)
	 * @see org.molgenis.util.email.EmailServer#getSmtpHostName()
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.molgenis.util.email.EmailServer#getSmtpHostName()
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.molgenis.util.email.EmailService#getSmtpHostName()
	 */
	public String getSmtpHostname();

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.molgenis.util.email.EmailServer#setSmtpHostName(java.lang.String)
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.molgenis.util.email.EmailServer#setSmtpHostName(java.lang.String)
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.molgenis.util.email.EmailService#setSmtpHostName(java.lang.String)
	 */
	public void setSmtpHostname(String smtpHostName);

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.molgenis.util.email.EmailServer#getSmtpHostPort()
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.molgenis.util.email.EmailServer#getSmtpHostPort()
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.molgenis.util.email.EmailService#getSmtpHostPort()
	 */
	public Integer getSmtpPort();

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.molgenis.util.email.EmailServer#setSmtpHostPort(java.lang.Integer)
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.molgenis.util.email.EmailServer#setSmtpHostPort(java.lang.Integer)
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.molgenis.util.email.EmailService#setSmtpHostPort(java.lang.Integer)
	 */
	public void setSmtpPort(Integer smtpHostPort);

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.molgenis.util.email.EmailServer#getSmtpAuthUser()
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.molgenis.util.email.EmailServer#getSmtpAuthUser()
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.molgenis.util.email.EmailService#getSmtpAuthUser()
	 */
	public String getSmtpUser();

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.molgenis.util.email.EmailServer#setSmtpAuthUser(java.lang.String)
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.molgenis.util.email.EmailServer#setSmtpAuthUser(java.lang.String)
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.molgenis.util.email.EmailService#setSmtpAuthUser(java.lang.String)
	 */
	public void setSmtpUser(String smtpAuthUser);

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.molgenis.util.email.EmailServer#getSmtpAuthPassword()
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.molgenis.util.email.EmailServer#getSmtpAuthPassword()
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.molgenis.util.email.EmailService#getSmtpAuthPassword()
	 */
	public String getSmtpAu();

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.molgenis.util.email.EmailServer#setSmtpAuthPassword(java.lang.String)
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.molgenis.util.email.EmailServer#setSmtpAuthPassword(java.lang.String)
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.molgenis.util.email.EmailService#setSmtpAuthPassword(java.lang.String
	 * )
	 */
	public void setSmtpAu(String smtpAu);

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.molgenis.util.email.EmailServer#getSmtpProtocol()
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.molgenis.util.email.EmailServer#getSmtpProtocol()
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.molgenis.util.email.EmailService#getSmtpProtocol()
	 */
	public String getSmtpProtocol();

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.molgenis.util.email.EmailServer#setSmtpProtocol(java.lang.String)
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.molgenis.util.email.EmailServer#setSmtpProtocol(java.lang.String)
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.molgenis.util.email.EmailService#setSmtpProtocol(java.lang.String)
	 */
	public void setSmtpProtocol(String smtpProtocol);

}
