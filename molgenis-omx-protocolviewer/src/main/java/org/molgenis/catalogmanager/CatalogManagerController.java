package org.molgenis.catalogmanager;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.molgenis.catalog.Catalog;
import org.molgenis.catalog.CatalogMeta;
import org.molgenis.catalog.CatalogModel;
import org.molgenis.catalog.CatalogModelBuilder;
import org.molgenis.catalog.UnknownCatalogException;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
@RequestMapping(CatalogManagerController.URI)
public class CatalogManagerController extends MolgenisPluginController
{
	private static final Logger LOG = Logger.getLogger(CatalogManagerController.class);

	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + "catalogmanager";
	public static final String LOAD_LIST_URI = "/load-list";
	public static final String VIEW_NAME = "view-catalogmanager";

	private final CatalogManagerService catalogManagerService;

	@Autowired
	public CatalogManagerController(CatalogManagerService catalogManagerService)
	{
		super(URI);
		if (catalogManagerService == null) throw new IllegalArgumentException("Catalog manager service is null");
		this.catalogManagerService = catalogManagerService;
	}

	/**
	 * Show the available catalogs.
	 * 
	 * Catalogs are exposed via a 'catalogs' model attribute that contains a list of CatalogModel objects.
	 * 
	 * @param model
	 * @return
	 * @throws DatabaseException
	 * @throws UnknownCatalogException
	 */
	@RequestMapping(method = RequestMethod.GET)
	public String listCatalogs(Model model)
	{
		Iterable<CatalogMeta> catalogs = catalogManagerService.findCatalogs();
		LOG.debug("Got catalogs from service");

		List<CatalogMetaModel> models = new ArrayList<CatalogMetaModel>();
		for (CatalogMeta catalog : catalogs)
		{
			boolean catalogLoaded;
			try
			{
				catalogLoaded = catalogManagerService.isCatalogLoaded(catalog.getId());
			}
			catch (UnknownCatalogException e)
			{
				throw new RuntimeException(e);
			}
			models.add(new CatalogMetaModel(catalog.getId(), catalog.getName(), catalogLoaded));
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
	@RequestMapping(value = "/load", params = "load", method = RequestMethod.POST)
	public String loadCatalog(@RequestParam(value = "id", required = false)
	String id, Model model)
	{
		try
		{
			if (id != null)
			{
				if (!catalogManagerService.isCatalogLoaded(id))
				{
					catalogManagerService.loadCatalog(id);
					model.addAttribute("successMessage", "Catalog loaded");
					LOG.info("Loaded catalog with id [" + id + "]");
				}
				else
				{
					model.addAttribute("errorMessage", "Catalog already loaded");
				}
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

	@RequestMapping(value = "/load", params = "unload", method = RequestMethod.POST)
	public String unloadCatalog(@RequestParam(value = "id", required = false)
	String id, Model model)
	{
		try
		{
			if (id != null)
			{
				if (catalogManagerService.isCatalogLoaded(id))
				{
					catalogManagerService.unloadCatalog(id);
					model.addAttribute("successMessage", "Catalog unloaded");
					LOG.info("Unloaded catalog with id [" + id + "]");
				}
				else
				{
					model.addAttribute("errorMessage", "Catalog not loaded");
				}
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

	@RequestMapping(value = "/view/{id}", method = RequestMethod.GET)
	@ResponseBody
	public CatalogModel getCatalog(@PathVariable
	String id) throws UnknownCatalogException
	{
		Catalog catalog = catalogManagerService.getCatalog(id);
		return CatalogModelBuilder.create(catalog);
	}

	@ExceptionHandler(
	{ Exception.class, RuntimeException.class })
	public String handleException(Exception e, HttpServletRequest request)
	{
		LOG.error("An exception occured in the " + CatalogManagerController.class.getSimpleName(), e);

		request.setAttribute("errorMessage",
				"An error occured. Please contact the administrator.<br />Message:" + e.getMessage());

		return VIEW_NAME;
	}

	@ExceptionHandler(UnknownCatalogException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public void handleUnknownCatalogException()
	{
	}
}
