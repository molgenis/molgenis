package org.molgenis.data.validation;

import org.molgenis.data.ErrorCoded;
import org.molgenis.data.MolgenisDataAccessException;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;
import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

/**
 * Thrown when deleting entity types that are still referenced by other entity types.
 */
public class EntityTypeReferenceConstraintViolationException extends MolgenisDataAccessException implements ErrorCoded
{
	private static final String ERROR_CODE = "V10";

	private final Map<String, Set<String>> entityTypeMap;

	public EntityTypeReferenceConstraintViolationException(Map<String, Set<String>> entityTypeMap, Throwable cause)
	{
		super(cause);
		this.entityTypeMap = requireNonNull(entityTypeMap);
	}

	@Override
	public String getMessage()
	{
		return entityTypeMap.entrySet().stream().map(this::getMessageRow).collect(joining(","));
	}

	private String getMessageRow(Map.Entry<String, Set<String>> entry)
	{
		String dependenciesString = entry.getValue().stream().collect(joining(","));
		return "type:" + entry.getKey() + " dependencies:[" + dependenciesString + "]";
	}

	@Override
	public String getLocalizedMessage()
	{
		try
		{
			String entityTypesAsString = entityTypeMap.keySet().stream().collect(joining(","));
			String entityTypeDependeniesAsString = entityTypeMap.values()
																.stream()
																.flatMap(Collection::stream)
																.collect(joining(","));
			return getLanguageService().map(languageService -> languageService.getString(ERROR_CODE))
									   .map(format -> MessageFormat.format(format, entityTypesAsString,
											   entityTypeDependeniesAsString))
									   .orElse(super.getLocalizedMessage());
		}
		catch (Exception e)
		{
			throw e;
		}
	}

	@Override
	public String getErrorCode()
	{
		return ERROR_CODE;
	}
}
