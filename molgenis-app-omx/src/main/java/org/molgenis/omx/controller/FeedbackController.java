package org.molgenis.omx.controller;

import java.util.ArrayList;
import java.util.List;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

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
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller that handles feedback page requests.
 */
@Controller
@RequestMapping(FeedbackController.URI)
public class FeedbackController extends MolgenisPluginController
{
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
	public String submitFeedback(@RequestParam(value = "form[name]") final String name,
			@RequestParam(value = "form[subject]") final String subject,
			@RequestParam(value = "form[email]") final String email,
			@RequestParam(value = "form[comments]", required = true) final String comments, final Model model)
			throws MessagingException
	{
		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message, false);
		helper.setTo(molgenisUserService.getSuEmailAddresses().toArray(new String[0]));
		if (email != null)
		{
			helper.setCc(email);
			helper.setReplyTo(email);
		}
		helper.setSubject(getSubject(name, subject));
		helper.setText(comments);
		mailSender.send(message);
		model.addAttribute("submitted", Boolean.TRUE);
		return "view-feedback";
	}

	private String getSubject(final String name, String subject) throws MessagingException
	{
		String appName = molgenisSettings.getProperty("app.name");
		if (appName == null)
		{
			appName = "molgenis";
		}
		if (subject == null)
		{
			subject = "<no subject>";
		}
		return String.format("[feedback-%s] %s", appName, subject);
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
