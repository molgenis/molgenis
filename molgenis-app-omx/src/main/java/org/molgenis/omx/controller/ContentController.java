package org.molgenis.omx.controller;

import static org.molgenis.omx.controller.ContentController.URI;

import org.apache.log4j.Logger;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Controller that handles static content pages requests
 * 
 * RuntimeProperty_[KeyApp] is the way an identifier is made
 */
@Controller
@RequestMapping(URI)
public class ContentController extends MolgenisPluginController
{
	private final static Logger logger = Logger.getLogger(ContentController.class);

	/**
	 * TODO before ending this implementation
	 * 
	 * REMOVE: BackgroundController.java; NewsController.java; HomeController.java; ContactController.java; MORE???
	 */
	public static final String DEFAULT_CONTENT = "<p>Place here some content!</p>";
	public static final String URI = "/plugin/content";
	public static final String PREFIX_KEY = "app.";

	private final MolgenisSettings molgenisSettings;

	@Autowired
	public ContentController(final MolgenisSettings molgenisSettings)
	{
		this(molgenisSettings, URI);
	}
	
	public ContentController(final MolgenisSettings molgenisSettings, String uri)
	{
		super(uri);
		if (molgenisSettings == null)
		{
			throw new IllegalArgumentException("molgenisSettings is null");
		}
		this.molgenisSettings = molgenisSettings;
	}
	
	@RequestMapping(value = "/background", method = RequestMethod.GET)
	public String init(final Model model)
	{
		return this.init("background", model);
	}
	
	@RequestMapping(value = "/contact", method = RequestMethod.GET)
	public String initContact(final Model model)
	{
		return this.init("contact", model);
	}

	@RequestMapping(value = "/home", method = RequestMethod.GET)
	public String initHome(final Model model)
	{
		return this.init("home", model);
	}
	
	@RequestMapping(value = "/news", method = RequestMethod.GET)
	public String initNews(final Model model)
	{
		return this.init("news", model);
	}
	
	@RequestMapping(value = "/references", method = RequestMethod.GET)
	public String initReferences(final Model model)
	{
		return this.init("references", model);
	}
	
	protected String init(final String uniqueReference, final Model model){
		String content = this.molgenisSettings.getProperty(PREFIX_KEY + uniqueReference, DEFAULT_CONTENT);
	
		if (null == content || content.isEmpty())
		{
			throw new MolgenisDataException("content is null or empty");
		}
		else
		{
			model.addAttribute("content", content);
		}
		 
		return "view-staticcontent";
	}
}
