package org.molgenis.omx.controller;

import org.molgenis.framework.ui.MolgenisPluginController;
import org.springframework.beans.factory.annotation.Autowired;
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
		return this.staticContentService.init(this.uniqueReference, model);
	}
	
	@RequestMapping(value = "/edit", method = RequestMethod.GET)
	public String initEdit(final Model model)
	{
		return this.staticContentService.initEdit(this.uniqueReference, model);
	}
	
	@RequestMapping(value = "/edit", method = RequestMethod.POST)
	public String submitContent(@RequestParam(value = "content", required = true) final String content, final Model model)
	{
		this.staticContentService.submitContent(this.uniqueReference, content);
		return this.staticContentService.initEdit(this.uniqueReference, model);
	}
}