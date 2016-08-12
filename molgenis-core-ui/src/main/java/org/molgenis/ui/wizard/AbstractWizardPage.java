package org.molgenis.ui.wizard;

import org.springframework.validation.BindingResult;

import javax.servlet.http.HttpServletRequest;

/**
 * Abstract wizard page.
 * <p>
 * The freemarker template name is the name simple name of the concrete wizard page class appended with '.ftl'
 *
 * @author erwin
 */
public abstract class AbstractWizardPage implements WizardPage
{
	private static final long serialVersionUID = 1L;

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
	 * <p>
	 * If you have an error add it to the BindingResult
	 * <p>
	 * Returns the successmessage, if you don't have a successmessage return null
	 *
	 * @param request
	 * @return message to show the user (success message)
	 */
	@Override
	public abstract String handleRequest(HttpServletRequest request, BindingResult result, Wizard wizard);

}
