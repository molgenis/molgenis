package org.molgenis.omx.controller;

import java.util.ArrayList;
import java.util.List;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.validation.Valid;

import org.apache.log4j.Logger;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;
import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.molgenis.omx.auth.MolgenisUser;
import org.molgenis.security.core.utils.SecurityUtils;
import org.molgenis.security.user.MolgenisUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Controller that handles feedback page requests.
 */
@Controller
@RequestMapping(FeedbackController.URI)
public class FeedbackController extends MolgenisPluginController
{
	public static final String ID = "feedback";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;
	private static final Logger logger = Logger.getLogger(MolgenisPluginController.class);

	@Autowired
	private MolgenisUserService molgenisUserService;

	@Autowired
	private MolgenisSettings molgenisSettings;

	@Autowired
	private JavaMailSender mailSender;

	public FeedbackController()
	{
		super(URI);
	}

	/**
	 * Serves feedback form.
	 */
	@RequestMapping(method = RequestMethod.GET)
	public String init(final Model model)
	{
		model.addAttribute("adminEmails", molgenisUserService.getSuEmailAddresses());
		if (SecurityUtils.currentUserIsAuthenticated())
		{
			MolgenisUser currentUser = getCurrentUser();
			model.addAttribute("userName", getFormattedName(currentUser));
			model.addAttribute("userEmail", currentUser.getEmail());
		}
		return "view-feedback";
	}

	/**
	 * Handles feedback form submission.
	 * 
	 * @throws MessagingException
	 */
	@RequestMapping(method = RequestMethod.POST)
	public String submitFeedback(@Valid FeedbackForm form)
	{
		try
		{
			MimeMessage message = createFeedbackMessage(form);
			mailSender.send(message);
			form.setSubmitted(true);
		}
		catch (MessagingException e)
		{
			logger.warn("Unable to create mime message for feedback form.");
		}
		catch (MailAuthenticationException e)
		{

		}
		catch (MailSendException e)
		{

		}
		return "view-feedback";
	}

	/**
	 * Creates a MimeMessage based on a FeedbackForm.
	 * 
	 */
	private MimeMessage createFeedbackMessage(FeedbackForm form) throws MessagingException
	{
		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message, false);
		helper.setTo(molgenisUserService.getSuEmailAddresses().toArray(new String[0]));
		if (form.hasEmail())
		{
			helper.setCc(form.getEmail());
			helper.setReplyTo(form.getEmail());
		}
		else
		{
			helper.setReplyTo("no-reply@molgenis.org");
		}
		String appName = molgenisSettings.getProperty("app.name", "molgenis");
		helper.setSubject(String.format("[feedback-%s] %s", appName, form.getSubject()));
		if (form.hasName())
		{
			helper.setText(String.format("Feedback from %s:\n\n%s", form.getName(), form.getFeedback()));
		}
		else
		{
			helper.setText("Anonymous feedback:\n\n" + form.getFeedback());
		}
		return message;
	}

	private MolgenisUser getCurrentUser()
	{
		return molgenisUserService.getUser(SecurityUtils.getCurrentUsername());
	}

	/**
	 * Formats a MolgenisUser's name.
	 * 
	 * @return String containing the user's first name, middle names and last name.
	 */
	private static String getFormattedName(MolgenisUser user)
	{
		List<String> parts = new ArrayList<String>();
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
	 * Bean for the feedback form data. Allows for annotation-based validation.
	 */
	public static class FeedbackForm
	{
		private String name;
		private String email;
		private String subject;
		private String feedback;
		private boolean submitted;
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

	}
}
