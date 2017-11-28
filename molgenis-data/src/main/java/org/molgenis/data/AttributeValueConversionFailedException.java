package org.molgenis.data;

import org.molgenis.data.meta.model.Attribute;

import java.text.MessageFormat;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

/**
 * Thrown on mismatch between value type and attribute type.
 */
public class AttributeValueConversionFailedException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "D10";

	private final Attribute attr;
	private final Object value;
	private Optional<Exception> exception;

	public AttributeValueConversionFailedException(Attribute attr, Object value, Exception exception)
	{
		super(ERROR_CODE);
		this.attr = requireNonNull(attr);
		this.value = requireNonNull(value);
		this.exception = Optional.of(exception);
	}

	public AttributeValueConversionFailedException(Attribute attr, Object value)
	{
		super(ERROR_CODE);
		this.attr = requireNonNull(attr);
		this.value = requireNonNull(value);
		this.exception = Optional.empty();
	}

	public Attribute getAttr()
	{
		return attr;
	}

	public Object getValue()
	{
		return value;
	}

	public Optional<Exception> getException()
	{
		return exception;
	}

	public void setException(Optional<Exception> exception)
	{
		this.exception = exception;
	}

	@Override
	public String getMessage()
	{
		Throwable t = exception.isPresent() ? exception.get().getCause() : null;
		return String.format("type:%s attribute:%s expected:%s actual:%s value:%s, cause:%s", attr.getEntity().getId(),
				attr.getName(), attr.getDataType().name(), value.getClass().getName(), value.toString(), t);
	}

	@Override
	public String getLocalizedMessage()
	{
		return getLanguageService().map(languageService ->
		{
			final String languageCode = languageService.getCurrentUserLanguageCode();
			return MessageFormat.format(languageService.getString(ERROR_CODE), attr.getEntity().getLabel(languageCode),
					attr.getLabel(languageCode), value.toString(),
					attr.getDataType().toString());
		}).orElseGet(super::getLocalizedMessage);
	}
}
