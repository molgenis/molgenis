package org.molgenis.core.ui;

import org.molgenis.web.PluginController;
import org.molgenis.web.Ui;
import org.molgenis.web.UiMenu;
import org.molgenis.web.UiMenuItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.format.DateTimeFormatter;

import static java.time.ZonedDateTime.now;
import static java.time.format.FormatStyle.MEDIUM;
import static org.molgenis.core.ui.MolgenisMenuController.URI;
import static org.molgenis.web.PluginAttributes.KEY_CONTEXT_URL;

@Controller
@RequestMapping(URI)
public class MolgenisMenuController
{
	private static final Logger LOG = LoggerFactory.getLogger(MolgenisMenuController.class);

	public static final String URI = "/menu";

	static final String KEY_MENU_ID = "menu_id";
	static final String KEY_MOLGENIS_VERSION = "molgenis_version";
	static final String KEY_MOLGENIS_BUILD_DATE = "molgenis_build_date";

	private final Ui molgenisUi;
	private final String molgenisVersion;
	private final String molgenisBuildData;

	public MolgenisMenuController(Ui molgenisUi, @Value("${molgenis.version}") String molgenisVersion,
			@Value("${molgenis.build.date}") String molgenisBuildData)
	{
		if (molgenisUi == null) throw new IllegalArgumentException("molgenisUi is null");
		if (molgenisVersion == null) throw new IllegalArgumentException("molgenisVersion is null");
		if (molgenisBuildData == null) throw new IllegalArgumentException("molgenisBuildDate is null");
		this.molgenisUi = molgenisUi;
		this.molgenisVersion = molgenisVersion;
		// workaround for Eclipse bug: https://github.com/molgenis/molgenis/issues/2667
		this.molgenisBuildData = molgenisBuildData.equals("${maven.build.timestamp}") ?
				DateTimeFormatter.ofLocalizedDateTime(MEDIUM).format(now()) + " by Eclipse" : molgenisBuildData;
	}

	@RequestMapping
	public String forwardDefaultMenuDefaultPlugin(Model model)
	{
		UiMenu menu = molgenisUi.getMenu();
		if (menu == null) throw new RuntimeException("main menu does not exist");
		String menuId = menu.getId();
		model.addAttribute(KEY_MENU_ID, menuId);

		UiMenuItem activeItem = menu.getActiveItem();
		if (activeItem == null)
		{
			LOG.warn("main menu does not contain any (accessible) items");
			return "forward:/login";
		}
		String pluginId = activeItem.getId();

		String contextUri = URI + '/' + menuId + '/' + pluginId;
		model.addAttribute(KEY_CONTEXT_URL, contextUri);
		model.addAttribute(KEY_MOLGENIS_VERSION, molgenisVersion);
		model.addAttribute(KEY_MOLGENIS_BUILD_DATE, molgenisBuildData);
		return getForwardPluginUri(activeItem.getUrl(), null);
	}

	@RequestMapping("/{menuId}")
	public String forwardMenuDefaultPlugin(@Valid @NotNull @PathVariable String menuId, Model model)
	{
		UiMenu menu = molgenisUi.getMenu(menuId);
		if (menu == null) throw new RuntimeException("menu with id [" + menuId + "] does not exist");
		model.addAttribute(KEY_MENU_ID, menuId);

		UiMenuItem activeItem = menu.getActiveItem();
		String pluginId = activeItem != null ? activeItem.getId() : VoidPluginController.ID;

		String contextUri = URI + '/' + menuId + '/' + pluginId;
		model.addAttribute(KEY_CONTEXT_URL, contextUri);
		model.addAttribute(KEY_MOLGENIS_VERSION, molgenisVersion);
		model.addAttribute(KEY_MOLGENIS_BUILD_DATE, molgenisBuildData);
		return getForwardPluginUri(pluginId, null);
	}

	@RequestMapping("/{menuId}/{pluginId}/**")
	public String forwardMenuPlugin(HttpServletRequest request, @Valid @NotNull @PathVariable String menuId,
			@Valid @NotNull @PathVariable String pluginId, Model model)
	{
		String contextUri = URI + '/' + menuId + '/' + pluginId;
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
		strBuilder.append(PluginController.PLUGIN_URI_PREFIX).append(pluginId);
		if (pathRemainder != null) strBuilder.append(pathRemainder);
		return strBuilder.toString();
	}

	/**
	 * Plugin without content
	 */
	@Controller
	@RequestMapping(VoidPluginController.URI)
	public static class VoidPluginController extends PluginController
	{
		public static final String ID = "void";
		public static final String URI = PluginController.PLUGIN_URI_PREFIX + ID;

		public VoidPluginController()
		{
			super(URI);
		}

		@GetMapping
		public String init()
		{
			return "view-void";
		}
	}
}
