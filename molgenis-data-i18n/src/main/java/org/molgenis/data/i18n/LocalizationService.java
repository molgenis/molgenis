package org.molgenis.data.i18n;

import com.google.common.collect.Sets;
import org.molgenis.data.DataService;
import org.molgenis.data.i18n.model.L10nString;
import org.molgenis.data.i18n.model.L10nStringFactory;
import org.molgenis.i18n.MessageResolution;
import org.molgenis.security.core.runas.RunAsSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.*;
import static org.molgenis.data.i18n.model.L10nStringMetaData.*;

/**
 * Reads and writes messages to and from the {@link L10nString} entity.
 * <p>
 * The values returned are exactly how they are stored in the entity columns.
 */
public class LocalizationService implements MessageResolution
{
	private static final Logger LOG = LoggerFactory.getLogger(LocalizationService.class);
	private final DataService dataService;
	private final L10nStringFactory l10nStringFactory;

	LocalizationService(DataService dataService, L10nStringFactory l10nStringFactory)
	{
		this.dataService = requireNonNull(dataService);
		this.l10nStringFactory = requireNonNull(l10nStringFactory);
	}

	/**
	 * Looks up a single localized message.
	 *
	 * @param code   messageID
	 * @param locale the Locale for the language
	 * @return String containing the message or null if not specified
	 */
	@Override
	@RunAsSystem
	public String resolveCodeWithoutArguments(String code, Locale locale)
	{
		return Optional.ofNullable(dataService.query(L10N_STRING, L10nString.class).eq(MSGID, code).findOne())
					   .map(l10nString -> l10nString.getString(locale))
					   .orElse(null);
	}

	/**
	 * Gets all messages for a certain namespace and languageCode.
	 * Returns exactly those messages that are explicitly specified for this namespace and languageCode.
	 * Does not fall back to the default language.
	 *
	 * @param namespace the namespace for the messages
	 * @param locale    the Locale for the messages
	 * @return Map mapping messageID to message
	 */
	@RunAsSystem
	public Map<String, String> getMessages(String namespace, Locale locale)
	{
		return getL10nStrings(namespace).stream()
										.filter(e -> e.getString(locale) != null)
										.collect(toMap(L10nString::getMessageID, e -> e.getString(locale)));
	}

	public List<String> getAllMessageIds()
	{
		return dataService.findAll(L10N_STRING, L10nString.class).map(L10nString::getMessageID).collect(toList());
	}

	private List<L10nString> getL10nStrings(String namespace)
	{
		return dataService.query(L10N_STRING, L10nString.class).eq(NAMESPACE, namespace).findAll().collect(toList());
	}

	public Stream<L10nString> getExistingMessages(String namespace, Set<String> messageIds)
	{
		return dataService.query(L10N_STRING, L10nString.class)
						  .in(MSGID, messageIds)
						  .and()
						  .eq(NAMESPACE, namespace)
						  .findAll();
	}

	/**
	 * Adds Localization strings for missing messageIDs in a namespace.
	 * User needs write permission on the {@link L10nString} entity.
	 *
	 * @param namespace  the namespace to which the missing messageIDs should be added
	 * @param messageIDs the missing messageIDs to add.
	 */
	@Transactional
	public void addMissingMessageIds(String namespace, Set<String> messageIDs)
	{
		Set<String> alreadyPresent = getExistingMessages(namespace, messageIDs).map(L10nString::getMessageID)
																			   .collect(toCollection(TreeSet::new));
		Set<String> toAdd = Sets.difference(messageIDs, alreadyPresent);
		if (!toAdd.isEmpty())
		{
			Stream<L10nString> entities = toAdd.stream().map(key -> l10nStringFactory.create(key).setMessageID(key))
											   .map(l -> l.setNamespace(namespace));
			dataService.add(L10N_STRING, entities);
			LOG.debug("Added message IDs to namespace '{}' : {}.", namespace, messageIDs);
		}
	}

	/**
	 * Deletes all localization strings for a given namespace
	 */
	@Transactional
	public void deleteNamespace(String namespace)
	{
		List<L10nString> namespaceEntities = getL10nStrings(namespace);
		dataService.delete(L10N_STRING, namespaceEntities.stream());
	}

	public void store(Collection<L10nString> toUpdate, Collection<L10nString> toAdd)
	{
		dataService.update(L10N_STRING, toUpdate.stream());
		dataService.add(L10N_STRING, toAdd.stream());
	}
}
