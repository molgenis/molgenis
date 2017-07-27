package org.molgenis.model.registry.controller;

import com.google.common.collect.Lists;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.i18n.LanguageService;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.semantic.LabeledResource;
import org.molgenis.data.semanticsearch.service.TagService;
import org.molgenis.data.settings.AppSettings;
import org.molgenis.model.registry.model.ModelRegistryPackage;
import org.molgenis.model.registry.model.ModelRegistrySearch;
import org.molgenis.model.registry.model.ModelRegistryTreeNode;
import org.molgenis.model.registry.model.PackageSearchRequest;
import org.molgenis.model.registry.services.MetaDataSearchService;
import org.molgenis.model.registry.services.TreeNodeService;
import org.molgenis.ui.MolgenisPluginController;
import org.molgenis.ui.menu.MenuReaderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.molgenis.model.registry.controller.ModelRegistryController.URI;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@Controller
@RequestMapping(URI)
public class ModelRegistryController extends MolgenisPluginController
{

	//  namechange of route to model-registry is planned for 6.0.0
	//	public static final String ID = "model-registry";
	public static final String ID = "standardsregistry";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;
	private static final Logger LOG = LoggerFactory.getLogger(ModelRegistryController.class);
	private static final String VIEW_NAME = "view-model-registry";
	private static final String VIEW_NAME_DETAILS = "view-model-registry_details";
	private static final String VIEW_NAME_DOCUMENTATION = "view-model-registry_docs";
	private static final String VIEW_NAME_DOCUMENTATION_EMBED = "view-model-registry_docs-macros";
	private static final String VIEW_NAME_UML = "view-model-registry_uml";

	private final MetaDataService metaDataService;
	private final TagService<LabeledResource, LabeledResource> tagService;

	// Needed for VUE implementation
	private final LanguageService languageService;
	private final AppSettings appSettings;
	private final MenuReaderService menuReaderService;

	// Services for model-registry use only
	private final TreeNodeService treeNodeService;
	private final MetaDataSearchService metaDataSearchService;

	@Autowired
	public ModelRegistryController(MetaDataService metaDataService, MetaDataSearchService metaDataSearchService,
			TagService<LabeledResource, LabeledResource> tagService, TreeNodeService treeNodeService,
			LanguageService languageService, AppSettings appSettings, MenuReaderService menuReaderService)
	{
		super(URI);
		this.metaDataService = Objects.requireNonNull(metaDataService);
		this.metaDataSearchService = Objects.requireNonNull(metaDataSearchService);
		this.tagService = Objects.requireNonNull(tagService);
		this.treeNodeService = Objects.requireNonNull(treeNodeService);

		// Needed for VUE implementation
		this.languageService = Objects.requireNonNull(languageService);
		this.appSettings = Objects.requireNonNull(appSettings);
		this.menuReaderService = Objects.requireNonNull(menuReaderService);

	}

	@RequestMapping(method = GET)
	public String init(@RequestParam(value = "showPackageNotFound", required = false) String showPackageNotFound,
			Model model)
	{
		if (showPackageNotFound != null && showPackageNotFound.equalsIgnoreCase("true"))
		{
			model.addAttribute("warningMessage", "Package not found");
		}

		return VIEW_NAME;
	}

	@RequestMapping(value = "/documentation", method = GET)
	public String getModelDocumentation(Model model)
	{
		List<Package> packages = Lists.newArrayList(metaDataService.getRootPackages());
		model.addAttribute("packages", packages);
		model.addAttribute("tagService", tagService);
		return VIEW_NAME_DOCUMENTATION;
	}

	@RequestMapping(value = "/documentation/{packageName}", method = GET)
	public String getModelDocumentation(@PathVariable("packageName") String packageName,
			@RequestParam(value = "embed", required = false) Boolean embed, Model model)
	{
		Package aPackage = metaDataService.getPackage(packageName);
		model.addAttribute("package", aPackage);
		model.addAttribute("tagService", tagService);
		return VIEW_NAME_DOCUMENTATION_EMBED;
	}

	/**
	 * <p>Fired when a search is performed</p>
	 *
	 * @param packageSearchValue
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/search", method = GET)
	public String search(@RequestParam("packageSearchValue") String packageSearchValue, Model model)
	{
		ModelRegistrySearch modelRegistrySearchPackage = metaDataSearchService.search(packageSearchValue, 0, 3);
		if (modelRegistrySearchPackage != null)
		{
			model.addAttribute("packageSearchResponse", modelRegistrySearchPackage);
		}

		return VIEW_NAME;
	}

	/**
	 * <p>Only at the initialization of the model-registry.</p>
	 *
	 * @param packageSearchRequest
	 * @return
	 */
	@RequestMapping(value = "/search", method = POST)
	@ResponseBody
	public ModelRegistrySearch search(@Valid @RequestBody PackageSearchRequest packageSearchRequest)
	{
		return metaDataSearchService.search(packageSearchRequest.getQuery(), packageSearchRequest.getOffset(),
				packageSearchRequest.getNum());
	}

	@RequestMapping(value = "/details", method = GET)
	public String showView(@RequestParam(value = "package", required = false) String selectedPackageName, Model model)
	{
		if (selectedPackageName == null)
		{
			List<Package> packages = Lists.newArrayList(metaDataService.getRootPackages());
			selectedPackageName = packages.get(0).getId();
		}
		Package selectedPackage = metaDataService.getPackage(selectedPackageName);
		model.addAttribute("selectedPackageName", selectedPackageName);
		model.addAttribute("package", selectedPackage);
		model.addAttribute("tagService", tagService);
		return VIEW_NAME_DETAILS;
	}

	/**
	 * <p>Get uml data for UML-viewer</p>
	 *
	 * @param selectedPackageName
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/uml", method = GET, produces = "application/json")
	public String getUml(@RequestParam(value = "package", required = true) String selectedPackageName, Model model)
	{

		model.addAttribute("lng", languageService.getCurrentUserLanguageCode());
		model.addAttribute("fallbackLng", appSettings.getLanguageCode());
		model.addAttribute("baseUrl", getBaseUrl());

		if (!selectedPackageName.isEmpty())
		{
			model.addAttribute("molgenisPackage", selectedPackageName);
		}
		else
		{
			throw new MolgenisDataException("Unknown package: [ " + selectedPackageName + " ]");
		}
		return VIEW_NAME_UML;
	}

	@RequestMapping(value = "/getPackage", method = GET)
	@ResponseBody
	public ModelRegistryPackage getPackage(@RequestParam(value = "package") String packageName)
	{
		Package molgenisPackage = metaDataService.getPackage(packageName);
		if (molgenisPackage == null)
		{
			throw new MolgenisDataException("Unknown package: [ " + packageName + " ]");
		}

		return ModelRegistryPackage.create(molgenisPackage.getId(), molgenisPackage.getLabel(),
				molgenisPackage.getDescription(), null,
				metaDataSearchService.getEntitiesInPackage(molgenisPackage.getId()),
				metaDataSearchService.getTagsForPackage(molgenisPackage));
	}

	/**
	 * <p>Returns a {@link ModelRegistryTreeNode}-collections.</p>
	 *
	 * @param packageName
	 * @return
	 */
	@RequestMapping(value = "/getTreeData", method = GET)
	@ResponseBody
	public Collection<ModelRegistryTreeNode> getTree(@RequestParam(value = "package") String packageName)
	{
		Package molgenisPackage = metaDataService.getPackage(packageName);
		if (molgenisPackage == null)
		{
			throw new MolgenisDataException("Unknown package: [ " + packageName + " ]");
		}

		return Collections.singletonList(treeNodeService.createTreeNode(molgenisPackage));
	}

	private String getBaseUrl()
	{
		return menuReaderService.getMenu().findMenuItemPath(ModelRegistryController.ID);
	}
}
