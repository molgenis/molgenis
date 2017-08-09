package org.molgenis.data.i18n;

import com.google.common.collect.Sets;
import org.molgenis.data.DataService;
import org.molgenis.data.MolgenisDataAccessException;
import org.molgenis.data.Query;
import org.molgenis.data.i18n.model.L10nString;
import org.molgenis.data.i18n.model.L10nStringFactory;
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
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.Objects.requireNonNull;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.*;
import static org.molgenis.data.i18n.model.L10nStringMetaData.*;

/**
 * Reads and writes messages to and from the {@link L10nString} entity.
 * <p>
 * The values returned are exactly how they are stored in the entity columns.
 */
@Service
public class LocalizationService
{
	public static final String NAMESPACE_ALL = "__ALL__";
	private static final Logger LOG = LoggerFactory.getLogger(LocalizationService.class);
	private final DataService dataService;
	private final L10nStringFactory l10nStringFactory;

	@Autowired
	public LocalizationService(DataService dataService, L10nStringFactory l10nStringFactory)
	{
		this.dataService = requireNonNull(dataService);
		this.l10nStringFactory = requireNonNull(l10nStringFactory);
	}

	/**
	 * Looks up a single localized message.
	 *
	 * @param namespace    namespace for the message
	 * @param messageID    messageID
	 * @param languageCode code for the language
	 * @return String containing the message or null if not specified
	 */
	@RunAsSystem
	public String getMessage(String namespace, String messageID, String languageCode)
	{
		Query<L10nString> query = new QueryImpl<L10nString>().eq(MSGID, messageID);
		if (!NAMESPACE_ALL.equals(namespace))
		{
			query = query.and().eq(NAMESPACE, namespace);
		}
		L10nString i18nString = dataService.findOne(L10N_STRING, query, L10nString.class);
		if (i18nString == null)
		{
			return null;
		}
		return i18nString.getString(languageCode);
	}

	private List<L10nString> getL10nStrings(String namespace)
	{
		Query<L10nString> query = new QueryImpl<>();
		if (!NAMESPACE_ALL.equals(namespace))
		{
			query = query.eq(NAMESPACE, namespace);
		}
		return dataService.findAll(L10N_STRING, query, L10nString.class).collect(Collectors.toList());
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
		return getL10nStrings(namespace).stream()
										.filter(e -> e.getString(languageCode) != null)
										.collect(toMap(L10nString::getMessageID, e -> e.getString(languageCode)));
	}

	/**
	 * Adds all values in a namespace from those specified in the property files for that namespace.
	 * <p>
	 * Already existing values are left exactly how they are.
	 * All new values are written to the repository.
	 * <p>
	 * If no {@link L10nString} exists yet for a certain messageID, a new one will be added.
	 *
	 * @param messageSource the {@link PropertiesMessageSource} for the namespace that is added
	 */
	public void populateLocalizationStrings(PropertiesMessageSource messageSource)
	{
		String namespace = messageSource.getNamespace();
		Set<String> messageIDs = messageSource.getMessageIDs();

		Stream<L10nString> stream = dataService.findAll(L10N_STRING,
				new QueryImpl<L10nString>().in(MSGID, messageIDs).and().eq(NAMESPACE, namespace), L10nString.class);
		Map<String, L10nString> toUpdate = stream.map(l10nStringFactory::create)
												 .collect(toMap(L10nString::getMessageID, identity()));

		Map<String, L10nString> toAdd = Sets.difference(messageIDs, toUpdate.keySet()).stream().map(msgId ->
		{
			L10nString result = l10nStringFactory.create();
			result.setMessageID(msgId);
			result.setNamespace(namespace);
			return result;
		}).collect(toMap(L10nString::getMessageID, identity()));

		for (String messageID : messageIDs)
		{
			L10nString i18nString = toUpdate.getOrDefault(messageID, toAdd.get(messageID));
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
		dataService.update(L10N_STRING, toUpdate.values().stream());
		dataService.add(L10N_STRING, toAdd.values().stream());
	}

	/**
	 * Adds Localization strings for missing messageIDs in a namespace.
	 * User needs write permission on the {@link L10nString} entity.
	 *
	 * @param namespace  the namespace to which the missing messageIDs should be added
	 * @param messageIDs the missing messageIDs to add.
	 */
	@Transactional
	public void addMissingMessageIDs(String namespace, Set<String> messageIDs)
	{
		Set<String> alreadyPresent = dataService.findAll(L10N_STRING,
				new QueryImpl<L10nString>().in(MSGID, messageIDs).and().eq(NAMESPACE, namespace), L10nString.class)
												.map(L10nString::getMessageID)
												.collect(toCollection(TreeSet::new));

		Set<String> toAdd = Sets.difference(messageIDs, alreadyPresent);
		if (!toAdd.isEmpty())
		{
			Stream<L10nString> entities = toAdd.stream()
											   .map(key -> l10nStringFactory.create()
																			.setMessageID(key)
																			.setNamespace(namespace));
			try
			{
				dataService.add(L10N_STRING, entities);
				LOG.info("Added message IDs to namespace '{}' : {}.", namespace, messageIDs);
			}
			catch (MolgenisDataAccessException ex)
			{
				LOG.info("No permission to add message IDs to namespace '{}' : {}.", namespace, messageIDs, ex);
			}
		}
	}

	/**
	 * Returns all messageIDs in a namespace.
	 *
	 * @param namespace the name of the namespace
	 * @return Set of messageIDs
	 */
	@RunAsSystem
	public Set<String> getMessageIDs(String namespace)
	{
		return getL10nStrings(namespace).stream().map(L10nString::getMessageID).collect(toCollection(TreeSet::new));
	}

	/**
	 * Deletes all localization strings for a given namespace
	 *
	 * @param namespace
	 */
	@Transactional
	@RunAsSystem
	public void deleteNameSpace(String namespace)
	{
		List<L10nString> namespaceEntities = getL10nStrings(namespace);
		dataService.delete(L10N_STRING, namespaceEntities.stream());
	}
}
