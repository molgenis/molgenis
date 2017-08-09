package org.molgenis.standardsregistry;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import org.molgenis.data.DataService;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.MetaDataSearchService;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.PackageSearchResultItem;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.semantic.LabeledResource;
import org.molgenis.data.semantic.SemanticTag;
import org.molgenis.data.semanticsearch.service.TagService;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.security.core.MolgenisPermissionService;
import org.molgenis.security.core.Permission;
import org.molgenis.standardsregistry.utils.PackageTreeNode;
import org.molgenis.ui.MolgenisPluginController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.*;

import static org.molgenis.standardsregistry.StandardsRegistryController.URI;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@Controller
@RequestMapping(URI)
public class StandardsRegistryController extends MolgenisPluginController
{
	public static final String ID = "standardsregistry";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;
	private static final String VIEW_NAME = "view-standardsregistry";
	private static final String VIEW_NAME_DETAILS = "view-standardsregistry_details";
	private static final String VIEW_NAME_DOCUMENTATION = "view-standardsregistry_docs";
	private static final String VIEW_NAME_DOCUMENTATION_EMBED = "view-standardsregistry_docs-body";
	private final MetaDataService metaDataService;
	private final DataService dataService;
	private final MetaDataSearchService metaDataSearchService;
	private final MolgenisPermissionService molgenisPermissionService;
	private final TagService<LabeledResource, LabeledResource> tagService;

	@Autowired
	public StandardsRegistryController(DataService dataService, MetaDataService metaDataService,
			MolgenisPermissionService molgenisPermissionService,
			TagService<LabeledResource, LabeledResource> tagService, MetaDataSearchService metaDataSearchService)
	{
		super(URI);
		if (dataService == null) throw new IllegalArgumentException("dataService is null");
		if (molgenisPermissionService == null) throw new IllegalArgumentException("molgenisPermissionService is null");
		if (metaDataService == null) throw new IllegalArgumentException("metaDataService is null");
		if (metaDataSearchService == null) throw new IllegalArgumentException("metaDataSearchService is null");
		this.dataService = dataService;
		this.metaDataService = metaDataService;
		this.molgenisPermissionService = molgenisPermissionService;
		this.metaDataSearchService = metaDataSearchService;
		this.tagService = tagService;
	}

	@RequestMapping(method = GET)
	public String init(@RequestParam(value = "showPackageNotFound", required = false) String showPackageNotFound,
			Model model)
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
	public String getModelDocumentation(@PathVariable("packageName") String packageName,
			@RequestParam(value = "embed", required = false) Boolean embed, Model model)
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

		PackageSearchResponse packageSearchResponse = search(packageSearchRequest, model);
		if (packageSearchRequest != null)
		{
			model.addAttribute("packageSearchResponse", gson.toJson(packageSearchResponse));
		}

		return VIEW_NAME;
	}

	@RequestMapping(value = "/search", method = POST)
	@ResponseBody
	public PackageSearchResponse search(@Valid @RequestBody PackageSearchRequest packageSearchRequest, Model model)
	{
		String searchQuery = packageSearchRequest.getQuery();
		List<PackageResponse> packageResponses = Lists.newArrayList();

		List<PackageSearchResultItem> searchResults = metaDataSearchService.findRootPackages(searchQuery);
		for (PackageSearchResultItem searchResult : searchResults)
		{
			Package p = searchResult.getPackageFound();
			List<PackageResponse.Entity> entitiesInPackageUnfiltered = getEntitiesInPackage(p.getId());
			List<PackageResponse.Entity> entitiesInPackageFiltered = Lists.newArrayList(
					Iterables.filter(entitiesInPackageUnfiltered, entity ->
					{
						if (entity.isAbtract()) return false;

						String entityTypeId = entity.getName();

						// Check read permission
						if (!molgenisPermissionService.hasPermissionOnEntity(entityTypeId, Permission.READ))
							return false;

						// Check has data
						if (!dataService.hasRepository(entityTypeId)
								|| dataService.count(entityTypeId, new QueryImpl<>()) == 0) return false;

						return true;
					}));

			PackageResponse pr = new PackageResponse(p.getId(), p.getLabel(), p.getDescription(),
					searchResult.getMatchDescription(), entitiesInPackageFiltered, getTagsForPackage(p));
			packageResponses.add(pr);
		}

		int total = packageResponses.size();
		if (total > 0)
		{
			if (packageSearchRequest.getOffset() != null)
			{
				packageResponses = packageResponses.subList(packageSearchRequest.getOffset(), packageResponses.size());
			}

			if (packageSearchRequest.getNum() != null && packageResponses.size() > packageSearchRequest.getNum())
			{
				packageResponses = packageResponses.subList(0, packageSearchRequest.getNum());
			}
		}

		int offset = packageSearchRequest.getOffset() != null ? packageSearchRequest.getOffset() : 0;
		int num = packageSearchRequest.getNum() != null ? packageSearchRequest.getNum() : packageResponses.size();

		PackageSearchResponse packageSearchResponse = new PackageSearchResponse(searchQuery, offset, num, total,
				packageResponses);

		return packageSearchResponse;
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
	public String getUml(@RequestParam(value = "package") String selectedPackageName, Model model)
	{
		Package molgenisPackage = metaDataService.getPackage(selectedPackageName);

		if (molgenisPackage != null)
		{
			model.addAttribute("molgenisPackage", molgenisPackage);
		}

		return "view-standardsregistry_uml";
	}

	@RequestMapping(value = "/getPackage", method = GET)
	@ResponseBody
	public PackageResponse getPackage(@RequestParam(value = "package") String packageName)
	{
		Package molgenisPackage = metaDataService.getPackage(packageName);
		if (molgenisPackage == null) return null;

		return new PackageResponse(molgenisPackage.getId(), molgenisPackage.getLabel(),
				molgenisPackage.getDescription(), null, getEntitiesInPackage(molgenisPackage.getId()),
				getTagsForPackage(molgenisPackage));
	}

	/* PACKAGE TREE */
	@RequestMapping(value = "/getTreeData", method = GET)
	@ResponseBody
	public Collection<PackageTreeNode> getTree(@RequestParam(value = "package") String packageName)
	{
		Package molgenisPackage = metaDataService.getPackage(packageName);
		if (molgenisPackage == null) return null;

		return Collections.singletonList(createPackageTreeNode(molgenisPackage));
	}

	private PackageTreeNode createPackageTreeNode(Package package_)
	{
		String title = package_.getLabel() != null ? package_.getLabel() : package_.getId();
		String key = package_.getId();
		String tooltip = package_.getDescription();
		List<PackageTreeNode> result = new ArrayList<>();
		boolean folder = true;
		boolean expanded = true;

		Map<String, Object> data = new HashMap<>();
		data.put("type", "package");

		for (Package subPackage : package_.getChildren())
		{
			result.add(createPackageTreeNode(subPackage));
		}

		for (EntityType emd : package_.getEntityTypes())
		{
			result.add(createPackageTreeNode(emd));
		}

		return new PackageTreeNode("package", title, key, tooltip, folder, expanded, data, result);
	}

	private PackageTreeNode createPackageTreeNode(EntityType emd)
	{
		String title = emd.getLabel();
		String key = emd.getId();
		String tooltip = emd.getDescription();
		List<PackageTreeNode> result = new ArrayList<>();
		boolean folder = true;
		boolean expanded = false;

		Map<String, Object> data = new HashMap<>();
		data.put("type", "entity");
		data.put("href", "/api/v1/" + emd.getId() + "/meta");

		for (Attribute amd : emd.getAttributes())
		{
			result.add(createPackageTreeNode(amd, emd));
		}

		return new PackageTreeNode("entity", title, key, tooltip, folder, expanded, data, result);
	}

	private PackageTreeNode createPackageTreeNode(Attribute amd, EntityType emd)
	{
		String title = amd.getLabel();
		String key = amd.getName();
		String tooltip = amd.getDescription();
		List<PackageTreeNode> result = new ArrayList<>();
		boolean folder;
		boolean expanded = false;

		Map<String, Object> data = new HashMap<>();
		data.put("type", "attribute");
		data.put("href", "/api/v1/" + emd.getId() + "/meta/" + amd.getName());
		data.put("tags", tagService.getTagsForAttribute(emd, amd));

		if (amd.getDataType() == AttributeType.COMPOUND)
		{
			for (Attribute subAmd : amd.getChildren())
			{
				result.add(createPackageTreeNode(subAmd, emd));
			}
			folder = true;
		}
		else
		{
			folder = false;
		}

		return new PackageTreeNode("attribute", title, key, tooltip, folder, expanded, data, result);
	}

	private List<PackageResponse.Tag> getTagsForPackage(Package p)
	{
		List<PackageResponse.Tag> tags = Lists.newArrayList();

		for (SemanticTag<Package, LabeledResource, LabeledResource> tag : tagService.getTagsForPackage(p))
		{
			tags.add(new PackageResponse.Tag(tag.getObject().getLabel(), tag.getObject().getIri(),
					tag.getRelation().toString()));
		}

		return tags;
	}

	private List<PackageResponse.Entity> getEntitiesInPackage(String packageName)
	{
		List<PackageResponse.Entity> entiesForThisPackage = new ArrayList<>();
		Package aPackage = metaDataService.getPackage(packageName);
		getEntitiesInPackageRec(aPackage, entiesForThisPackage);
		return entiesForThisPackage;
	}

	private void getEntitiesInPackageRec(Package aPackage, List<PackageResponse.Entity> entiesForThisPackage)
	{
		for (EntityType emd : aPackage.getEntityTypes())
		{
			entiesForThisPackage.add(new PackageResponse.Entity(emd.getId(), emd.getLabel(), emd.isAbstract()));
		}
		Iterable<Package> subPackages = aPackage.getChildren();
		if (subPackages != null)
		{
			for (Package subPackage : subPackages)
			{
				getEntitiesInPackageRec(subPackage, entiesForThisPackage);
			}
		}
	}

	private static class PackageSearchResponse
	{
		private final String query;
		private final int offset;
		private final int num;
		private final int total;
		private final List<PackageResponse> packages;

		public PackageSearchResponse(String query, int offset, int num, int total, List<PackageResponse> packages)
		{
			this.offset = offset;
			this.num = num;
			this.query = query;
			this.total = total;
			this.packages = packages;
		}

		@SuppressWarnings("unused")
		public String getQuery()
		{
			return query;
		}

		@SuppressWarnings("unused")
		public int getOffset()
		{
			return offset;
		}

		@SuppressWarnings("unused")
		public int getNum()
		{
			return num;
		}

		@SuppressWarnings("unused")
		public int getTotal()
		{
			return total;
		}

		@SuppressWarnings("unused")
		public List<PackageResponse> getPackages()
		{
			return packages;
		}
	}

	private static class PackageSearchRequest
	{
		private String query;
		@Min(0)
		private Integer offset;
		@Min(0)
		@Max(100)
		private Integer num;

		public String getQuery()
		{
			return query;
		}

		public void setQuery(String query)
		{
			this.query = query;
		}

		public Integer getOffset()
		{
			return offset;
		}

		public void setOffset(Integer offset)
		{
			this.offset = offset;
		}

		public Integer getNum()
		{
			return num;
		}

		public void setNum(Integer num)
		{
			this.num = num;
		}
	}

	private static class PackageResponse
	{
		private final String name;
		private final String label;
		private final String description;
		private final String matchDescription;
		private final List<PackageResponse.Entity> entitiesInPackage;
		private final List<Tag> tags;

		public PackageResponse(String name, String label, String description, String matchDescription,
				List<PackageResponse.Entity> entitiesInPackage, List<Tag> tags)
		{
			this.name = name;
			this.label = label;
			this.description = description;
			this.matchDescription = matchDescription;
			this.entitiesInPackage = entitiesInPackage;
			this.tags = tags;
		}

		@SuppressWarnings("unused")
		public String getName()
		{
			return name;
		}

		public String getLabel()
		{
			return label;
		}

		@SuppressWarnings("unused")
		public String getDescription()
		{
			return description;
		}

		@SuppressWarnings("unused")
		public String getMatchDescription()
		{
			return matchDescription;
		}

		@SuppressWarnings("unused")
		public List<PackageResponse.Entity> getEntities()
		{
			return entitiesInPackage;
		}

		@SuppressWarnings("unused")
		public Iterable<Tag> getTags()
		{
			return tags;
		}

		private static class Entity
		{
			private final String name;
			private final String label;
			private final boolean abstr;

			public Entity(String name, String label, boolean abstr)
			{
				super();
				this.name = name;
				this.label = label;
				this.abstr = abstr;
			}

			public String getName()
			{
				return name;
			}

			@SuppressWarnings("unused")
			public String getLabel()
			{
				return label;
			}

			public boolean isAbtract()
			{
				return abstr;
			}
		}

		private static class Tag
		{
			private final String label;
			private final String relation;
			private final String iri;

			public Tag(String label, String iri, String relation)
			{
				super();
				this.label = label;
				this.iri = iri;
				this.relation = relation;
			}

			@SuppressWarnings("unused")
			public String getLabel()
			{
				return label;
			}

			@SuppressWarnings("unused")
			public String getIri()
			{
				return iri;
			}

			@SuppressWarnings("unused")
			public String getRelation()
			{
				return relation;
			}

		}
	}

}
