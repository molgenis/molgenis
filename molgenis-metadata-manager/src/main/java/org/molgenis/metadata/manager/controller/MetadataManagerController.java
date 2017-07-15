package org.molgenis.metadata.manager.controller;

import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.i18n.LanguageService;
import org.molgenis.data.settings.AppSettings;
import org.molgenis.metadata.manager.model.EditorAttributeResponse;
import org.molgenis.metadata.manager.model.EditorEntityType;
import org.molgenis.metadata.manager.model.EditorEntityTypeResponse;
import org.molgenis.metadata.manager.model.EditorPackageIdentifier;
import org.molgenis.metadata.manager.service.MetadataManagerService;
import org.molgenis.ui.MolgenisPluginController;
import org.molgenis.ui.menu.MenuReaderService;
import org.molgenis.util.ErrorMessageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static org.molgenis.metadata.manager.controller.MetadataManagerController.URI;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@Controller
@RequestMapping(URI)
public class MetadataManagerController extends MolgenisPluginController
{
	private static final Logger LOG = LoggerFactory.getLogger(MetadataManagerController.class);

	public static final String METADATA_MANAGER = "metadata-manager";
	public static final String URI = PLUGIN_URI_PREFIX + METADATA_MANAGER;

	private MenuReaderService menuReaderService;
	private LanguageService languageService;
	private AppSettings appSettings;
	private MetadataManagerService metadataManagerService;

	public MetadataManagerController(MenuReaderService menuReaderService, LanguageService languageService,
			AppSettings appSettings, MetadataManagerService metadataManagerService)
	{
		super(URI);
		this.menuReaderService = requireNonNull(menuReaderService);
		this.languageService = requireNonNull(languageService);
		this.appSettings = requireNonNull(appSettings);
		this.metadataManagerService = requireNonNull(metadataManagerService);
	}

	// Place '/**' annotation on method instead of class to avoid wrongly matching of URLs with a path variable
	@RequestMapping(value = "/**", method = GET)
	public String init(Model model)
	{
		model.addAttribute("lng", languageService.getCurrentUserLanguageCode());
		model.addAttribute("fallbackLng", appSettings.getLanguageCode());
		model.addAttribute("baseUrl", getBaseUrl());
		return "view-metadata-manager";
	}

	@ResponseBody
	@RequestMapping(value = "/editorPackages", method = GET, produces = "application/json")
	public List<EditorPackageIdentifier> getEditorPackages()
	{
		return metadataManagerService.getEditorPackages();
	}

	@ResponseBody
	@RequestMapping(value = "/entityType/{id:.*}", method = GET, produces = "application/json")
	public EditorEntityTypeResponse getEditorEntityType(@PathVariable("id") String id)
	{
		return metadataManagerService.getEditorEntityType(id);
	}

	@ResponseBody
	@RequestMapping(value = "/create/entityType", method = GET, produces = "application/json")
	public EditorEntityTypeResponse createEditorEntityType()
	{
		return metadataManagerService.createEditorEntityType();
	}

	@ResponseStatus(OK)
	@RequestMapping(value = "/entityType", method = POST, consumes = "application/json")
	public void upsertEntityType(@RequestBody EditorEntityType editorEntityType)
	{
		metadataManagerService.upsertEntityType(editorEntityType);
	}

	@ResponseBody
	@RequestMapping(value = "/create/attribute", method = GET, produces = "application/json")
	public EditorAttributeResponse createEditorAttribute()
	{
		return metadataManagerService.createEditorAttribute();
	}

	@ResponseBody
	@ResponseStatus(BAD_REQUEST)
	@ExceptionHandler(UnknownEntityException.class)
	public ErrorMessageResponse handleUnknownEntityException(UnknownEntityException e)
	{
		LOG.debug("", e);
		return new ErrorMessageResponse(singletonList(new ErrorMessageResponse.ErrorMessage(e.getMessage())));
	}

	@ResponseBody
	@ResponseStatus(INTERNAL_SERVER_ERROR)
	@ExceptionHandler(RuntimeException.class)
	public ErrorMessageResponse handleRuntimeException(RuntimeException e)
	{
		LOG.error("", e);
		return new ErrorMessageResponse(singletonList(new ErrorMessageResponse.ErrorMessage(e.getMessage())));
	}

	private String getBaseUrl()
	{
		return menuReaderService.getMenu().findMenuItemPath(MetadataManagerController.METADATA_MANAGER);
	}
}
