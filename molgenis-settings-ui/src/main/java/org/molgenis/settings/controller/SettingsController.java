package org.molgenis.settings.controller;

import org.molgenis.core.ui.controller.VuePluginController;
import org.molgenis.core.ui.menu.MenuReaderService;
import org.molgenis.data.DataService;
import org.molgenis.data.Query;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeMetadata;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.security.user.UserAccountService;
import org.molgenis.settings.AppSettings;
import org.molgenis.settings.SettingsEntityType;
import org.molgenis.settings.model.SettingsEntityResponse;
import org.molgenis.web.PluginController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static org.molgenis.settings.controller.SettingsController.URI;

@Controller
@RequestMapping(URI)
public class SettingsController extends VuePluginController

{
	public static final String ID = "settings";
	public static final String URI = PluginController.PLUGIN_URI_PREFIX + ID;

	public static final String VIEW_TEMPLATE = "view-settings";

	private DataService dataService;

	public SettingsController(MenuReaderService menuReaderService, AppSettings appSettings,
			UserAccountService userAccountService, DataService dataService)
	{
		super(URI, menuReaderService, appSettings, userAccountService);
		this.dataService = requireNonNull(dataService);
	}

	@GetMapping("/**")
	public String init(Model model)
	{
		super.init(model, ID);
		model.addAttribute("initialSelectedSetting", "sys_set_app");
		model.addAttribute("settingEntities", getSettingEntities());
		return VIEW_TEMPLATE;
	}

	private List<SettingsEntityResponse> getSettingEntities()
	{
		Query<EntityType> query = new QueryImpl<EntityType>().eq(EntityTypeMetadata.EXTENDS,
				SettingsEntityType.SETTINGS);

		return dataService.findAll(EntityTypeMetadata.ENTITY_TYPE_META_DATA, query, EntityType.class)
						  .map(entityType -> SettingsEntityResponse.create(entityType.getId(), entityType.getLabel()))
						  .collect(Collectors.toList());
	}
}
