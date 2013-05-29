package org.molgenis.lifelines.catalogue;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.omx.observ.Characteristic;
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
	public static final String LOAD_LIST_URI = "/load-list";
	public static final String LOAD_URI = "/load";
	public static final String VIEW_NAME = "catalog-loader";
	private static final Logger LOG = Logger.getLogger(CatalogLoaderController.class);
	private final CatalogLoaderService catalogLoaderService;
	private final Database database;

	@Autowired
	public CatalogLoaderController(CatalogLoaderService catalogLoaderService, Database database)
	{
		if (catalogLoaderService == null) throw new IllegalArgumentException("CatalogLoaderService is null");
		if (database == null) throw new IllegalArgumentException("Database id null");
		this.catalogLoaderService = catalogLoaderService;
		this.database = database;
	}

	/**
	 * Shows a loading spinner in the iframe and loads the catalogs list page
	 * 
	 * @param model
	 * @return
	 */
	@RequestMapping(LOAD_LIST_URI)
	public String showSpinner(Model model)
	{
		model.addAttribute("url", BASE_URL + LIST_URI);
		return "spinner";
	}

	/**
	 * Show the available catalogs.
	 * 
	 * Catalogs are exposed via a 'catalogs' model attribute that contains a list of CatalogModel objects.
	 * 
	 * @param model
	 * @return
	 * @throws DatabaseException
	 */
	@RequestMapping(LIST_URI)
	public String listCatalogs(Model model) throws DatabaseException
	{
		List<CatalogInfo> catalogs = catalogLoaderService.findCatalogs();
		LOG.debug("Got [" + catalogs.size() + "] catalogs from service");

		List<CatalogModel> models = new ArrayList<CatalogModel>(catalogs.size());
		for (CatalogInfo catalog : catalogs)
		{
			String identifier = CatalogIdConverter.catalogIdToOmxIdentifier(catalog.getId());
			Characteristic dataset = Characteristic.findByIdentifier(database, identifier);
			boolean catalogLoaded = dataset != null;
			models.add(new CatalogModel(catalog.getId(), catalog.getName(), catalogLoaded));
		}

		model.addAttribute("catalogs", models);

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
	 * @throws DatabaseException
	 */
	@RequestMapping(LOAD_URI)
	public String loadCatalog(@RequestParam(value = "id", required = false)
	String id, Model model) throws DatabaseException
	{
		try
		{
			if (id != null)
			{
				catalogLoaderService.loadCatalog(id);
				model.addAttribute("successMessage", "Catalog loaded");
				LOG.info("Loaded catalog with id [" + id + "]");
			}
			else
			{
				model.addAttribute("errorMessage", "Please select a catalogue");
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
