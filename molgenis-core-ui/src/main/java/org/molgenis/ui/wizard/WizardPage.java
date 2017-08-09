package org.molgenis.ui.wizard;

import org.springframework.validation.BindingResult;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;

public interface WizardPage extends Serializable
{
	String getTitle();

	/**
	 * Name of the freemarker template (including the ftl) that holds the piece of html for this wizard page that is
	 * pasted into the wizard html template
	 *
	 * @return
	 */
	String getFreemarkerTemplateName();

	/**
	 * Handles this wizard pages business logic.
	 * <p>
	 * If you have an error add it to the BindingResult
	 * <p>
	 * Returns the successmessage, if you don't have a successmessage return null
	 *
	 * @param request
	 * @return message to show the user (success message)
	 */
	String handleRequest(HttpServletRequest request, BindingResult result, Wizard wizard);

}
