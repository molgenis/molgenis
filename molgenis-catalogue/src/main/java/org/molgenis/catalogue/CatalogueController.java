package org.molgenis.catalogue;

import static org.molgenis.catalogue.CatalogueController.URI;
import static org.springframework.http.HttpStatus.OK;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.catalogue.model.ShoppingCart;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Package;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.molgenis.tree.TreeNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.google.common.collect.Lists;

@Controller
@RequestMapping(URI)
public class CatalogueController extends MolgenisPluginController
{
	private static final String SHOPPINGCART_URI = "/shoppingcart";
	public static final String ID = "catalogue";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;
	private static final String VIEW_NAME = "view-packagebrowser";
	private static final String CART_VIEW_NAME = "view-shoppingcart";
	private final DataService dataService;
	private final MetaDataService metaDataService;

	@Autowired
	public CatalogueController(DataService dataService, MetaDataService metaDataService)
	{
		super(URI);
		this.dataService = dataService;
		this.metaDataService = metaDataService;
	}

	@RequestMapping(method = GET)
	public String init()
	{
		return VIEW_NAME;
	}

	@RequestMapping(value = "/search", method = POST, headers = "Content-Type=application/x-www-form-urlencoded")
	@ResponseBody
	public PackageSearchResponse search(@ModelAttribute(value = "packageSearch") String selectedPackageName, Model model)
	{
		PackageSearchResponse packageSearchResponse;
		if(selectedPackageName != null && !selectedPackageName.isEmpty()) {
			packageSearchResponse = new PackageSearchResponse(1,
					Collections.singletonList(getPackage(selectedPackageName)));
		} else {
			packageSearchResponse = new PackageSearchResponse(1, Collections.singletonList(getPackage("default")));
		}
		return packageSearchResponse;
	}

	private static class PackageSearchResponse{
		private int offset = 0;
		private int num = 10;
		private int total;
		private List<PackageResponse> packages;
		
		public PackageSearchResponse(int total, List<PackageResponse> packages)
		{
			this.total = total;
			this.packages = packages;
		}
	}
	
	@RequestMapping(value = "/package-details-explorer", method = GET)
	public String showView(@RequestParam(value = "package", required = false) String selectedPackageName, Model model)
	{
		boolean showPackageSelect = (selectedPackageName == null);
		List<Package> packages = Lists.newArrayList(metaDataService.getRootPackages());

		model.addAttribute("showPackageSelect", showPackageSelect);
		model.addAttribute("packages", packages);

		if (selectedPackageName == null) selectedPackageName = packages.get(0).getName();
		model.addAttribute("selectedPackageName", selectedPackageName);

		return "view-package-details";
	}

	@RequestMapping(value = "/getPackage", method = GET)
	@ResponseBody
	public PackageResponse getPackage(@RequestParam(value = "package") String selectedPackageName)
	{
		Package molgenisPackage = metaDataService.getPackage(selectedPackageName);
		return new PackageResponse(molgenisPackage.getSimpleName(), molgenisPackage.getDescription());
	}

	private static class PackageResponse
	{
		private String name;
		private String description;

		public PackageResponse(String name, String description)
		{
			this.setName(name);
			this.setDescription(description);
		}

		public String getName()
		{
			return name;
		}

		public void setName(String name)
		{
			this.name = name;
		}

		public String getDescription()
		{
			return description;
		}

		public void setDescription(String description)
		{
			this.description = description;
		}
	}

	/* PACKAGE TREE */
	@RequestMapping(value = "/getTreeData", method = GET)
	@ResponseBody
	public Collection<TreeNode> getTree(@RequestParam(value = "package") String packageName)
	{
		Package selectedPackage = metaDataService.getPackage(packageName);
		return Collections.singletonList(createTreeForPackage(selectedPackage));
	}

	private TreeNode createTreeForPackage(Package selectedPackage)
	{
		String title = selectedPackage.getSimpleName();
		String key = selectedPackage.getName();
		String tooltip = selectedPackage.getDescription();
		List<TreeNode> result = new ArrayList<TreeNode>();
		boolean folder = true;
		boolean expanded = true;

		Map<String, Object> data = new HashMap<String, Object>();
		data.put("type", "package");

		for (Package subPackage : selectedPackage.getSubPackages())
		{
			result.add(createTreeForPackage(subPackage));
		}

		for (EntityMetaData emd : selectedPackage.getEntities())
		{
			result.add(createNodeForEntityMetaData(emd));
		}

		return new TreeNode(title, key, tooltip, folder, expanded, data, result);
	}

	private TreeNode createNodeForEntityMetaData(EntityMetaData emd)
	{
		String title = emd.getLabel();
		String key = emd.getName();
		String tooltip = emd.getDescription();
		List<TreeNode> result = new ArrayList<TreeNode>();
		boolean folder = true;
		boolean expanded = false;

		Map<String, Object> data = new HashMap<String, Object>();
		data.put("type", "entity");
		data.put("href", "/api/v1/" + emd.getName() + "/meta");

		for (AttributeMetaData amd : emd.getAttributes())
		{
			result.add(createNodeForAttribute(amd, emd));
		}

		return new TreeNode(title, key, tooltip, folder, expanded, data, result);
	}

	private TreeNode createNodeForAttribute(AttributeMetaData amd, EntityMetaData emd)
	{
		String title = amd.getLabel();
		String key = amd.getName();
		String tooltip = amd.getDescription();
		List<TreeNode> result = new ArrayList<TreeNode>();
		boolean folder;
		boolean expanded = false;

		Map<String, Object> data = new HashMap<String, Object>();
		data.put("type", "attribute");
		data.put("href", "/api/v1/" + emd.getName() + "/meta/" + amd.getName());

		if (amd.getDataType().getEnumType() == FieldTypeEnum.COMPOUND)
		{
			for (AttributeMetaData subAmd : amd.getAttributeParts())
			{
				result.add(createNodeForAttribute(subAmd, emd));
			}
			folder = true;
		}
		else
		{
			folder = false;
		}

		return new TreeNode(title, key, tooltip, folder, expanded, data, result);
	}

	/* CART */
	@RequestMapping(value = SHOPPINGCART_URI, method = POST)
	@ResponseStatus(OK)
	public void refreshShoppingCart(@Valid @RequestBody RefreshShoppingCartRequest request, HttpSession session)
	{
		ShoppingCart cart = getShoppingCart(session);
		cart.clear();

		String entityName = request.getEntityName();
		for (String attributeName : request.getAttributeNames())
		{
			cart.addAttribute(entityName, attributeName);
		}
	}

	@RequestMapping(SHOPPINGCART_URI + "/remove")
	public @ResponseBody
	ShoppingCart removeFromShoppingCart(HttpSession session, @RequestParam(required = true) String attributeName)
	{
		return getShoppingCart(session).removeAttribute(attributeName);
	}

	@RequestMapping(SHOPPINGCART_URI + "/clear")
	public @ResponseBody
	ShoppingCart clearShoppingCart(HttpSession session)
	{
		return getShoppingCart(session).clear();
	}

	@RequestMapping(SHOPPINGCART_URI + "/show")
	public String showShoppingCart(@RequestParam(required = false) String entityName, Model model, HttpSession session)
	{
		final ShoppingCart cart = getShoppingCart(session);
		if (entityName != null)
		{
			cart.setEntityName(entityName);
		}
		if (cart.isEmpty())
		{
			model.addAttribute("attributes", Collections.emptyList());
		}
		else
		{
			EntityMetaData metaData = dataService.getEntityMetaData(cart.getEntityName());

			List<AttributeMetaData> selectedAttributes = Lists.newArrayList();
			for (String attrName : cart.getAttributes())
			{
				AttributeMetaData attr = metaData.getAttribute(attrName);
				if (attr != null)
				{
					selectedAttributes.add(attr);
				}
			}

			model.addAttribute("attributes", selectedAttributes);

		}
		return CART_VIEW_NAME;
	}

	/**
	 * Retrieves the {@link ShoppingCart} from the session or creates one if none present.
	 * 
	 * @param session
	 *            the {@link HttpSession}
	 * @return the {@link ShoppingCart}
	 */
	private static ShoppingCart getShoppingCart(HttpSession session)
	{
		if (session.getAttribute("shoppingCart") == null)
		{
			session.setAttribute("shoppingCart", new ShoppingCart());
		}
		ShoppingCart cart = (ShoppingCart) session.getAttribute("shoppingCart");
		return cart;
	}
}
