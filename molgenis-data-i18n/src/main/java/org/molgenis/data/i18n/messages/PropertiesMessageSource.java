package org.molgenis.data.i18n.messages;

import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.util.ResourceUtils;

import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import static org.molgenis.data.i18n.LanguageService.getLanguageCodes;

/**
 * This {@link org.springframework.context.MessageSource} reads messages from properties files on the classpath.
 */
public class PropertiesMessageSource extends ReloadableResourceBundleMessageSource
{
	private final String namespace;

	public PropertiesMessageSource(String namespace)
	{
		setBasename(ResourceUtils.CLASSPATH_URL_PREFIX + "l10n/" + namespace);
		setAlwaysUseMessageFormat(false);
		setFallbackToSystemLocale(false);
		setUseCodeAsDefaultMessage(false);
		this.namespace = namespace;
	}

	public String getNamespace()
	{
		return namespace;
	}

	/**
	 * Retrieves all message codes for this PropertiesMessageSource.
	 *
	 * @return message codes present in any of the properties files for this namespace
	 */
	public Set<String> getCodes()
	{
		return getLanguageCodes().flatMap(
				(languageCode) -> getMergedProperties(new Locale(languageCode)).getProperties().keySet().stream())
				.map(Object::toString).collect(Collectors.toSet());
	}

	/**
	 * Retrieves a message for a specific language and code.
	 *
	 * @param language Language code for which to retrieve the message
	 * @param code     the code of the message
	 * @return the message, or null if not specified
	 */
	public String getMessage(String language, String code)
	{
		return resolveCodeWithoutArguments(code, new Locale(language));
	}
}
