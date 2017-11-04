package org.molgenis.security.account;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.populate.IdGenerator;
import org.molgenis.data.settings.AppSettings;
import org.molgenis.security.core.model.User;
import org.molgenis.security.core.runas.RunAsSystem;
import org.molgenis.security.core.service.UserService;
import org.molgenis.security.core.service.exception.EmailAlreadyExistsException;
import org.molgenis.security.core.service.exception.MolgenisUserException;
import org.molgenis.security.core.service.exception.UsernameAlreadyExistsException;
import org.molgenis.security.settings.AuthenticationSettings;
import org.molgenis.util.CountryCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.text.MessageFormat;
import java.util.List;

import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.molgenis.data.populate.IdGenerator.Strategy.SECURE_RANDOM;
import static org.molgenis.data.populate.IdGenerator.Strategy.SHORT_SECURE_RANDOM;

@Service
public class AccountServiceImpl implements AccountService
{
	private static final Logger LOG = LoggerFactory.getLogger(AccountServiceImpl.class);

	private final MailSender mailSender;
	private final UserService userService;
	private final AppSettings appSettings;
	private final AuthenticationSettings authenticationSettings;
	private final IdGenerator idGenerator;

	public AccountServiceImpl(MailSender mailSender, UserService userService, AppSettings appSettings,
			AuthenticationSettings authenticationSettings, IdGenerator idGenerator)
	{
		this.mailSender = requireNonNull(mailSender);
		this.userService = requireNonNull(userService);
		this.appSettings = requireNonNull(appSettings);
		this.authenticationSettings = requireNonNull(authenticationSettings);
		this.idGenerator = requireNonNull(idGenerator);
	}

	@Override
	@RunAsSystem
	@Transactional
	public User register(RegisterRequest registerRequest, String baseActivationUri)
			throws UsernameAlreadyExistsException, EmailAlreadyExistsException
	{
		String activationCode = idGenerator.generateId(SECURE_RANDOM);

		// create user
		User user = toUser(registerRequest, activationCode);
		userService.add(user);
		LOG.debug("created user " + user.getUsername());

		// collect activation info
		List<String> activationEmailAddresses;
		if (authenticationSettings.getSignUpModeration())
		{
			activationEmailAddresses = userService.getSuEmailAddresses();
			if (activationEmailAddresses == null || activationEmailAddresses.isEmpty())
			{
				throw new MolgenisDataException("Administrator account is missing required email address");
			}
		}
		else
		{
			String activationEmailAddress = user.getEmail();
			if (StringUtils.isBlank(activationEmailAddress))
			{
				throw new MolgenisDataException(
						MessageFormat.format("User ''{0}'' is missing required email address", user.getUsername()));
			}
			activationEmailAddresses = singletonList(activationEmailAddress);
		}

		// send activation email
		URI activationUri = URI.create(baseActivationUri + '/' + activationCode);

		try
		{
			SimpleMailMessage mailMessage = new SimpleMailMessage();
			mailMessage.setTo(activationEmailAddresses.toArray(new String[] {}));
			mailMessage.setSubject("User registration for " + appSettings.getTitle());
			mailMessage.setText(createActivationEmailText(user, activationUri));
			LOG.debug("send activation email for user {} to {}", user.getUsername(), activationEmailAddresses);
			mailSender.send(mailMessage);
		}
		catch (MailException mce)
		{
			LOG.error("Could not send signup mail", mce);
			throw new MolgenisUserException(
					"An error occurred. Please contact the administrator. You are not signed up!");
		}

		return user;
	}

	private User toUser(RegisterRequest request, String activationCode)
	{
		User.Builder result = User.builder()
								  .username(request.getUsername())
								  .email(request.getEmail())
								  .password(request.getPassword())
								  .activationCode(activationCode)
								  .active(false)
								  .changePassword(false)
								  .superuser(false);
		if (isNotBlank(request.getFirstname()))
		{
			result.firstName(request.getFirstname());
		}
		if (isNotBlank(request.getLastname()))
		{
			result.lastName(request.getLastname());
		}
		if (isNotBlank(request.getPhone()))
		{
			result.phone(request.getPhone());
		}
		if (isNotBlank(request.getFax()))
		{
			result.fax(request.getFax());
		}
		if (isNotBlank(request.getTollFreePhone()))
		{
			result.tollFreePhone(request.getTollFreePhone());
		}
		if (isNotBlank(request.getAddress()))
		{
			result.address(request.getAddress());
		}
		if (isNotBlank(request.getTitle()))
		{
			result.title(request.getTitle());
		}
		if (isNotBlank(request.getLastname()))
		{
			result.lastName(request.getLastname());
		}
		if (isNotBlank(request.getDepartment()))
		{
			result.department(request.getDepartment());
		}
		if (isNotBlank(request.getCity()))
		{
			result.city(request.getCity());
		}
		if (isNotBlank(request.getCountry()))
		{
			String country = CountryCodes.get(request.getCountry());
			if (country != null)
			{
				result.country(country);
			}
		}
		return result.build();
	}

	@Override
	@RunAsSystem
	public void activateUser(String activationCode)
	{
		User user = userService.activateUserUsingCode(activationCode)
							   .orElseThrow(() -> new MolgenisUserException(
									   "Invalid activation code or account already activated."));
		// send activated email to user
		SimpleMailMessage mailMessage = new SimpleMailMessage();
		mailMessage.setTo(user.getEmail());
		mailMessage.setSubject("Your registration request for " + appSettings.getTitle());
		mailMessage.setText(createActivatedEmailText(user, appSettings.getTitle()));
		mailSender.send(mailMessage);
	}

	@Override
	@RunAsSystem
	public void changePassword(String username, String newPassword)
	{
		userService.update(
				userService.findByUsername(username).toBuilder().password(newPassword).changePassword(false).build());
		LOG.info("Changed password of user [{}]", username);
	}

	@Override
	@RunAsSystem
	public void resetPassword(String email)
	{
		String newPassword = idGenerator.generateId(SHORT_SECURE_RANDOM);
		User user = userService.findByEmail(email);
		User updated = user.toBuilder().password(newPassword).changePassword(true).build();
		userService.update(updated);

		// send password reset email to user
		SimpleMailMessage mailMessage = new SimpleMailMessage();
		mailMessage.setTo(user.getEmail());
		mailMessage.setSubject("Your new password request");
		mailMessage.setText(createPasswordResettedEmailText(newPassword));
		mailSender.send(mailMessage);
		LOG.info("Reset password of user [{}]", user.getUsername());
	}

	private String createActivationEmailText(User user, URI activationUri)
	{
		return MessageFormat.format("User registration for {0}\nUser name: {1} Full name: {2}\n"
						+ "In order to activate the user visit the following URL:\n{3}\n\n", appSettings.getTitle(),
				user.getUsername(), user.getFormattedName(), activationUri);
	}

	private String createActivatedEmailText(User user, String appName)
	{
		return MessageFormat.format(
				"Dear {0},\n\nyour registration request for {1} was approved.\nYour account is now active.\n",
				user.getFormattedName(), appName);
	}

	private String createPasswordResettedEmailText(String newPassword)
	{
		return MessageFormat.format(
				"Somebody, probably you, requested a new password for {0}.\nThe new password is: {1}\n"
						+ "Note: we strongly recommend you reset your password after log-in!", appSettings.getTitle(),
				newPassword);
	}
}
