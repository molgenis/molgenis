package org.molgenis.omx.controller;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.molgenis.framework.ui.MolgenisPluginController;
import org.molgenis.security.user.MolgenisUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller that handles feedback page requests.
 */
@Controller
@RequestMapping(FeedbackController.URI)
public class FeedbackController extends MolgenisPluginController {
	public static final String ID = "feedback";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX
			+ ID;

	@Autowired
	private MolgenisUserService molgenisUserService;

	@Autowired
	private JavaMailSender mailSender;

	public FeedbackController() {
		super(URI);
	}

	/**
	 * Serves feedback form.
	 */
	@RequestMapping(method = RequestMethod.GET)
	public String init(final Model model) {
		model.addAttribute("adminEmails",
				molgenisUserService.getSuEmailAddresses());
		return "view-feedback";
	}

	/**
	 * Handles feedback form submission.
	 * 
	 * @throws MessagingException
	 */
	@RequestMapping(method = RequestMethod.POST)
	public String submitFeedback(
			@RequestParam(value = "form[name]") final String name,
			@RequestParam(value = "form[subject]") final String subject,
			@RequestParam(value = "form[email]") final String email,
			@RequestParam(value = "form[comments]", required = true) final String comments,
			final Model model) throws MessagingException {
		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message, false);
		helper.setTo(molgenisUserService.getSuEmailAddresses().toArray(
				new String[0]));
		if( email != null ) {
			helper.setCc(email);
			helper.setReplyTo(email);
		}
		if( subject != null ) {
			helper.setSubject("[feedback-molgenis] "+subject);
		} else {
			helper.setSubject("[feedback-molgenis] Feedback from "+ name);
		}
		helper.setText(comments);
		mailSender.send(message);
		model.addAttribute("submitted", Boolean.TRUE);
		return "view-feedback";
	}
}
