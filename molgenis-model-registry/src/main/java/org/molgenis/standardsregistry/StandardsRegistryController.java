package org.molgenis.standardsregistry;

import static org.molgenis.standardsregistry.StandardsRegistryController.URI;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Package;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.semantic.UntypedTagService;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.molgenis.standardsregistry.utils.PackageTreeNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

@Controller
@RequestMapping(URI)
public class StandardsRegistryController extends MolgenisPluginController
{
	public static final String ID = "standardsregistry";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;
	private static final String VIEW_NAME = "view-standardsregistry";
	private static final String VIEW_NAME_DETAILS = "view-standardsregistry_details";
	private final MetaDataService metaDataService;
	private final UntypedTagService tagService;

	@Autowired
	public StandardsRegistryController(MetaDataService metaDataService, UntypedTagService tagService)
	{
		super(URI);
		if (metaDataService == null) throw new IllegalArgumentException("metaDataService is null");
		this.metaDataService = metaDataService;
		this.tagService = tagService;
	}

	@RequestMapping(method = GET)
	public String init()
	{
		return VIEW_NAME;
	}

	@RequestMapping(value = "/search", method = POST)
	@ResponseBody
	public PackageSearchResponse search(@Valid @RequestBody PackageSearchRequest packageSearchRequest, Model model)
	{
		// FIXME hookup with meta data search service once implemented
		String selectedPackageName = packageSearchRequest.getQuery();

		PackageSearchResponse packageSearchResponse;
		if (selectedPackageName != null && !selectedPackageName.isEmpty())
		{
			PackageResponse aPackage = getPackage(selectedPackageName);
			if (aPackage != null)
			{
				packageSearchResponse = new PackageSearchResponse(selectedPackageName, 0, 1, 1,
						Collections.singletonList(aPackage));
			}
			else
			{
				packageSearchResponse = new PackageSearchResponse(selectedPackageName, 0, 0, 0,
						Collections.<PackageResponse> emptyList());
			}
		}
		else
		{
			List<PackageResponse> packageResponses = Lists.newArrayList(Iterables.transform(
					metaDataService.getRootPackages(), new Function<Package, PackageResponse>()
					{
						@Override
						public PackageResponse apply(Package aPackage)
						{
							return new PackageResponse(aPackage.getSimpleName(), aPackage.getDescription());
						}
					}));
			int total = packageResponses.size();
			if (packageSearchRequest.getOffset() != null)
			{
				packageResponses = packageResponses.subList(packageSearchRequest.getOffset(),
						packageResponses.size() - 1);
			}
			if (packageSearchRequest.getNum() != null && packageResponses.size() > packageSearchRequest.getNum())
			{
				packageResponses = packageResponses.subList(0, packageSearchRequest.getNum());
			}
			int offset = packageSearchRequest.getOffset() != null ? packageSearchRequest.getOffset() : 0;
			int num = packageSearchRequest.getNum() != null ? packageSearchRequest.getNum() : packageResponses.size();
			packageSearchResponse = new PackageSearchResponse(selectedPackageName, offset, num, total, packageResponses);
		}
		return packageSearchResponse;
	}

	@RequestMapping(value = "/details", method = GET)
	public String showView(@RequestParam(value = "package", required = false) String selectedPackageName, Model model)
	{
		boolean showPackageSelect = (selectedPackageName == null);
		List<Package> packages = Lists.newArrayList(metaDataService.getRootPackages());

		model.addAttribute("showPackageSelect", showPackageSelect);
		model.addAttribute("packages", packages);

		if (selectedPackageName == null) selectedPackageName = packages.get(0).getName();
		model.addAttribute("selectedPackageName", selectedPackageName);

		return VIEW_NAME_DETAILS;
	}

	@RequestMapping(value = "/getPackage", method = GET)
	@ResponseBody
	public PackageResponse getPackage(@RequestParam(value = "package") String selectedPackageName)
	{
		Package molgenisPackage = metaDataService.getPackage(selectedPackageName);
		return new PackageResponse(molgenisPackage.getSimpleName(), molgenisPackage.getDescription());
	}

	/* PACKAGE TREE */
	@RequestMapping(value = "/getTreeData", method = GET)
	@ResponseBody
	public Collection<PackageTreeNode> getTree(@RequestParam(value = "package") String packageName)
	{
		Package selectedPackage = metaDataService.getPackage(packageName);
		return Collections.singletonList(createPackageTreeNode(selectedPackage));
	}

	private PackageTreeNode createPackageTreeNode(Package selectedPackage)
	{
		String title = selectedPackage.getSimpleName();
		String key = selectedPackage.getName();
		String tooltip = selectedPackage.getDescription();
		List<PackageTreeNode> result = new ArrayList<PackageTreeNode>();
		boolean folder = true;
		boolean expanded = true;

		Map<String, Object> data = new HashMap<String, Object>();
		data.put("type", "package");

		for (Package subPackage : selectedPackage.getSubPackages())
		{
			result.add(createPackageTreeNode(subPackage));
		}

		for (EntityMetaData emd : selectedPackage.getEntityMetaDatas())
		{
			result.add(createPackageTreeNode(emd));
		}

		return new PackageTreeNode(title, key, tooltip, folder, expanded, data, result);
	}

	private PackageTreeNode createPackageTreeNode(EntityMetaData emd)
	{
		String title = emd.getLabel();
		String key = emd.getName();
		String tooltip = emd.getDescription();
		List<PackageTreeNode> result = new ArrayList<PackageTreeNode>();
		boolean folder = true;
		boolean expanded = false;

		Map<String, Object> data = new HashMap<String, Object>();
		data.put("type", "entity");
		data.put("href", "/api/v1/" + emd.getName() + "/meta");

		for (AttributeMetaData amd : emd.getAttributes())
		{
			result.add(createPackageTreeNode(amd, emd));
		}

		return new PackageTreeNode(title, key, tooltip, folder, expanded, data, result);
	}

	private PackageTreeNode createPackageTreeNode(AttributeMetaData amd, EntityMetaData emd)
	{
		String title = amd.getLabel();
		String key = amd.getName();
		String tooltip = amd.getDescription();
		List<PackageTreeNode> result = new ArrayList<PackageTreeNode>();
		boolean folder;
		boolean expanded = false;

		Map<String, Object> data = new HashMap<String, Object>();
		data.put("type", "attribute");
		data.put("href", "/api/v1/" + emd.getName() + "/meta/" + amd.getName());
		data.put("tags", tagService.getTagsForAttribute(emd, amd));

		if (amd.getDataType().getEnumType() == FieldTypeEnum.COMPOUND)
		{
			for (AttributeMetaData subAmd : amd.getAttributeParts())
			{
				result.add(createPackageTreeNode(subAmd, emd));
			}
			folder = true;
		}
		else
		{
			folder = false;
		}

		return new PackageTreeNode(title, key, tooltip, folder, expanded, data, result);
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

		@SuppressWarnings("unused")
		public void setQuery(String query)
		{
			this.query = query;
		}

		public Integer getOffset()
		{
			return offset;
		}

		@SuppressWarnings("unused")
		public void setOffset(Integer offset)
		{
			this.offset = offset;
		}

		public Integer getNum()
		{
			return num;
		}

		@SuppressWarnings("unused")
		public void setNum(Integer num)
		{
			this.num = num;
		}
	}

	private static class PackageResponse
	{
		private final String name;
		private final String description;

		public PackageResponse(String name, String description)
		{
			this.name = name;
			this.description = description;
		}

		@SuppressWarnings("unused")
		public String getName()
		{
			return name;
		}

		@SuppressWarnings("unused")
		public String getDescription()
		{
			return description;
		}
	}
}
