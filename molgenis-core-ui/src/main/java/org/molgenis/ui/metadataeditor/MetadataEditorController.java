package org.molgenis.ui.metadataeditor;

import com.google.common.collect.ImmutableList;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeMetadata;
import org.molgenis.ui.MolgenisPluginController;
import org.molgenis.ui.metadataeditor.model.EditorEntityType;
import org.molgenis.ui.metadataeditor.model.EditorEntityTypeResponse;
import org.molgenis.util.ErrorMessageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.i18n.LanguageService.getLanguageCodes;
import static org.molgenis.ui.metadataeditor.MetadataEditorController.URI;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@Controller
@RequestMapping(URI)
public class MetadataEditorController extends MolgenisPluginController
{
	private static final Logger LOG = LoggerFactory.getLogger(MetadataEditorController.class);

	public static final String ID = "metadataeditor";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;

	private final MetaDataService metadataService;
	private final EntityTypeMapper entityTypeMapper;

	@Autowired
	public MetadataEditorController(MetaDataService metadataService, EntityTypeMapper entityTypeMapper)
	{
		super(URI);
		this.metadataService = requireNonNull(metadataService);
		this.entityTypeMapper = requireNonNull(entityTypeMapper);
	}

	@RequestMapping(method = GET)
	public String init()
	{
		return "view-metadataeditor";
	}

	@RequestMapping(value = "/entityType/{id:.+}", method = GET)
	@ResponseBody
	public EditorEntityTypeResponse getEntityType(@PathVariable("id") String entityTypeId)
	{
		// FIXME metadataService.getEntityType does not return extendedBy
		//EntityType entityType = metadataService.getEntityType(entityTypeId);
		EntityType entityType = metadataService
				.getRepository(EntityTypeMetadata.ENTITY_TYPE_META_DATA, EntityType.class).findOneById(entityTypeId);
		if (entityType == null)
		{
			throw new UnknownEntityException(String.format("Unknown entity [%s]", entityTypeId));
		}
		EditorEntityType editorEntityType = EntityTypeMapper.toEditorEntityType(entityType);
		ImmutableList<String> languageCodes = ImmutableList.copyOf(getLanguageCodes().iterator());
		return EditorEntityTypeResponse.create(editorEntityType, languageCodes);
	}

	@RequestMapping(value = "/entityType", method = POST)
	@ResponseStatus(OK)
	public void updateEntityType(@RequestBody EditorEntityType editorEntityType)
	{
		EntityType entityType = entityTypeMapper.toEntityType(editorEntityType);
		metadataService.updateEntityType(entityType);
	}

	@ExceptionHandler(UnknownEntityException.class)
	@ResponseBody
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ErrorMessageResponse handleUnknownEntityException(UnknownEntityException e)
	{
		LOG.debug("", e);
		return new ErrorMessageResponse(
				Collections.singletonList(new ErrorMessageResponse.ErrorMessage(e.getMessage())));
	}
}
