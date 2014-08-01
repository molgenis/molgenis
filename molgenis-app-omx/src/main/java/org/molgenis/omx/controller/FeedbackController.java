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
		return "view-feedback";
	}
	
	/**
	 * Handles feedback form submission.
	 * @throws MessagingException 
	 */
	@RequestMapping(method = RequestMethod.POST)
	public String submitFeedback(final Model model) throws MessagingException
	{
		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message, true);
		helper.setTo(molgenisUserService.getSuEmailAddresses().toArray(new String[0]));
		//helper.setCc("user's email address");
//		helper.setSubject("Submission confirmation from " + appName);
//		helper.setText(createOrderConfirmationEmailText(appName));
//		helper.addAttachment(fileName, new FileSystemResource(orderFile));
//		helper.addAttachment(variablesFileName, new FileSystemResource(variablesFile));
		mailSender.send(message);
		model.addAttribute("submitted", Boolean.TRUE);
		return "view-feedback";
	}
}
