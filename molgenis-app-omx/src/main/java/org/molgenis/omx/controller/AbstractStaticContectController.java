package org.molgenis.omx.controller;

import org.molgenis.framework.ui.MolgenisPluginController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

public abstract class AbstractStaticContectController extends MolgenisPluginController
{
	@Autowired
	private StaticContentService staticContentService;
	private final String uniqueReference;
	
	public AbstractStaticContectController(final String uniqueReference, final String uri)
	{
		super(uri);
		this.uniqueReference = uniqueReference;
	}

	@RequestMapping(method = RequestMethod.GET)
	public String init(final Model model)
	{
		return this.initModelAndView(this.uniqueReference, model);
	}
	
	@RequestMapping(value = "/edit", method = RequestMethod.GET)
	public String initEdit(final Model model)
	{
		if(this.staticContentService.isCurrentUserAuthenticatedSu()){
			return this.initEditModelAndView(uniqueReference, model);
		}else{
			return this.initModelAndView(uniqueReference, model);
		}
	}
	
	@RequestMapping(value = "/edit", method = RequestMethod.POST)
	public String submitContent(@RequestParam(value = "content", required = true) final String content, final Model model)
	{
		this.staticContentService.submitContent(this.uniqueReference, content);
		return this.initEditModelAndView(this.uniqueReference, model);
	}
	
	private String initModelAndView(final String uniqueReference, final Model model)
	{
		model.addAttribute("content", this.staticContentService.getContent(uniqueReference));
		model.addAttribute("isCurrentUserAuthenticatedSu", this.staticContentService.isCurrentUserAuthenticatedSu());
		model.addAttribute("editHref", "/menu/main/" + uniqueReference + "/edit");
		return "view-staticcontent";
	}
	
	@PreAuthorize("hasAnyRole('ROLE_SU')")
	private String initEditModelAndView(final String uniqueReference, final Model model)
	{
		model.addAttribute("content", this.staticContentService.getContent(uniqueReference));
		model.addAttribute("cancelHref", "/menu/main/" + uniqueReference);
		return "view-staticcontent-edit";
	}
}