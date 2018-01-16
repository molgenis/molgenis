package org.molgenis.metadata.manager.controller;

import org.molgenis.core.ui.controller.VuePluginController;
import org.molgenis.core.ui.menu.MenuReaderService;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.metadata.manager.model.EditorAttributeResponse;
import org.molgenis.metadata.manager.model.EditorEntityType;
import org.molgenis.metadata.manager.model.EditorEntityTypeResponse;
import org.molgenis.metadata.manager.model.EditorPackageIdentifier;
import org.molgenis.metadata.manager.service.MetadataManagerService;
import org.molgenis.security.user.UserAccountService;
import org.molgenis.settings.AppSettings;
import org.molgenis.web.ErrorMessageResponse;
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

@Controller
@RequestMapping(URI)
public class MetadataManagerController extends VuePluginController
{
	private static final Logger LOG = LoggerFactory.getLogger(MetadataManagerController.class);

	public static final String METADATA_MANAGER = "metadata-manager";
	public static final String URI = PLUGIN_URI_PREFIX + METADATA_MANAGER;

	private MetadataManagerService metadataManagerService;

	public MetadataManagerController(MenuReaderService menuReaderService, AppSettings appSettings,
			MetadataManagerService metadataManagerService, UserAccountService userAccountService)
	{
		super(URI, menuReaderService, appSettings, userAccountService);
		this.metadataManagerService = requireNonNull(metadataManagerService);

	}

	@GetMapping("/**")
	public String init(Model model)
	{
		super.init(model, METADATA_MANAGER);
		return "view-metadata-manager";
	}

	@ResponseBody
	@GetMapping(value = "/editorPackages", produces = "application/json")
	public List<EditorPackageIdentifier> getEditorPackages()
	{
		return metadataManagerService.getEditorPackages();
	}

	@ResponseBody
	@GetMapping(value = "/entityType/{id:.*}", produces = "application/json")
	public EditorEntityTypeResponse getEditorEntityType(@PathVariable("id") String id)
	{
		return metadataManagerService.getEditorEntityType(id);
	}

	@ResponseBody
	@GetMapping(value = "/create/entityType", produces = "application/json")
	public EditorEntityTypeResponse createEditorEntityType()
	{
		return metadataManagerService.createEditorEntityType();
	}

	@ResponseStatus(OK)
	@PostMapping(value = "/entityType", consumes = "application/json")
	public void upsertEntityType(@RequestBody EditorEntityType editorEntityType)
	{
		metadataManagerService.upsertEntityType(editorEntityType);
	}

	@ResponseBody
	@GetMapping(value = "/create/attribute", produces = "application/json")
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
}
