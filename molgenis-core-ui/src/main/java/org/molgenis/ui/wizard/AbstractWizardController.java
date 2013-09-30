package org.molgenis.ui.wizard;

import static org.molgenis.ui.MolgenisPluginAttributes.KEY_CONTEXT_URL;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.molgenis.framework.ui.MolgenisPluginController;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

public abstract class AbstractWizardController extends MolgenisPluginController
{
	private static final String VIEW_NAME = "view-wizard";
	private final String wizardName;

	public AbstractWizardController(String uri, String wizardName)
	{
		super(uri);
		this.wizardName = wizardName;
	}

	protected abstract Wizard createWizard();

	@ModelAttribute("javascripts")
	public List<String> getJavascripts()
	{
		return Collections.emptyList();
	}

	@ModelAttribute("stylesheets")
	public List<String> getStylesheets()
	{
		return Collections.emptyList();
	}

	@ModelAttribute("wizard")
	public Wizard wizard(HttpSession session)
	{
		Wizard wizard = (Wizard) session.getAttribute(wizardName);
		if (wizard == null)
		{
			wizard = createWizard();
			session.setAttribute(wizardName, wizard);
		}

		return wizard;
	}

	@RequestMapping(value = "/**", method = GET)
	public String init()
	{
		return VIEW_NAME;
	}

	@RequestMapping(value = "/next", method = POST)
	public String next(HttpServletRequest request, HttpServletResponse response, @ModelAttribute("wizard")
	Wizard wizard, BindingResult result, Model model) throws IOException
	{
		if (wizard.isLastPage())
		{
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return null;
		}

		WizardPage page = wizard.getCurrentPage();
		String message = page.handleRequest(request, result, wizard);
		if (!result.hasErrors())
		{
			model.addAttribute("successMessage", message);
			wizard.next();
		}

		return VIEW_NAME;
	}

	@RequestMapping(value = "/previous", method = POST)
	public String previous(HttpServletResponse response, @ModelAttribute("wizard")
	Wizard wizard) throws IOException
	{
		if (wizard.isFirstPage())
		{
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return null;
		}

		wizard.previous();

		return VIEW_NAME;
	}

	@RequestMapping(value = "/restart")
	public String restart(HttpServletRequest request, HttpSession session)
	{
		session.removeAttribute(wizardName);
		return "redirect:" + request.getAttribute(KEY_CONTEXT_URL);
	}
}
