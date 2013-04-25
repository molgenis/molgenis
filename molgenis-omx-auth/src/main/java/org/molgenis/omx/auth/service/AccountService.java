package org.molgenis.omx.auth.service;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.db.QueryRule.Operator;
import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.omx.auth.MolgenisUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.util.UriComponentsBuilder;

@Scope(WebApplicationContext.SCOPE_REQUEST)
@Service
public class AccountService
{
	private static Logger logger = Logger.getLogger(AccountService.class);

	private static final String KEY_PLUGIN_AUTH_ACTIVATIONMODE = "plugin.auth.activation_mode";
	private static final String KEY_APP_NAME = "app.name";
	private static final ActivationMode DEFAULT_ACTIVATION_MODE = ActivationMode.ADMIN;
	private static final String DEFAULT_APP_NAME = "MOLGENIS";

	@Autowired
	@Qualifier("unauthorizedDatabase")
	private Database database;

	@Autowired
	private MolgenisSettings molgenisSettings;

	@Autowired
	private JavaMailSender mailSender;

	public void createUser(MolgenisUser molgenisUser, URI baseActivationUri) throws DatabaseException
	{
		// collect activation info
		String activationCode = UUID.randomUUID().toString();
		String activationEmailAddress;
		switch (getActivationMode())
		{
			case ADMIN:
				activationEmailAddress = MolgenisUserService.getInstance(database).findAdminEmail();
				if (activationEmailAddress == null || activationEmailAddress.isEmpty()) throw new DatabaseException(
						"Administrator account is missing required email address");
				break;
			case USER:
				activationEmailAddress = molgenisUser.getEmail();
				if (activationEmailAddress == null || activationEmailAddress.isEmpty()) throw new DatabaseException(
						"User '" + molgenisUser.getName() + "' is missing required email address");
				break;
			default:
				throw new RuntimeException("unknown activation mode: " + getActivationMode());
		}

		// create user
		molgenisUser.setActivationCode(activationCode);
		molgenisUser.setActive(false);
		database.add(molgenisUser);
		logger.debug("created user " + molgenisUser.getName());

		// send activation email
		URI activationUri = UriComponentsBuilder.fromUri(baseActivationUri).path('/' + activationCode).build().toUri();

		SimpleMailMessage mailMessage = new SimpleMailMessage();
		mailMessage.setTo(activationEmailAddress);
		mailMessage.setSubject("User registration for " + getAppName());
		mailMessage.setText(createActivationEmailText(molgenisUser, activationUri));
		mailSender.send(mailMessage);
		logger.debug("send activation email for user " + molgenisUser.getName() + " to " + activationEmailAddress);
	}

	/**
	 * Activate a registered user
	 * 
	 * @param activationCode
	 * @throws DatabaseException
	 */
	public void activateUser(String activationCode) throws DatabaseException
	{
		List<MolgenisUser> molgenisUsers = database.find(MolgenisUser.class, new QueryRule(MolgenisUser.ACTIVE,
				Operator.EQUALS, false), new QueryRule(MolgenisUser.ACTIVATIONCODE, Operator.EQUALS, activationCode));
		if (molgenisUsers != null && !molgenisUsers.isEmpty())
		{
			MolgenisUser molgenisUser = molgenisUsers.get(0);
			molgenisUser.setActive(true);
			database.update(molgenisUser);

			// send activated email to user
			SimpleMailMessage mailMessage = new SimpleMailMessage();
			mailMessage.setTo(molgenisUser.getEmail());
			mailMessage.setSubject("Your registration request for " + getAppName());
			mailMessage.setText(createActivatedEmailText(molgenisUser, getAppName()));
			mailSender.send(mailMessage);
		}
	}

	public void resetPassword(MolgenisUser molgenisUser) throws DatabaseException
	{
		// TODO: make this mandatory (password that was sent is valid only once)
		String newPassword = UUID.randomUUID().toString().substring(0, 8);
		molgenisUser.setPassword(newPassword);
		database.update(molgenisUser);

		// send password reseted email to user
		SimpleMailMessage mailMessage = new SimpleMailMessage();
		mailMessage.setTo(molgenisUser.getEmail());
		mailMessage.setSubject("Your new password request");
		mailMessage.setText(createPasswordResettedEmailText(newPassword));
		mailSender.send(mailMessage);
	}

	private ActivationMode getActivationMode()
	{
		String activationModeStr = molgenisSettings.getProperty(KEY_PLUGIN_AUTH_ACTIVATIONMODE);
		return ActivationMode.from(activationModeStr, DEFAULT_ACTIVATION_MODE);
	}

	private String createActivationEmailText(MolgenisUser user, URI activationUri)
	{
		StringBuilder strBuilder = new StringBuilder();
		strBuilder.append("User registration for ").append(getAppName()).append('\n');
		strBuilder.append("User name: ").append(user.getName()).append(" Full name: ").append(user.getFirstName());
		strBuilder.append(' ').append(user.getLastName()).append('\n');
		strBuilder.append("In order to activate the user visit the following URL:").append('\n');
		strBuilder.append(activationUri).append('\n').append('\n');
		return strBuilder.toString();
	}

	private String createActivatedEmailText(MolgenisUser user, String appName)
	{
		StringBuilder strBuilder = new StringBuilder();
		strBuilder.append("Dear ").append(user.getFirstName()).append(" ").append(user.getLastName()).append(",\n\n");
		strBuilder.append("your registration request for ").append(appName).append(" was approved.\n");
		strBuilder.append("Your account is now active.\n");
		return strBuilder.toString();
	}

	private String createPasswordResettedEmailText(String newPassword)
	{
		StringBuilder strBuilder = new StringBuilder();
		strBuilder.append("Somebody, probably you, requested a new password for ").append(getAppName()).append(".\n");
		strBuilder.append("The new password is: ").append(newPassword).append('\n');
		strBuilder.append("Note: we strongly recommend you reset your password after log-in!");
		return strBuilder.toString();
	}

	// TODO move to utility class
	private String getAppName()
	{
		return molgenisSettings.getProperty(KEY_APP_NAME, DEFAULT_APP_NAME);
	}

	private static enum ActivationMode
	{
		ADMIN, USER;

		public static ActivationMode from(String str, ActivationMode defaultActivationMode)
		{
			if (str == null) return defaultActivationMode;
			for (ActivationMode activationMode : ActivationMode.values())
				if (str.equalsIgnoreCase(activationMode.toString())) return activationMode;
			return defaultActivationMode;
		}
	}
}
