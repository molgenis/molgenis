package org.molgenis.ui.wizard;

import javax.servlet.http.HttpServletRequest;

import org.springframework.validation.BindingResult;

/**
 * Abstract wizard page.
 * 
 * The freemarker template name is the name simple name of the concrete wizard page class appended with '.ftl'
 * 
 * @author erwin
 * 
 */
public abstract class AbstractWizardPage implements WizardPage
{

	@Override
	public abstract String getTitle();

	/**
	 * Name of the freemarker template (including the ftl) that holds the piece of html for this wizard page that is
	 * pasted into the wizard html template
	 * 
	 * @return
	 */
	@Override
	public String getFreemarkerTemplateName()
	{
		return getClass().getSimpleName() + ".ftl";
	}

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
	@Override
	public abstract String handleRequest(HttpServletRequest request, BindingResult result, Wizard wizard);

}
