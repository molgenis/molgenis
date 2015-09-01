package org.molgenis.ui;

import static org.molgenis.ui.MolgenisMenuController.URI;
import static org.molgenis.ui.MolgenisPluginAttributes.KEY_CONTEXT_URL;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.HandlerMapping;

@Controller
@RequestMapping(URI)
public class MolgenisMenuController
{
	private static final Logger LOG = LoggerFactory.getLogger(MolgenisMenuController.class);

	public static final String URI = "/menu";

	static final String KEY_MENU_ID = "menu_id";
	static final String KEY_MOLGENIS_VERSION = "molgenis_version";
	static final String KEY_MOLGENIS_BUILD_DATE = "molgenis_build_date";

	private final MolgenisUi molgenisUi;
	private final String molgenisVersion;
	private final String molgenisBuildData;

	@Autowired
	public MolgenisMenuController(MolgenisUi molgenisUi, @Value("${molgenis.version}") String molgenisVersion,
			@Value("${molgenis.build.date}") String molgenisBuildData)
	{
		if (molgenisUi == null) throw new IllegalArgumentException("molgenisUi is null");
		if (molgenisVersion == null) throw new IllegalArgumentException("molgenisVersion is null");
		if (molgenisBuildData == null) throw new IllegalArgumentException("molgenisBuildDate is null");
		this.molgenisUi = molgenisUi;
		this.molgenisVersion = molgenisVersion;
		// workaround for Eclipse bug: https://github.com/molgenis/molgenis/issues/2667
		this.molgenisBuildData = molgenisBuildData.equals("${maven.build.timestamp}") ? new SimpleDateFormat(
				"yyyy-MM-dd HH:mm").format(new Date()) + " by Eclipse" : molgenisBuildData;
	}

	@RequestMapping
	public String forwardDefaultMenuDefaultPlugin(Model model)
	{
		MolgenisUiMenu menu = molgenisUi.getMenu();
		if (menu == null) throw new RuntimeException("main menu does not exist");
		String menuId = menu.getId();
		model.addAttribute(KEY_MENU_ID, menuId);

		MolgenisUiMenuItem activeItem = menu.getActiveItem();
		if (activeItem == null)
		{
			LOG.warn("main menu does not contain any (accessible) items");
			return "forward:/login";
		}
		String pluginId = activeItem.getId();

		String contextUri = new StringBuilder(URI).append('/').append(menuId).append('/').append(pluginId).toString();
		model.addAttribute(KEY_CONTEXT_URL, contextUri);
		model.addAttribute(KEY_MOLGENIS_VERSION, molgenisVersion);
		model.addAttribute(KEY_MOLGENIS_BUILD_DATE, molgenisBuildData);
		return getForwardPluginUri(activeItem.getUrl(), null);
	}

	@RequestMapping("/{menuId}")
	public String forwardMenuDefaultPlugin(@Valid @NotNull @PathVariable String menuId, Model model)
	{
		MolgenisUiMenu menu = molgenisUi.getMenu(menuId);
		if (menu == null) throw new RuntimeException("menu with id [" + menuId + "] does not exist");
		model.addAttribute(KEY_MENU_ID, menuId);

		MolgenisUiMenuItem activeItem = menu.getActiveItem();
		String pluginId = activeItem != null ? activeItem.getId() : VoidPluginController.ID;

		String contextUri = new StringBuilder(URI).append('/').append(menuId).append('/').append(pluginId).toString();
		model.addAttribute(KEY_CONTEXT_URL, contextUri);
		model.addAttribute(KEY_MOLGENIS_VERSION, molgenisVersion);
		model.addAttribute(KEY_MOLGENIS_BUILD_DATE, molgenisBuildData);
		return getForwardPluginUri(pluginId, null);
	}

	@RequestMapping("/{menuId}/{pluginId}/**")
	public String forwardMenuPlugin(HttpServletRequest request, @Valid @NotNull @PathVariable String menuId,
			@Valid @NotNull @PathVariable String pluginId, Model model)
	{
		String contextUri = new StringBuilder(URI).append('/').append(menuId).append('/').append(pluginId).toString();
		String mappingUri = (String) (request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE));
		String remainder = mappingUri.substring(contextUri.length());

		model.addAttribute(KEY_CONTEXT_URL, contextUri);
		model.addAttribute(KEY_MOLGENIS_VERSION, molgenisVersion);
		model.addAttribute(KEY_MOLGENIS_BUILD_DATE, molgenisBuildData);
		model.addAttribute(KEY_MENU_ID, menuId);
		return getForwardPluginUri(pluginId, remainder);
	}

	private String getForwardPluginUri(String pluginId, String pathRemainder)
	{
		StringBuilder strBuilder = new StringBuilder("forward:");
		strBuilder.append(MolgenisPluginController.PLUGIN_URI_PREFIX).append(pluginId);
		if (pathRemainder != null) strBuilder.append(pathRemainder);
		return strBuilder.toString();
	}

	/**
	 * Plugin without content
	 */
	@Controller
	@RequestMapping(VoidPluginController.URI)
	public static class VoidPluginController extends MolgenisPluginController
	{
		public static final String ID = "void";
		public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;

		public VoidPluginController()
		{
			super(URI);
		}

		@RequestMapping
		public String init()
		{
			return "view-void";
		}
	}
}
