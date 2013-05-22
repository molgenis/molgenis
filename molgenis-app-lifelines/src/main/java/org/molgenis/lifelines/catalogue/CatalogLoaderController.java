package org.molgenis.lifelines.catalogue;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller for showing available catalogs and loading a specific catalog
 * 
 * @author erwin
 * 
 */
@Controller
@RequestMapping(CatalogLoaderController.BASE_URL)
public class CatalogLoaderController
{
	public static final String BASE_URL = "/plugin/catalog";
	public static final String LIST_URI = "/list";
	public static final String LOAD_URI = "/load";
	public static final String VIEW_NAME = "catalog-loader";
	private static final Logger LOG = Logger.getLogger(CatalogLoaderController.class);
	private final CatalogLoaderService catalogLoaderService;

	@Autowired
	public CatalogLoaderController(CatalogLoaderService catalogLoaderService)
	{
		if (catalogLoaderService == null) throw new IllegalArgumentException("CatalogLoaderService is null");
		this.catalogLoaderService = catalogLoaderService;
	}

	/**
	 * Show the available catalogs.
	 * 
	 * Catalogs are exposed via a 'catalogs' model attribute that contains a list of CatalogInfo objects.
	 * 
	 * @param model
	 * @return
	 */
	@RequestMapping(LIST_URI)
	public String listCatalogs(Model model)
	{
		List<CatalogInfo> catalogs = catalogLoaderService.findCatalogs();
		LOG.debug("Got [" + catalogs.size() + "] catalogs from service");

		model.addAttribute("catalogs", catalogs);

		return VIEW_NAME;
	}

	/**
	 * Loads a catalog by it's id.
	 * 
	 * If an error occurred an 'errorMessage' model attribute is exposed.
	 * 
	 * If the catalog was successfully loaded a 'successMessage' model attribute is exposed.
	 * 
	 * @param id
	 * @param model
	 * @return
	 */
	@RequestMapping(LOAD_URI)
	public String loadCatalog(@RequestParam("id")
	String id, Model model)
	{
		try
		{
			if (id != null)
			{
				catalogLoaderService.loadCatalog(id);
				model.addAttribute("successMessage", "Catalog loaded");
				LOG.info("Loaded catalog with id [" + id + "]");
			}
		}
		catch (UnknownCatalogException e)
		{
			model.addAttribute("errorMessage", e.getMessage());
		}

		return listCatalogs(model);
	}

	@ExceptionHandler(Exception.class)
	public String handleException(Exception e, HttpServletRequest request)
	{
		LOG.error("An exception occured in the CatalogLoaderController", e);

		request.setAttribute("errorMessage",
				"An error occured. Please contact the administrator.<br />Message:" + e.getMessage());

		return VIEW_NAME;
	}
}
