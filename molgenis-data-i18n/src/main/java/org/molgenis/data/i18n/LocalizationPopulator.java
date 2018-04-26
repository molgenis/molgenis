package org.molgenis.data.i18n;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.molgenis.data.i18n.model.L10nString;
import org.molgenis.data.i18n.model.L10nStringFactory;
import org.molgenis.i18n.LanguageService;
import org.molgenis.i18n.properties.AllPropertiesMessageSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.Objects.requireNonNull;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public class LocalizationPopulator
{
	private static final Logger LOG = LoggerFactory.getLogger(LocalizationPopulator.class);
	private final LocalizationService localizationService;
	private final L10nStringFactory l10nStringFactory;

	LocalizationPopulator(LocalizationService localizationService, L10nStringFactory l10nStringFactory)
	{
		this.localizationService = requireNonNull(localizationService);
		this.l10nStringFactory = requireNonNull(l10nStringFactory);
	}

	/**
	 * Adds all values in all namespaces from those specified in the property files for that namespace.
	 * <p>
	 * Already existing values are left exactly how they are.
	 * All new values are written to the repository.
	 * <p>
	 * If no {@link L10nString} exists yet for a certain messageID, a new one will be added.
	 */
	@Transactional
	public void populateLocalizationStrings(AllPropertiesMessageSource source)
	{
		source.getAllMessageIds()
			  .asMap()
			  .forEach((namespace, messageIds) -> updateNamespace(source, namespace, ImmutableSet.copyOf(messageIds)));
	}

	private void updateNamespace(AllPropertiesMessageSource source, String namespace, Set<String> messageIds)
	{
		Map<String, L10nString> toUpdate = localizationService.getExistingMessages(namespace, messageIds)
															  .collect(toMap(L10nString::getMessageID, identity()));
		Map<String, L10nString> toAdd = Sets.difference(messageIds, toUpdate.keySet())
											.stream()
											.map(msgId -> createL10nString(namespace, msgId))
											.collect(toMap(L10nString::getMessageID, identity()));
		Map<String, L10nString> all = Maps.asMap(messageIds,
				messageID -> toUpdate.getOrDefault(messageID, toAdd.get(messageID)));
		all.forEach((messageID, l10nString) -> updateFromSource(source, namespace, messageID, l10nString));
		localizationService.store(toUpdate.values(), toAdd.values());
	}

	private L10nString createL10nString(String namespace, String msgId)
	{
		L10nString result = l10nStringFactory.create(msgId);
		result.setMessageID(msgId);
		result.setNamespace(namespace);
		return result;
	}

	private void updateFromSource(AllPropertiesMessageSource source, String namespace, String messageID,
			L10nString l10nString)
	{
		for (String languageCode : LanguageService.getLanguageCodes().collect(toList()))
		{
			Locale locale = new Locale(languageCode);
			String message = source.resolveCodeWithoutArguments(messageID, locale);
			if (message != null && isNullOrEmpty(l10nString.getString(locale)))
			{
				LOG.debug("Setting {}.{}.{} to {}", namespace, messageID, languageCode, message);
				l10nString.set(languageCode, message);
			}
		}
	}
}
