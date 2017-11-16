package org.molgenis.data.validation;

import javax.annotation.Nullable;
import java.text.MessageFormat;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

/**
 * TODO discuss: split in two exceptions?
 */
public class NotNullConstraintViolationException extends DataIntegrityViolationException
{
	private static final String ERROR_CODE = "V04";
	private static final String MESSAGE_ID_ENTITY = ERROR_CODE + 'a';
	private static final String MESSAGE_ID_ENTITY_UNKNOWN = ERROR_CODE + 'b';

	private final String entityTypeId;
	private final String attributeName;
	private final String entityId;

	public NotNullConstraintViolationException(String entityTypeId, String attributeName)
	{
		this(entityTypeId, attributeName, null, null);
	}

	public NotNullConstraintViolationException(String entityTypeId, String attributeName, @Nullable Throwable cause)
	{
		this(entityTypeId, attributeName, null, cause);
	}

	public NotNullConstraintViolationException(String entityTypeId, String attributeName, @Nullable String entityId)
	{
		this(entityTypeId, attributeName, entityId, null);
	}

	public NotNullConstraintViolationException(String entityTypeId, String attributeName, @Nullable String entityId, @Nullable Throwable cause)
	{
		super(ERROR_CODE, cause);
		this.entityTypeId = requireNonNull(entityTypeId);
		this.attributeName = requireNonNull(attributeName);
		this.entityId = entityId;
	}

	@Override
	public String getMessage()
	{
		return String.format("type:%s attribute:%s entity:%s", entityTypeId, attributeName, entityId);
	}

	@Override
	public String getLocalizedMessage()
	{
		String localizedMessage;
		if (entityId != null)
		{
			localizedMessage = getLanguageService().map(
					languageService -> MessageFormat.format(languageService.getString(MESSAGE_ID_ENTITY), entityTypeId,
							attributeName, entityId)).orElse(super.getLocalizedMessage());
		}
		else
		{
			localizedMessage = getLanguageService().map(languageService -> MessageFormat.format(languageService.getString(MESSAGE_ID_ENTITY_UNKNOWN),
					entityTypeId, attributeName)).orElse(super.getLocalizedMessage());
		}
		return localizedMessage;
	}
}
