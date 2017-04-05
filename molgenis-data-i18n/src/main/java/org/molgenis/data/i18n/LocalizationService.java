package org.molgenis.data.i18n;

import com.google.common.collect.Sets;
import org.molgenis.data.DataService;
import org.molgenis.data.MolgenisDataAccessException;
import org.molgenis.data.Query;
import org.molgenis.data.i18n.model.I18nString;
import org.molgenis.data.i18n.model.I18nStringFactory;
import org.molgenis.data.i18n.model.I18nStringMetaData;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.security.core.runas.RunAsSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.Objects.requireNonNull;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.molgenis.data.i18n.model.I18nStringMetaData.I18N_STRING;
import static org.molgenis.data.i18n.model.I18nStringMetaData.MSGID;
import static org.molgenis.data.i18n.model.I18nStringMetaData.NAMESPACE;

/**
 * Reads and writes messages to and from the {@link I18nString} entity.
 */
@Service
public class LocalizationService
{
	public static final String NAMESPACE_ALL = "__ALL__";
	private static final Logger LOG = LoggerFactory.getLogger(LocalizationService.class);
	private final DataService dataService;
	private final I18nStringFactory i18nStringFactory;

	@Autowired
	public LocalizationService(DataService dataService, I18nStringFactory i18nStringFactory)
	{
		this.dataService = requireNonNull(dataService);
		this.i18nStringFactory = requireNonNull(i18nStringFactory);
	}

	/**
	 * Looks up a single localized message.
	 *
	 * @param namespace    namespace for the message
	 * @param messageId    messageID
	 * @param languageCode code for the language
	 * @return String containing the message or null if not specified
	 */
	@RunAsSystem
	public String getMessage(String namespace, String messageId, String languageCode)
	{
		Query<I18nString> query = new QueryImpl<I18nString>().eq(MSGID, messageId);
		if (!NAMESPACE_ALL.equals(namespace))
		{
			query = query.eq(NAMESPACE, namespace);
		}
		I18nString i18nString = dataService.findOne(I18N_STRING, query, I18nString.class);
		if (i18nString == null)
		{
			return null;
		}
		return i18nString.getString(languageCode);
	}

	private List<I18nString> getI18nStrings(String namespace)
	{
		Query<I18nString> query = new QueryImpl<>();
		if (!NAMESPACE_ALL.equals(namespace))
		{
			query = query.eq(NAMESPACE, namespace);
		}
		return findI18nStrings(query);
	}

	private List<I18nString> findI18nStrings(Query<I18nString> query)
	{
		return dataService.findAll(I18N_STRING, query, I18nString.class).collect(Collectors.toList());
	}

	/**
	 * Gets all messages for a certain namespace and languageCode.
	 * Returns exactly those messages that are explicitly specified for this namespace and languageCode.
	 * Does not fall back to the default language.
	 *
	 * @param namespace    the namespace for the messages
	 * @param languageCode the languageCode for the messages
	 * @return Map mapping messageID to message
	 */
	@RunAsSystem
	public Map<String, String> getMessages(String namespace, String languageCode)
	{
		return getI18nStrings(namespace).stream().filter(e -> e.getString(languageCode) != null)
				.collect(toMap(I18nString::getMessageId, e -> e.getString(languageCode)));
	}

	/**
	 * Adds all values in a namespace from those specified in the property files for that namespace.
	 * No already existing values are overwritten.
	 * All new values are written, if no {@link I18nString} exists yet for a certain messageID, it
	 * gets created.
	 *
	 * @param messageSource {@link PropertiesMessageSource} for that namespace
	 */
	public void updateAllLanguages(PropertiesMessageSource messageSource)
	{
		String namespace = messageSource.getNamespace();
		Set<String> messageIDs = messageSource.getMessageIDs();

		Map<String, I18nString> toUpdate = dataService
				.findAll(I18N_STRING, QueryImpl.IN(MSGID, messageIDs).and().eq(I18nStringMetaData.NAMESPACE, namespace))
				.map(i18nStringFactory::create).collect(toMap(I18nString::getMessageId, identity()));

		Map<String, I18nString> toAdd = Sets.difference(messageIDs, toUpdate.keySet()).stream().map(msgId ->
		{
			I18nString result = i18nStringFactory.create();
			result.setMessageId(msgId);
			result.setNamespace(namespace);
			return result;
		}).collect(toMap(I18nString::getMessageId, identity()));

		for (String messageID : messageIDs)
		{
			I18nString i18nString = toUpdate.getOrDefault(messageID, toAdd.get(messageID));
			for (String languageCode : LanguageService.getLanguageCodes().collect(toList()))
			{
				String message = messageSource.getMessage(languageCode, messageID);
				if (message != null && isNullOrEmpty(i18nString.getString(languageCode)))
				{
					LOG.info("Setting {}.{}.{} to {}", namespace, messageID, languageCode, message);
					i18nString.set(languageCode, message);
				}
			}
		}
		dataService.update(I18N_STRING, toUpdate.values().stream());
		dataService.add(I18N_STRING, toAdd.values().stream());
	}

	@Transactional
	public void addMissingMessageIDs(String namespace, Set<String> messageIDs)
	{
		Set<String> alreadyPresent = dataService
				.findAll(I18N_STRING, QueryImpl.IN(MSGID, messageIDs).and().eq(I18nStringMetaData.NAMESPACE, namespace))
				.map(e -> e.getString(MSGID)).collect(toSet());

		Set<String> toAdd = Sets.difference(messageIDs, alreadyPresent);
		if (!toAdd.isEmpty())
		{
			Stream<I18nString> entities = toAdd.stream()
					.map(key -> i18nStringFactory.create().setMessageId(key).setNamespace(namespace));
			try
			{
				dataService.add(I18N_STRING, entities);
				LOG.info("Added message IDs to namespace '{}' : {}.", messageIDs);
			}
			catch (MolgenisDataAccessException ex)
			{
				LOG.info("No permission to add message IDs to namespace '{}' : {}.", messageIDs, ex);
			}
		}
	}

	@RunAsSystem
	public Set<String> getKeys(String namespace)
	{
		return getI18nStrings(namespace).stream().map(I18nString::getMessageId).collect(Collectors.toSet());
	}
}
