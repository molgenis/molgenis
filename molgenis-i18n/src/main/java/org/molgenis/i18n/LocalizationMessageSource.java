package org.molgenis.i18n;

import org.molgenis.i18n.format.MessageFormatFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.support.AbstractMessageSource;

import javax.annotation.Nullable;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static org.molgenis.i18n.LanguageService.DEFAULT_LOCALE;

/**
 * The standard Molgenis {@link MessageSource} which looks up messages in the {@link MessageResolution}
 * and delegates {@link MessageFormat} creation to a {@link MessageFormatFactory}.
 * <p>
 * Marks missing values with # characters.
 * <p>
 * Caching should be done by the {@link MessageResolution}.
 */
public class LocalizationMessageSource extends AbstractMessageSource
{
	private final MessageFormatFactory messageFormatFactory;
	private final MessageResolution messageRepository;
	private final Supplier<Locale> fallbackLocaleSupplier;

	public LocalizationMessageSource(MessageFormatFactory messageFormatFactory, MessageResolution messageRepository,
			Supplier<Locale> fallbackLocaleSupplier)
	{
		super();
		this.messageFormatFactory = requireNonNull(messageFormatFactory);
		this.messageRepository = messageRepository;
		this.fallbackLocaleSupplier = fallbackLocaleSupplier;
		setAlwaysUseMessageFormat(false);
		setUseCodeAsDefaultMessage(false);
	}

	/**
	 * The default message adds # marks around the code so that they stand out as not yet translated.
	 *
	 * @param code the untranslated code
	 * @return the code surrounded by `#` characters.
	 */
	@Override
	public String getDefaultMessage(String code)
	{
		return "#" + code + "#";
	}

	/**
	 * Looks up the {@link MessageFormat} for a code.
	 *
	 * @param code   the code to look up
	 * @param locale the {@link Locale} for which the code should be looked up
	 * @return newly created {@link MessageFormat}
	 */
	@Override
	public MessageFormat resolveCode(String code, Locale locale)
	{
		String resolved = resolveCodeWithoutArguments(code, locale);
		if (resolved == null)
		{
			return null;
		}
		return createMessageFormat(resolved, locale);
	}

	@Override
	protected MessageFormat createMessageFormat(String msg, Locale locale)
	{
		return messageFormatFactory.createMessageFormat(msg, locale);
	}

	/**
	 * Looks up a code in the {@link MessageResolution}.
	 * <p>
	 * First tries the given locale if it is nonnull, then the fallbackLocale and finally the default locale.
	 *
	 * @param code   the messageID to look up.
	 * @param locale the Locale whose language code should be tried first, may be null
	 * @return The message, or null if none found.
	 */
	@Override
	protected String resolveCodeWithoutArguments(String code, @Nullable Locale locale)
	{
		Stream<Locale> candidates = Stream.of(locale, tryGetFallbackLocale(), DEFAULT_LOCALE);
		return candidates.filter(Objects::nonNull)
						 .map(candidate -> messageRepository.resolveCodeWithoutArguments(code, candidate))
						 .filter(Objects::nonNull)
						 .findFirst()
						 .orElse(null);
	}

	private Locale tryGetFallbackLocale()
	{
		Locale fallbackLocale = null;
		try
		{
			fallbackLocale = fallbackLocaleSupplier.get();
		}
		catch (RuntimeException ignore)
		{
		}
		return fallbackLocale;
	}
}
