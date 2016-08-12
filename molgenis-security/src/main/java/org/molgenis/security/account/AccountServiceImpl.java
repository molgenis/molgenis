package org.molgenis.security.account;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.auth.MolgenisGroup;
import org.molgenis.auth.MolgenisGroupMember;
import org.molgenis.auth.MolgenisGroupMemberFactory;
import org.molgenis.auth.MolgenisUser;
import org.molgenis.data.DataService;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.settings.AppSettings;
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

import java.net.URI;
import java.util.List;
import java.util.UUID;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static org.molgenis.auth.MolgenisGroupMemberMetaData.MOLGENIS_GROUP_MEMBER;
import static org.molgenis.auth.MolgenisGroupMetaData.MOLGENIS_GROUP;
import static org.molgenis.auth.MolgenisGroupMetaData.NAME;
import static org.molgenis.auth.MolgenisUserMetaData.*;

@Service
public class AccountServiceImpl implements AccountService
{
	private static final Logger LOG = LoggerFactory.getLogger(AccountServiceImpl.class);

	private final DataService dataService;
	private final JavaMailSender mailSender;
	private final MolgenisUserService molgenisUserService;
	private final AppSettings appSettings;
	private final MolgenisGroupMemberFactory molgenisGroupMemberFactory;

	@Autowired
	public AccountServiceImpl(DataService dataService, JavaMailSender mailSender,
			MolgenisUserService molgenisUserService, AppSettings appSettings,
			MolgenisGroupMemberFactory molgenisGroupMemberFactory)
	{
		this.dataService = requireNonNull(dataService);
		this.mailSender = requireNonNull(mailSender);
		this.molgenisUserService = requireNonNull(molgenisUserService);
		this.appSettings = requireNonNull(appSettings);
		this.molgenisGroupMemberFactory = requireNonNull(molgenisGroupMemberFactory);
	}

	@Override
	@RunAsSystem
	@Transactional
	public void createUser(MolgenisUser molgenisUser, String baseActivationUri)
			throws UsernameAlreadyExistsException, EmailAlreadyExistsException
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
			if (activationEmailAddresses == null || activationEmailAddresses.isEmpty())
				throw new MolgenisDataException("Administrator account is missing required email address");
		}
		else
		{
			String activationEmailAddress = molgenisUser.getEmail();
			if (activationEmailAddress == null || activationEmailAddress.isEmpty()) throw new MolgenisDataException(
					"User '" + molgenisUser.getUsername() + "' is missing required email address");
			activationEmailAddresses = asList(activationEmailAddress);
		}

		// create user
		molgenisUser.setActivationCode(activationCode);
		molgenisUser.setActive(false);
		dataService.add(MOLGENIS_USER, molgenisUser);
		LOG.debug("created user " + molgenisUser.getUsername());

		// add user to group
		MolgenisGroup group = dataService.query(MOLGENIS_GROUP, MolgenisGroup.class).eq(NAME, ALL_USER_GROUP).findOne();
		MolgenisGroupMember molgenisGroupMember = null;
		if (group != null)
		{
			molgenisGroupMember = molgenisGroupMemberFactory.create();
			molgenisGroupMember.setMolgenisGroup(group);
			molgenisGroupMember.setMolgenisUser(molgenisUser);
			dataService.add(MOLGENIS_GROUP_MEMBER, molgenisGroupMember);
		}

		// send activation email
		URI activationUri = URI.create(baseActivationUri + '/' + activationCode);

		try
		{
			SimpleMailMessage mailMessage = new SimpleMailMessage();
			mailMessage.setTo(activationEmailAddresses.toArray(new String[] {}));
			mailMessage.setSubject("User registration for " + appSettings.getTitle());
			mailMessage.setText(createActivationEmailText(molgenisUser, activationUri));
			mailSender.send(mailMessage);
		}
		catch (MailException mce)
		{
			LOG.error("Could not send signup mail", mce);

			if (molgenisGroupMember != null)
			{
				dataService.delete(MOLGENIS_GROUP_MEMBER, molgenisGroupMember);
			}

			dataService.delete(MOLGENIS_USER, molgenisUser);

			throw new MolgenisUserException(
					"An error occurred. Please contact the administrator. You are not signed up!");
		}
		LOG.debug("send activation email for user " + molgenisUser.getUsername() + " to " + StringUtils
				.join(activationEmailAddresses, ','));

	}

	@Override
	@RunAsSystem
	public void activateUser(String activationCode)
	{
		MolgenisUser user = dataService.query(MOLGENIS_USER, MolgenisUser.class).eq(ACTIVE, false).and()
				.eq(ACTIVATIONCODE, activationCode).findOne();

		if (user != null)
		{
			user.setActive(true);
			dataService.update(MOLGENIS_USER, user);

			// send activated email to user
			SimpleMailMessage mailMessage = new SimpleMailMessage();
			mailMessage.setTo(user.getEmail());
			mailMessage.setSubject("Your registration request for " + appSettings.getTitle());
			mailMessage.setText(createActivatedEmailText(user, appSettings.getTitle()));
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
		MolgenisUser user = dataService.query(MOLGENIS_USER, MolgenisUser.class).eq(USERNAME, username).findOne();
		if (user == null)
		{
			throw new MolgenisUserException(format("Unknown user [%s]"));
		}

		user.setPassword(newPassword);
		user.setChangePassword(false);
		dataService.update(MOLGENIS_USER, user);

		LOG.info("Changed password of user [{}]", username);
	}

	@Override
	@RunAsSystem
	public void resetPassword(String userEmail)
	{
		MolgenisUser molgenisUser = dataService.query(MOLGENIS_USER, MolgenisUser.class).eq(EMAIL, userEmail).findOne();

		if (molgenisUser != null)
		{
			String newPassword = UUID.randomUUID().toString().substring(0, 8);
			molgenisUser.setPassword(newPassword);
			molgenisUser.setChangePassword(true);
			dataService.update(MOLGENIS_USER, molgenisUser);

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
