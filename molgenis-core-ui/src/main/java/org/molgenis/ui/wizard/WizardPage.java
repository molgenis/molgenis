package org.molgenis.ui.wizard;

import javax.servlet.http.HttpServletRequest;

import org.springframework.validation.BindingResult;

public interface WizardPage
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
	 * 
	 * If you have an error add it to the BindingResult
	 * 
	 * Returns the successmessage, if you don't have a successmessage return null
	 * 
	 * @param request
	 * @return message to show the user (success message)
	 */
	String handleRequest(HttpServletRequest request, BindingResult result, Wizard wizard);

}
