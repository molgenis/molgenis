package org.molgenis.core.ui.controller;

import org.molgenis.web.PluginController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

public abstract class AbstractStaticContentController extends PluginController
{
	private static final Logger LOG = LoggerFactory.getLogger(AbstractStaticContentController.class);

	private static final String ERRORMESSAGE_PAGE = "An error occurred trying loading this page.";
	private static final String ERRORMESSAGE_SUBMIT = "An error occurred trying to save the content.";

	@Autowired
	private StaticContentService staticContentService;

	private final String uniqueReference;

	public AbstractStaticContentController(final String uniqueReference, final String uri)
	{
		super(uri);
		this.uniqueReference = uniqueReference;
	}

	@GetMapping
	public String init(final Model model)
	{
		try
		{
			model.addAttribute("content", this.staticContentService.getContent(uniqueReference));
			model.addAttribute("isCurrentUserCanEdit", this.staticContentService.isCurrentUserCanEdit(uniqueReference));
		}
		catch (RuntimeException re)
		{
			LOG.error("", re);
			model.addAttribute("errorMessage", ERRORMESSAGE_PAGE);
		}

		return "view-staticcontent";
	}

	@GetMapping("/edit")
	public String initEditView(final Model model)
	{
		this.staticContentService.checkPermissions(this.uniqueReference);
		try
		{
			model.addAttribute("content", this.staticContentService.getContent(this.uniqueReference));
		}
		catch (RuntimeException re)
		{
			LOG.error("", re);
			model.addAttribute("errorMessage", ERRORMESSAGE_PAGE);
		}

		return "view-staticcontent-edit";
	}

	@PostMapping("/edit")
	public String submitContent(@RequestParam(value = "content") final String content, final Model model)
	{
		this.staticContentService.checkPermissions(this.uniqueReference);
		try
		{
			this.staticContentService.submitContent(this.uniqueReference, content);
		}
		catch (RuntimeException re)
		{
			LOG.error("", re);
			model.addAttribute("errorMessage", ERRORMESSAGE_SUBMIT);
		}
		return this.initEditView(model);
	}
}