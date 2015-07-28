package org.molgenis.security.account;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.Query;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.db.QueryRule.Operator;
import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.omx.auth.MolgenisGroup;
import org.molgenis.omx.auth.MolgenisGroupMember;
import org.molgenis.omx.auth.MolgenisUser;
import org.molgenis.security.user.MolgenisUserException;
import org.molgenis.security.user.MolgenisUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class AccountService
{
	private static final Logger logger = Logger.getLogger(AccountService.class);

	public static final String KEY_PLUGIN_AUTH_ACTIVATIONMODE = "plugin.auth.activation_mode";
	public static final String ALL_USER_GROUP = "All Users";
	private static final String KEY_APP_NAME = "app.name";
	private static final ActivationMode DEFAULT_ACTIVATION_MODE = ActivationMode.ADMIN;
	private static final String DEFAULT_APP_NAME = "MOLGENIS";

	@Autowired
	private Database unsecuredDatabase;

	@Autowired
	private MolgenisSettings molgenisSettings;

	@Autowired
	private JavaMailSender mailSender;

	@Autowired
	private MolgenisUserService molgenisUserService;

	@Autowired
	private PasswordEncoder passwordEncoder;

	public void createUser(MolgenisUser molgenisUser, URI baseActivationUri) throws DatabaseException
	{
		// collect activation info
		String activationCode = UUID.randomUUID().toString();
		List<String> activationEmailAddresses;
		switch (getActivationMode())
		{
			case ADMIN:
				activationEmailAddresses = molgenisUserService.getSuEmailAddresses();
				if (activationEmailAddresses == null || activationEmailAddresses.isEmpty()) throw new DatabaseException(
						"Administrator account is missing required email address");
				break;
			case USER:
				String activationEmailAddress = molgenisUser.getEmail();
				if (activationEmailAddress == null || activationEmailAddress.isEmpty()) throw new DatabaseException(
						"User '" + molgenisUser.getUsername() + "' is missing required email address");
				activationEmailAddresses = Arrays.asList(activationEmailAddress);
				break;
			default:
				throw new RuntimeException("unknown activation mode: " + getActivationMode());
		}

		// create user
		molgenisUser.setActivationCode(activationCode);
		molgenisUser.setActive(false);
		logger.debug("created user " + molgenisUser.getUsername());
		unsecuredDatabase.add(molgenisUser);

		// add user to group
		Query<MolgenisGroup> groupQuery = unsecuredDatabase.query(MolgenisGroup.class);
		groupQuery.equals(MolgenisGroup.NAME, ALL_USER_GROUP);
		List<MolgenisGroup> allUserGroups = groupQuery.find();
		if (allUserGroups.size() == 1)
		{
			MolgenisGroup group = allUserGroups.get(0);
			MolgenisGroupMember molgenisGroupMember = new MolgenisGroupMember();
			molgenisGroupMember.setMolgenisGroup(group.getId());
			molgenisGroupMember.setMolgenisUser(molgenisUser.getId());
			unsecuredDatabase.add(molgenisGroupMember);
		}
		// send activation email
		URI activationUri = UriComponentsBuilder.fromUri(baseActivationUri).path('/' + activationCode).build().toUri();

		SimpleMailMessage mailMessage = new SimpleMailMessage();
		mailMessage.setTo(activationEmailAddresses.toArray(new String[]
		{}));
		mailMessage.setSubject("User registration for " + getAppName());
		mailMessage.setText(createActivationEmailText(molgenisUser, activationUri));
		mailSender.send(mailMessage);
		logger.debug("send activation email for user " + molgenisUser.getUsername() + " to "
				+ StringUtils.join(activationEmailAddresses, ','));
	}

	/**
	 * Activate a registered user
	 * 
	 * @param activationCode
	 * @throws DatabaseException
	 */
	public void activateUser(String activationCode) throws DatabaseException
	{
		List<MolgenisUser> molgenisUsers = unsecuredDatabase.find(MolgenisUser.class, new QueryRule(
				MolgenisUser.ACTIVE, Operator.EQUALS, false), new QueryRule(MolgenisUser.ACTIVATIONCODE,
				Operator.EQUALS, activationCode));
		if (molgenisUsers != null && !molgenisUsers.isEmpty())
		{
			MolgenisUser molgenisUser = molgenisUsers.get(0);
			molgenisUser.setActive(true);
			unsecuredDatabase.update(molgenisUser);

			// send activated email to user
			SimpleMailMessage mailMessage = new SimpleMailMessage();
			mailMessage.setTo(molgenisUser.getEmail());
			mailMessage.setSubject("Your registration request for " + getAppName());
			mailMessage.setText(createActivatedEmailText(molgenisUser, getAppName()));
			mailSender.send(mailMessage);
		}
		else
		{
			throw new MolgenisUserException("Invalid activation code or account already activated.");
		}
	}

	public void resetPassword(String userEmail) throws DatabaseException
	{
		MolgenisUser molgenisUser = MolgenisUser.findByEmail(unsecuredDatabase, userEmail);
		if (molgenisUser != null)
		{
			String newPassword = UUID.randomUUID().toString().substring(0, 8);
			molgenisUser.setPassword(passwordEncoder.encode(newPassword));
			unsecuredDatabase.update(molgenisUser);

			// send password reseted email to user
			SimpleMailMessage mailMessage = new SimpleMailMessage();
			mailMessage.setTo(molgenisUser.getEmail());
			mailMessage.setSubject("Your new password request");
			mailMessage.setText(createPasswordResettedEmailText(newPassword));
			mailSender.send(mailMessage);
		}
		else
		{
			throw new MolgenisUserException("Invalid email address.");
		}
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
		strBuilder.append("User name: ").append(user.getUsername()).append(" Full name: ").append(user.getFirstName());
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
