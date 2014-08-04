package org.molgenis.omx.controller;

import java.util.ArrayList;
import java.util.List;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.validation.Valid;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;
import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.molgenis.omx.auth.MolgenisUser;
import org.molgenis.security.core.utils.SecurityUtils;
import org.molgenis.security.user.MolgenisUserService;
import org.springframework.beans.factory.annotation.Autowired;
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
	public static class FeedbackForm
	{
		private String name;
		private String email;
		private String subject;
		private String feedback;
		private boolean submitted;

		public String getName()
		{
			return name;
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

		public String getSubject()
		{
			if (subject == null)
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
	}

	public static final String ID = "feedback";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;

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
	public String submitFeedback(@Valid FeedbackForm form) throws MessagingException
	{

		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message, false);
		helper.setTo(molgenisUserService.getSuEmailAddresses().toArray(new String[0]));
		if (form.getEmail() != null)
		{
			helper.setCc(form.getEmail());
			helper.setReplyTo(form.getEmail());
		}
		String subject = String.format("[feedback-%s] %s", 
				molgenisSettings.getProperty("app.name", "molgenis"),
				form.getSubject());
		helper.setSubject(subject);
		helper.setText(form.getFeedback());
		mailSender.send(message);
		form.setSubmitted(true);
		return "view-feedback";
	}

	private MolgenisUser getCurrentUser()
	{
		return molgenisUserService.getUser(SecurityUtils.getCurrentUsername());
	}

	private static String getFormattedName(MolgenisUser user)
	{
		List<String> parts = new ArrayList<String>();
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
}
