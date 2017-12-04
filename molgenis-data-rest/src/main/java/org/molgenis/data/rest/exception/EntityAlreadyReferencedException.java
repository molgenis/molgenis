package org.molgenis.data.rest.exception;

import org.molgenis.data.CodedRuntimeException;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;

import java.text.MessageFormat;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

public class EntityAlreadyReferencedException extends CodedRuntimeException
{
	public static String ERROR_CODE = "R11";
	private Entity refEntity;
	private Attribute refAttribute;
	private EntityType entityType;

	public EntityAlreadyReferencedException(Entity refEntity, Attribute refAttribute, EntityType entityType)
	{
		super(ERROR_CODE);
		this.refEntity = requireNonNull(refEntity);
		this.refAttribute = requireNonNull(refAttribute);
		this.entityType = requireNonNull(entityType);
	}

	@Override
	public String getMessage()
	{
		return String.format("refEntity:%s refAttribute:%s entityType:%s", refEntity, refAttribute, entityType);
	}

	@Override
	public String getLocalizedMessage()
	{
		return getLanguageService().map(languageService ->
		{
			String format = languageService.getString(ERROR_CODE);
			String language = languageService.getCurrentUserLanguageCode();
			return MessageFormat.format(format, refEntity.getEntityType().getLabel(language),
					refAttribute.getLabel(language), entityType.getLabel(language));
		}).orElse(super.getLocalizedMessage());
	}
}
