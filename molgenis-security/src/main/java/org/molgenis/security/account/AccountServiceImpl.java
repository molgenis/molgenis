package org.molgenis.security.account;

import static com.google.common.base.Preconditions.checkNotNull;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.auth.MolgenisGroup;
import org.molgenis.auth.MolgenisGroupMember;
import org.molgenis.auth.MolgenisUser;
import org.molgenis.data.DataService;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.settings.AppSettings;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.security.core.runas.RunAsSystem;
import org.molgenis.security.user.MolgenisUserException;
import org.molgenis.security.user.MolgenisUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountServiceImpl implements AccountService
{
	private static final Logger LOG = LoggerFactory.getLogger(AccountServiceImpl.class);

	private final DataService dataService;
	private final JavaMailSender mailSender;
	private final MolgenisUserService molgenisUserService;
	private final AppSettings appSettings;

	@Autowired
	public AccountServiceImpl(DataService dataService, JavaMailSender mailSender,
			MolgenisUserService molgenisUserService, AppSettings appSettings)
	{
		this.dataService = checkNotNull(dataService);
		this.mailSender = checkNotNull(mailSender);
		this.molgenisUserService = checkNotNull(molgenisUserService);
		this.appSettings = checkNotNull(appSettings);
	}

	@Override
	@RunAsSystem
	@Transactional
	public void createUser(MolgenisUser molgenisUser, String baseActivationUri) throws UsernameAlreadyExistsException,
			EmailAlreadyExistsException
	{
		// Check if username already exists
		if (molgenisUserService.getUser(molgenisUser.getUsername()) != null)
		{
			throw new UsernameAlreadyExistsException("Username '" + molgenisUser.getUsername() + "' already exists.");
		}

		// Check if email already exists
		if (molgenisUserService.getUserByEmail(molgenisUser.getEmail()) != null)
		{
			throw new EmailAlreadyExistsException("Email '" + molgenisUser.getEmail() + "' is already registered.");
		}

		// collect activation info
		String activationCode = UUID.randomUUID().toString();
		List<String> activationEmailAddresses;
		if (appSettings.getSignUpModeration())
		{
			activationEmailAddresses = molgenisUserService.getSuEmailAddresses();
			if (activationEmailAddresses == null || activationEmailAddresses.isEmpty()) throw new MolgenisDataException(
					"Administrator account is missing required email address");
		}
		else
		{
			String activationEmailAddress = molgenisUser.getEmail();
			if (activationEmailAddress == null || activationEmailAddress.isEmpty()) throw new MolgenisDataException(
					"User '" + molgenisUser.getUsername() + "' is missing required email address");
			activationEmailAddresses = Arrays.asList(activationEmailAddress);
		}

		// create user
		molgenisUser.setActivationCode(activationCode);
		molgenisUser.setActive(false);
		dataService.add(MolgenisUser.ENTITY_NAME, molgenisUser);
		LOG.debug("created user " + molgenisUser.getUsername());

		// add user to group
		MolgenisGroup group = dataService.findOne(MolgenisGroup.ENTITY_NAME,
				new QueryImpl().eq(MolgenisGroup.NAME, ALL_USER_GROUP), MolgenisGroup.class);
		MolgenisGroupMember molgenisGroupMember = null;
		if (group != null)
		{
			molgenisGroupMember = new MolgenisGroupMember();
			molgenisGroupMember.setMolgenisGroup(group);
			molgenisGroupMember.setMolgenisUser(molgenisUser);
			dataService.add(MolgenisGroupMember.ENTITY_NAME, molgenisGroupMember);
		}

		// send activation email
		URI activationUri = URI.create(baseActivationUri + '/' + activationCode);

		try
		{
			SimpleMailMessage mailMessage = new SimpleMailMessage();
			mailMessage.setTo(activationEmailAddresses.toArray(new String[]
			{}));
			mailMessage.setSubject("User registration for " + appSettings.getTitle());
			mailMessage.setText(createActivationEmailText(molgenisUser, activationUri));
			mailSender.send(mailMessage);
		}
		catch (MailException mce)
		{
			LOG.error("Could not send signup mail", mce);

			if (molgenisGroupMember != null)
			{
				dataService.delete(MolgenisGroupMember.ENTITY_NAME, molgenisGroupMember);
			}

			if (molgenisUser != null)
			{
				dataService.delete(MolgenisUser.ENTITY_NAME, molgenisUser);
			}

			throw new MolgenisUserException(
					"An error occurred. Please contact the administrator. You are not signed up!");
		}
		LOG.debug("send activation email for user " + molgenisUser.getUsername() + " to "
				+ StringUtils.join(activationEmailAddresses, ','));

	}

	@Override
	@RunAsSystem
	public void activateUser(String activationCode)
	{
		MolgenisUser molgenisUser = dataService.findOne(MolgenisUser.ENTITY_NAME,
				new QueryImpl().eq(MolgenisUser.ACTIVE, false).and().eq(MolgenisUser.ACTIVATIONCODE, activationCode),
				MolgenisUser.class);

		if (molgenisUser != null)
		{
			molgenisUser.setActive(true);
			dataService.update(MolgenisUser.ENTITY_NAME, molgenisUser);

			// send activated email to user
			SimpleMailMessage mailMessage = new SimpleMailMessage();
			mailMessage.setTo(molgenisUser.getEmail());
			mailMessage.setSubject("Your registration request for " + appSettings.getTitle());
			mailMessage.setText(createActivatedEmailText(molgenisUser, appSettings.getTitle()));
			mailSender.send(mailMessage);
		}
		else
		{
			throw new MolgenisUserException("Invalid activation code or account already activated.");
		}
	}

	@Override
	@RunAsSystem
	public void changePassword(String username, String newPassword)
	{
		MolgenisUser molgenisUser = dataService.findOne(MolgenisUser.ENTITY_NAME,
				new QueryImpl().eq(MolgenisUser.USERNAME, username), MolgenisUser.class);

		if (molgenisUser == null)
		{
			throw new MolgenisUserException("Unknown user [" + username + "]");
		}

		molgenisUser.setPassword(newPassword);
		molgenisUser.setChangePassword(false);
		dataService.update(MolgenisUser.ENTITY_NAME, molgenisUser);

		LOG.info("Changed password of user [" + username + "]");
	}

	@Override
	@RunAsSystem
	public void resetPassword(String userEmail)
	{
		MolgenisUser molgenisUser = dataService.findOne(MolgenisUser.ENTITY_NAME,
				new QueryImpl().eq(MolgenisUser.EMAIL, userEmail), MolgenisUser.class);

		if (molgenisUser != null)
		{
			String newPassword = UUID.randomUUID().toString().substring(0, 8);
			molgenisUser.setPassword(newPassword);
			molgenisUser.setChangePassword(true);
			dataService.update(MolgenisUser.ENTITY_NAME, molgenisUser);

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

	private String createActivationEmailText(MolgenisUser user, URI activationUri)
	{
		StringBuilder strBuilder = new StringBuilder();
		strBuilder.append("User registration for ").append(appSettings.getTitle()).append('\n');
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
		strBuilder.append("Somebody, probably you, requested a new password for ").append(appSettings.getTitle())
				.append(".\n");
		strBuilder.append("The new password is: ").append(newPassword).append('\n');
		strBuilder.append("Note: we strongly recommend you reset your password after log-in!");
		return strBuilder.toString();
	}
}
