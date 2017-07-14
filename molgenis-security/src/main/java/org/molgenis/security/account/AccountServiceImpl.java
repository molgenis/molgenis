package org.molgenis.security.account;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.auth.Group;
import org.molgenis.auth.GroupMember;
import org.molgenis.auth.GroupMemberFactory;
import org.molgenis.auth.User;
import org.molgenis.data.DataService;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.populate.IdGenerator;
import org.molgenis.data.settings.AppSettings;
import org.molgenis.security.core.runas.RunAsSystem;
import org.molgenis.security.user.MolgenisUserException;
import org.molgenis.security.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.util.List;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static org.molgenis.auth.GroupMemberMetaData.GROUP_MEMBER;
import static org.molgenis.auth.GroupMetaData.GROUP;
import static org.molgenis.auth.GroupMetaData.NAME;
import static org.molgenis.auth.UserMetaData.*;
import static org.molgenis.data.populate.IdGenerator.Strategy.SECURE_RANDOM;
import static org.molgenis.data.populate.IdGenerator.Strategy.SHORT_SECURE_RANDOM;

@Service
public class AccountServiceImpl implements AccountService
{
	private static final Logger LOG = LoggerFactory.getLogger(AccountServiceImpl.class);

	private final DataService dataService;
	private final MailSender mailSender;
	private final UserService userService;
	private final AppSettings appSettings;
	private final GroupMemberFactory groupMemberFactory;
	private final IdGenerator idGenerator;

	@Autowired
	public AccountServiceImpl(DataService dataService, MailSender mailSender, UserService userService,
			AppSettings appSettings, GroupMemberFactory groupMemberFactory, IdGenerator idGenerator)
	{
		this.dataService = requireNonNull(dataService);
		this.mailSender = requireNonNull(mailSender);
		this.userService = requireNonNull(userService);
		this.appSettings = requireNonNull(appSettings);
		this.groupMemberFactory = requireNonNull(groupMemberFactory);
		this.idGenerator = requireNonNull(idGenerator);
	}

	@Override
	@RunAsSystem
	@Transactional
	public void createUser(User user, String baseActivationUri)
			throws UsernameAlreadyExistsException, EmailAlreadyExistsException
	{
		// Check if username already exists
		if (userService.getUser(user.getUsername()) != null)
		{
			throw new UsernameAlreadyExistsException("Username '" + user.getUsername() + "' already exists.");
		}

		// Check if email already exists
		if (userService.getUserByEmail(user.getEmail()) != null)
		{
			throw new EmailAlreadyExistsException("Email '" + user.getEmail() + "' is already registered.");
		}

		// collect activation info
		String activationCode = idGenerator.generateId(SECURE_RANDOM);
		List<String> activationEmailAddresses;
		if (appSettings.getSignUpModeration())
		{
			activationEmailAddresses = userService.getSuEmailAddresses();
			if (activationEmailAddresses == null || activationEmailAddresses.isEmpty())
				throw new MolgenisDataException("Administrator account is missing required email address");
		}
		else
		{
			String activationEmailAddress = user.getEmail();
			if (activationEmailAddress == null || activationEmailAddress.isEmpty())
				throw new MolgenisDataException("User '" + user.getUsername() + "' is missing required email address");
			activationEmailAddresses = asList(activationEmailAddress);
		}

		// create user
		user.setActivationCode(activationCode);
		user.setActive(false);
		dataService.add(USER, user);
		LOG.debug("created user " + user.getUsername());

		// add user to group
		Group group = dataService.query(GROUP, Group.class).eq(NAME, ALL_USER_GROUP).findOne();
		GroupMember groupMember = null;
		if (group != null)
		{
			groupMember = groupMemberFactory.create();
			groupMember.setGroup(group);
			groupMember.setUser(user);
			dataService.add(GROUP_MEMBER, groupMember);
		}

		// send activation email
		URI activationUri = URI.create(baseActivationUri + '/' + activationCode);

		try
		{
			SimpleMailMessage mailMessage = new SimpleMailMessage();
			mailMessage.setTo(activationEmailAddresses.toArray(new String[] {}));
			mailMessage.setSubject("User registration for " + appSettings.getTitle());
			mailMessage.setText(createActivationEmailText(user, activationUri));
			mailSender.send(mailMessage);
		}
		catch (MailException mce)
		{
			LOG.error("Could not send signup mail", mce);

			if (groupMember != null)
			{
				dataService.delete(GROUP_MEMBER, groupMember);
			}

			dataService.delete(USER, user);

			throw new MolgenisUserException(
					"An error occurred. Please contact the administrator. You are not signed up!");
		}
		LOG.debug("send activation email for user " + user.getUsername() + " to " + StringUtils.join(
				activationEmailAddresses, ','));

	}

	@Override
	@RunAsSystem
	public void activateUser(String activationCode)
	{
		User user = dataService.query(USER, User.class)
							   .eq(ACTIVE, false)
							   .and()
							   .eq(ACTIVATIONCODE, activationCode)
							   .findOne();

		if (user != null)
		{
			user.setActive(true);
			dataService.update(USER, user);

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
		User user = dataService.query(USER, User.class).eq(USERNAME, username).findOne();
		if (user == null)
		{
			throw new MolgenisUserException(format("Unknown user [%s]", username));
		}

		user.setPassword(newPassword);
		user.setChangePassword(false);
		dataService.update(USER, user);

		LOG.info("Changed password of user [{}]", username);
	}

	@Override
	@RunAsSystem
	public void resetPassword(String userEmail)
	{
		User user = dataService.query(USER, User.class).eq(EMAIL, userEmail).findOne();

		if (user != null)
		{
			String newPassword = idGenerator.generateId(SHORT_SECURE_RANDOM);
			user.setPassword(newPassword);
			user.setChangePassword(true);
			dataService.update(USER, user);

			// send password reseted email to user
			SimpleMailMessage mailMessage = new SimpleMailMessage();
			mailMessage.setTo(user.getEmail());
			mailMessage.setSubject("Your new password request");
			mailMessage.setText(createPasswordResettedEmailText(newPassword));
			mailSender.send(mailMessage);
		}
		else
		{
			throw new MolgenisUserException("Invalid email address.");
		}
	}

	private String createActivationEmailText(User user, URI activationUri)
	{
		String strBuilder =
				"User registration for " + appSettings.getTitle() + '\n' + "User name: " + user.getUsername()
						+ " Full name: " + user.getFirstName() + ' ' + user.getLastName() + '\n'
						+ "In order to activate the user visit the following URL:" + '\n' + activationUri + '\n' + '\n';
		return strBuilder;
	}

	private String createActivatedEmailText(User user, String appName)
	{
		String strBuilder =
				"Dear " + user.getFirstName() + " " + user.getLastName() + ",\n\n" + "your registration request for "
						+ appName + " was approved.\n" + "Your account is now active.\n";
		return strBuilder;
	}

	private String createPasswordResettedEmailText(String newPassword)
	{
		String strBuilder = "Somebody, probably you, requested a new password for " + appSettings.getTitle() + ".\n"
				+ "The new password is: " + newPassword + '\n'
				+ "Note: we strongly recommend you reset your password after log-in!";
		return strBuilder;
	}
}
