package org.molgenis.standardsregistry;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.semantic.LabeledResource;
import org.molgenis.data.semanticsearch.service.TagService;
import org.molgenis.standardsregistry.services.MetaDataSearchService;
import org.molgenis.standardsregistry.services.TreeNodeService;
import org.molgenis.standardsregistry.model.*;
import org.molgenis.ui.MolgenisPluginController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.molgenis.standardsregistry.StandardsRegistryController.URI;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@Controller
@RequestMapping(URI)
public class StandardsRegistryController extends MolgenisPluginController
{

	private static final Logger LOG = LoggerFactory.getLogger(StandardsRegistryController.class);

	public static final String ID = "standardsregistry";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;

	private static final String VIEW_NAME = "view-model-standardsregistry";
	private static final String VIEW_NAME_DETAILS = "view-model-standardsregistry_details";
	private static final String VIEW_NAME_DOCUMENTATION = "view-model-standardsregistry_docs";
	private static final String VIEW_NAME_DOCUMENTATION_EMBED = "view-model-standardsregistry_docs-macros";
	private static final String VIEW_NAME_UML = "view-model-standardsregistry_uml";

	private final MetaDataService metaDataService;
	private final TagService<LabeledResource, LabeledResource> tagService;

	// Services for model-registry use only
	private final TreeNodeService treeNodeService;
	private final MetaDataSearchService metaDataSearchService;

	@Autowired
	public StandardsRegistryController(MetaDataService metaDataService, MetaDataSearchService metaDataSearchService, TagService<LabeledResource, LabeledResource> tagService, TreeNodeService treeNodeService)
	{
		super(URI);
		this.metaDataSearchService = Objects.requireNonNull(metaDataSearchService);
		this.treeNodeService = Objects.requireNonNull(treeNodeService);
		this.metaDataService = Objects.requireNonNull(metaDataService);
		this.tagService = Objects.requireNonNull(tagService);

	}

	@RequestMapping(method = GET)
	public String init(@RequestParam(value = "showPackageNotFound", required = false) String showPackageNotFound, Model model)
	{
		if (showPackageNotFound != null && showPackageNotFound.equalsIgnoreCase("true"))
		{
			model.addAttribute("warningMessage", "Model not found");
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
	public String getModelDocumentation(@PathVariable("packageName") String packageName, @RequestParam(value = "embed", required = false) Boolean embed, Model model)
	{
		Package aPackage = metaDataService.getPackage(packageName);
		model.addAttribute("package", aPackage);
		model.addAttribute("tagService", tagService);
		return VIEW_NAME_DOCUMENTATION_EMBED;
	}

	@RequestMapping(value = "/search", method = GET)
	public String search(@RequestParam("packageSearchValue") String packageSearchValue, Model model)
	{
		Gson gson = new Gson();
		PackageSearchRequest packageSearchRequest = new PackageSearchRequest();
		packageSearchRequest.setQuery(packageSearchValue);
		packageSearchRequest.setOffset(0);
		packageSearchRequest.setNum(3);

		PackageSearchResponse packageSearchResponse = metaDataSearchService.search(packageSearchRequest);
		if (packageSearchRequest != null)
		{
			model.addAttribute("packageSearchResponse", gson.toJson(packageSearchResponse));
		}

		return VIEW_NAME;
	}

	@RequestMapping(value = "/search", method = POST)
	@ResponseBody
	public PackageSearchResponse search(@RequestAttribute PackageSearchRequest packageSearchRequest)
	{
		return metaDataSearchService.search(packageSearchRequest);
	}

	@RequestMapping(value = "/details", method = GET)
	public String showView(@RequestParam(value = "package", required = false) String selectedPackageName, Model model)
	{
		if (selectedPackageName == null)
		{
			List<Package> packages = Lists.newArrayList(metaDataService.getRootPackages());
			selectedPackageName = packages.get(0).getId();
		}
		model.addAttribute("tagService", tagService);
		model.addAttribute("selectedPackageName", selectedPackageName);
		model.addAttribute("package", metaDataService.getPackage(selectedPackageName));

		return VIEW_NAME_DETAILS;
	}

	@RequestMapping(value = "/uml", method = GET)
	public String getUml(@RequestParam(value = "package", required = true) String selectedPackageName, Model model)
	{
		LOG.info("Requested package: [ " +selectedPackageName+ " ]");
		Package molgenisPackage = metaDataService.getPackage(selectedPackageName);

		LOG.info("Converted package: [ " + molgenisPackage + " ]");

		if (molgenisPackage != null)
		{
			model.addAttribute("molgenisPackage", molgenisPackage);
		}

		return VIEW_NAME_UML;
	}

	@RequestMapping(value = "/getPackage", method = GET)
	@ResponseBody
	public PackageResponse getPackage(@RequestParam(value = "package") String packageName)
	{
		Package molgenisPackage = metaDataService.getPackage(packageName);
		if (molgenisPackage == null) return null;

		return new PackageResponse(molgenisPackage.getId(), molgenisPackage.getLabel(),
				molgenisPackage.getDescription(), null, metaDataSearchService.getEntitiesInPackage(molgenisPackage.getId()),
				metaDataSearchService.getTagsForPackage(molgenisPackage));
	}

	/**
	 *
	 * <p>PackageTree</p>
	 *
	 * @param packageName
	 * @return
	 */
	@RequestMapping(value = "/getTreeData", method = GET)
	@ResponseBody
	public Collection<PackageTreeNode> getTree(@RequestParam(value = "package") String packageName)
	{
		Package molgenisPackage = metaDataService.getPackage(packageName);
		if (molgenisPackage == null) return null;

		return Collections.singletonList(treeNodeService.createPackageTreeNode(molgenisPackage));
	}
}
