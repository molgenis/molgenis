package org.molgenis.core.ui.controller;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;
import org.molgenis.data.security.auth.User;
import org.molgenis.data.security.user.UserService;
import org.molgenis.security.captcha.CaptchaException;
import org.molgenis.security.captcha.CaptchaRequest;
import org.molgenis.security.captcha.CaptchaService;
import org.molgenis.security.core.utils.SecurityUtils;
import org.molgenis.settings.AppSettings;
import org.molgenis.web.PluginController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * Controller that handles feedback page requests. The user can fill in this feedback form to send a mail to the app's
 * administrators.
 */
@Controller
@RequestMapping(FeedbackController.URI)
public class FeedbackController extends AbstractStaticContentController
{
	private static final Logger LOG = LoggerFactory.getLogger(FeedbackController.class);

	public static final String ID = "feedback";
	public static final String URI = PluginController.PLUGIN_URI_PREFIX + ID;
	private static final String MAIL_AUTHENTICATION_EXCEPTION_MESSAGE = "Unfortunately, we were unable to send the mail containing your feedback. Please contact the administrator.";
	private static final String MAIL_SEND_EXCEPTION_MESSAGE = MAIL_AUTHENTICATION_EXCEPTION_MESSAGE;

	private final UserService userService;
	private final AppSettings appSettings;
	private final CaptchaService captchaService;
	private final MailSender mailSender;

	public FeedbackController(UserService userService, AppSettings appSettings, CaptchaService captchaService,
			MailSender mailSender)
	{
		super(ID, URI);
		this.userService = requireNonNull(userService);
		this.appSettings = requireNonNull(appSettings);
		this.captchaService = requireNonNull(captchaService);
		this.mailSender = requireNonNull(mailSender);
	}

	/**
	 * Serves feedback form.
	 */
	@Override
	@GetMapping
	public String init(final Model model)
	{
		super.init(model);
		model.addAttribute("adminEmails", userService.getSuEmailAddresses());
		if (SecurityUtils.currentUserIsAuthenticated())
		{
			User currentUser = userService.getUser(SecurityUtils.getCurrentUsername());
			model.addAttribute("userName", getFormattedName(currentUser));
			model.addAttribute("userEmail", currentUser.getEmail());
		}
		return "view-feedback";
	}

	/**
	 * Handles feedback form submission.
	 *
	 * @throws CaptchaException if no valid captcha is supplied
	 */
	@PostMapping
	public String submitFeedback(@Valid FeedbackForm form, @Valid @ModelAttribute CaptchaRequest captchaRequest)
			throws CaptchaException
	{
		if (!captchaService.validateCaptcha(captchaRequest.getCaptcha()))
		{
			form.setErrorMessage("Invalid captcha.");
			return "view-feedback";
		}
		try
		{
			LOG.info("Sending feedback:" + form);
			SimpleMailMessage message = createFeedbackMessage(form);
			mailSender.send(message);
			form.setSubmitted(true);
			captchaService.removeCaptcha();
		}
		catch (MailAuthenticationException e)
		{
			LOG.error("Error authenticating with email server.", e);
			form.setErrorMessage(MAIL_AUTHENTICATION_EXCEPTION_MESSAGE);
		}
		catch (MailSendException e)
		{
			LOG.error("Error sending mail", e);
			form.setErrorMessage(MAIL_SEND_EXCEPTION_MESSAGE);
		}
		return "view-feedback";
	}

	/**
	 * Creates a MimeMessage based on a FeedbackForm.
	 */
	private SimpleMailMessage createFeedbackMessage(FeedbackForm form)
	{
		SimpleMailMessage message = new SimpleMailMessage();
		message.setTo(userService.getSuEmailAddresses().toArray(new String[] {}));
		if (form.hasEmail())
		{
			message.setCc(form.getEmail());
			message.setReplyTo(form.getEmail());
		}
		else
		{
			message.setReplyTo("no-reply@molgenis.org");
		}
		String appName = appSettings.getTitle();
		message.setSubject(String.format("[feedback-%s] %s", appName, form.getSubject()));
		message.setText(String.format("Feedback from %s:\n\n%s", form.getFrom(), form.getFeedback()));
		return message;
	}

	/**
	 * Formats a MolgenisUser's name.
	 *
	 * @return String containing the user's first name, middle names and last name.
	 */
	private static String getFormattedName(User user)
	{
		List<String> parts = new ArrayList<>();
		if (user.getTitle() != null)
		{
			parts.add(user.getTitle());
		}
		if (user.getFirstName() != null)
		{
			parts.add(user.getFirstName());
		}
		if (user.getMiddleNames() != null)
		{
			parts.add(user.getMiddleNames());
		}
		if (user.getLastName() != null)
		{
			parts.add(user.getLastName());
		}

		if (parts.isEmpty())
		{
			return null;
		}
		else
		{
			return StringUtils.collectionToDelimitedString(parts, " ");
		}
	}

	/**
	 * Bean for the feedback form data.
	 */
	public static class FeedbackForm
	{
		private String name;
		private String email;
		private String subject;
		private String feedback;
		private boolean submitted = false;
		private String errorMessage;

		public String getName()
		{
			return name;
		}

		public boolean hasName()
		{
			return name != null && !name.trim().isEmpty();
		}

		public void setName(String name)
		{
			this.name = name;
		}

		@Email
		public String getEmail()
		{
			return email;
		}

		public void setEmail(String email)
		{
			this.email = email;
		}

		public boolean hasEmail()
		{
			return email != null && !email.trim().isEmpty();
		}

		public String getFrom()
		{
			StringBuilder result = new StringBuilder();
			if (!hasName() && !hasEmail())
			{
				result.append("Anonymous");
			}
			else
			{
				if (hasName())
				{
					result.append(getName());
				}
				if (hasEmail())
				{
					if (hasName())
					{
						result.append(String.format(" (%s)", getEmail()));
					}
					else
					{
						result.append(getEmail());
					}
				}
			}
			return result.toString();
		}

		public String getSubject()
		{
			if (subject == null || subject.trim().isEmpty())
			{
				return "<no subject>";
			}
			return subject;
		}

		public void setSubject(String subject)
		{
			this.subject = subject;
		}

		@NotBlank
		public String getFeedback()
		{
			return feedback;
		}

		public void setFeedback(String feedback)
		{
			this.feedback = feedback;
		}

		public void setSubmitted(boolean b)
		{
			this.submitted = b;
		}

		public boolean isSubmitted()
		{
			return submitted;
		}

		public String getErrorMessage()
		{
			return errorMessage;
		}

		public void setErrorMessage(String errorMessage)
		{
			this.errorMessage = errorMessage;
		}

		@Override
		public String toString()
		{
			String builder = "[From: " + getFrom() + "\nSubject: " + getSubject() + "\nBody: " + getFeedback() + ']';
			return builder;
		}
	}
}
