package org.molgenis.data.i18n;

import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.util.ResourceUtils;

import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import static org.molgenis.data.i18n.LanguageService.getLanguageCodes;

/**
 * This {@link org.springframework.context.MessageSource} reads localization messages from properties files on the classpath.
 * <p>
 * Uses UTF-8 encoding.
 * <p>
 * N.B. If you want localization messages for a namespace to be picked up by the populator,
 * you need to create a {@link PropertiesMessageSource} bean for that namespace and add it to the context.
 */
public class PropertiesMessageSource extends ReloadableResourceBundleMessageSource
{
	private final String namespace;

	public PropertiesMessageSource(String namespace)
	{
		namespace = namespace.trim().toLowerCase();
		setBasename(ResourceUtils.CLASSPATH_URL_PREFIX + "l10n/" + namespace);
		setAlwaysUseMessageFormat(false);
		setFallbackToSystemLocale(false);
		setUseCodeAsDefaultMessage(false);
		setDefaultEncoding("UTF-8");

		this.namespace = namespace;
	}

	/**
	 * Returns the namespace of this {@link PropertiesMessageSource}
	 */
	public String getNamespace()
	{
		return namespace;
	}

	/**
	 * Retrieves all messageIDs for this PropertiesMessageSource.
	 *
	 * @return messageIDs present in any of the properties files for this namespace
	 */
	public Set<String> getMessageIDs()
	{
		return getLanguageCodes().flatMap(
				(languageCode) -> getMergedProperties(new Locale(languageCode)).getProperties().keySet().stream())
								 .map(Object::toString)
								 .collect(Collectors.toSet());
	}

	/**
	 * Retrieves a message for a specific language and code.
	 *
	 * @param language  language code for the message
	 * @param messageID the code of the message
	 * @return the message, or null if not specified
	 */
	public String getMessage(String language, String messageID)
	{
		return resolveCodeWithoutArguments(messageID, new Locale(language));
	}
}
