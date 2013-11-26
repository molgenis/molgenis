package org.molgenis.omx.controller;

import org.apache.log4j.Logger;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

public abstract class AbstractStaticContentController extends MolgenisPluginController
{
	private static final Logger logger = Logger.getLogger(AbstractStaticContentController.class);
	private static final String ERRORMESSAGE_PAGE = "There is an error occurred trying loading this page.";
	private static final String ERRORMESSAGE_SUBMIT = "There is an error occurred trying to save the content.";
	
	@Autowired
	private StaticContentService staticContentService;
	private final String uniqueReference;
	
	public AbstractStaticContentController(final String uniqueReference, final String uri)
	{
		super(uri);
		this.uniqueReference = uniqueReference;
	}

	@RequestMapping(method = RequestMethod.GET)
	public String init(final Model model)
	{
		try 
		{
			model.addAttribute("content", this.staticContentService.getContent(uniqueReference));
			model.addAttribute("isCurrentUserCanEdit", this.staticContentService.isCurrentUserCanEdit());
			model.addAttribute("editHref", "/menu/main/" + this.uniqueReference + "/edit");
		}
		catch (RuntimeException re) 
		{
			logger.error(re);
			model.addAttribute("errorMessage", ERRORMESSAGE_PAGE);	
		}
		
		return "view-staticcontent";
	}
	
	@PreAuthorize("hasAnyRole('ROLE_SU')")
	@RequestMapping(value = "/edit", method = RequestMethod.GET)
	public String initEditView(final Model model)
	{
		try 
		{
			model.addAttribute("content", this.staticContentService.getContent(this.uniqueReference));
			model.addAttribute("cancelHref", "/menu/main/" + this.uniqueReference);
		} catch (RuntimeException re) {
			logger.error(re);
			model.addAttribute("errorMessage", ERRORMESSAGE_PAGE);
		}
		
		return "view-staticcontent-edit";
	}
	
	@RequestMapping(value = "/edit", method = RequestMethod.POST)
	public String submitContent(@RequestParam(value = "content", required = true) final String content, final Model model)
	{
		try 
		{
			this.staticContentService.submitContent(this.uniqueReference, content);
		} 
		catch(RuntimeException re) {
			logger.error(re);
			model.addAttribute("errorMessage", ERRORMESSAGE_SUBMIT);	
		}
		return this.initEditView(model);
	}
}