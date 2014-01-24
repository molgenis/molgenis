package org.molgenis.omx.controller;

import java.io.File;
import java.io.IOException;

import javax.servlet.http.Part;

import org.apache.log4j.Logger;
import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.molgenis.ui.XmlMolgenisUi;
import org.molgenis.util.FileStore;
import org.molgenis.util.FileUploadUtils;
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
	private static final String ERRORMESSAGE_LOGO = "The logo needs to be an image file like png or jpg.";

	@Autowired
	private StaticContentService staticContentService;

	@Autowired
	private FileStore fileStore;

	@Autowired
	private MolgenisSettings molgenisSettings;

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
		}
		catch (RuntimeException re)
		{
			logger.error(re);
			model.addAttribute("errorMessage", ERRORMESSAGE_PAGE);
		}

		return "view-staticcontent-edit";
	}

	@RequestMapping(value = "/edit", method = RequestMethod.POST)
	public String submitContent(@RequestParam(value = "content", required = true)
	final String content, final Model model)
	{
		try
		{
			this.staticContentService.submitContent(this.uniqueReference, content);
		}
		catch (RuntimeException re)
		{
			logger.error(re);
			model.addAttribute("errorMessage", ERRORMESSAGE_SUBMIT);
		}
		return this.initEditView(model);
	}

	/**
	 * Upload a new molgenis logo
	 * 
	 * @param part
	 * @param model
	 * @return
	 * @throws IOException
	 */
	@PreAuthorize("hasAnyRole('ROLE_SU')")
	@RequestMapping(value = "/upload-logo", method = RequestMethod.POST)
	public String uploadLogo(@RequestParam("logo")
	Part part, Model model) throws IOException
	{
		String contentType = part.getContentType();
		if ((contentType == null) || !contentType.startsWith("image"))
		{
			model.addAttribute("errorMessage", ERRORMESSAGE_LOGO);
		}
		else
		{

			// Create the logo subdir in the filestore if it doesn't exist
			File logoDir = new File(fileStore.getStorageDir() + "/logo");
			if (!logoDir.exists())
			{
				if (!logoDir.mkdir())
				{
					throw new IOException("Unable to create directory [" + logoDir.getAbsolutePath() + "]");
				}
			}

			// Store the logo in the logo dir of the filestore
			String file = "/logo/" + FileUploadUtils.getOriginalFileName(part);
			fileStore.store(part.getInputStream(), file);

			// Update logo RuntimeProperty
			molgenisSettings.setProperty(XmlMolgenisUi.KEY_APP_HREF_LOGO, file);
		}

		return init(model);
	}
}