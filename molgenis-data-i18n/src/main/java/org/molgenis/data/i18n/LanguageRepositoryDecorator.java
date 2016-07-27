package org.molgenis.data.i18n;

import org.molgenis.data.*;
import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.i18n.model.I18nStringMetaData;
import org.molgenis.data.i18n.model.Language;
import org.molgenis.data.i18n.model.LanguageMetaData;
import org.molgenis.data.listeners.EntityListener;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.AttributeMetaDataFactory;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.meta.model.EntityMetaDataMetaData;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static org.molgenis.MolgenisFieldTypes.AttributeType.TEXT;
import static org.molgenis.data.i18n.model.I18nStringMetaData.I18N_STRING;
import static org.molgenis.data.i18n.model.LanguageMetaData.DEFAULT_LANGUAGE_CODE;
import static org.molgenis.data.meta.model.AttributeMetaDataMetaData.*;
import static org.molgenis.data.meta.model.EntityMetaDataMetaData.ENTITY_META_DATA;
import static org.molgenis.security.core.runas.RunAsSystemProxy.runAsSystem;

public class LanguageRepositoryDecorator implements Repository<Language>
{
	private final Repository<Language> decorated;
	private final DataService dataService;
	private final AttributeMetaDataFactory attrMetaFactory;
	private final EntityMetaDataMetaData entityMetaMeta;
	private final I18nStringMetaData i18nStringMeta;

	public LanguageRepositoryDecorator(Repository<Language> decorated, DataService dataService,
			AttributeMetaDataFactory attrMetaFactory, EntityMetaDataMetaData entityMetaMeta,
			I18nStringMetaData i18nStringMeta)
	{
		this.decorated = requireNonNull(decorated);
		this.dataService = requireNonNull(dataService);
		this.attrMetaFactory = requireNonNull(attrMetaFactory);
		this.entityMetaMeta = requireNonNull(entityMetaMeta);
		this.i18nStringMeta = requireNonNull(i18nStringMeta);
	}

	@Override
	public Iterator<Language> iterator()
	{
		return decorated.iterator();
	}

	@Override
	public void forEachBatched(Fetch fetch, Consumer<List<Language>> consumer, int batchSize)
	{
		decorated.forEachBatched(fetch, consumer, batchSize);
	}

	@Override
	public void close() throws IOException
	{
		decorated.close();
	}

	@Override
	public Set<RepositoryCapability> getCapabilities()
	{
		return decorated.getCapabilities();
	}

	@Override
	public Set<Operator> getQueryOperators()
	{
		return decorated.getQueryOperators();
	}

	@Override
	public String getName()
	{
		return decorated.getName();
	}

	@Override
	public EntityMetaData getEntityMetaData()
	{
		return decorated.getEntityMetaData();
	}

	@Override
	public long count()
	{
		return decorated.count();
	}

	@Override
	public Query<Language> query()
	{
		return decorated.query();
	}

	@Override
	public long count(Query<Language> q)
	{
		return decorated.count(q);
	}

	@Override
	public Stream<Language> findAll(Query<Language> q)
	{
		return decorated.findAll(q);
	}

	@Override
	public Language findOne(Query<Language> q)
	{
		return decorated.findOne(q);
	}

	@Override
	public Language findOneById(Object id)
	{
		return decorated.findOneById(id);
	}

	@Override
	public Language findOneById(Object id, Fetch fetch)
	{
		return decorated.findOneById(id, fetch);
	}

	@Override
	public Stream<Language> findAll(Stream<Object> ids)
	{
		return decorated.findAll(ids);
	}

	@Override
	public Stream<Language> findAll(Stream<Object> ids, Fetch fetch)
	{
		return decorated.findAll(ids, fetch);
	}

	@Override
	public AggregateResult aggregate(AggregateQuery aggregateQuery)
	{
		return decorated.aggregate(aggregateQuery);
	}

	@Override
	public void update(Language entity)
	{
		decorated.update(entity);
	}

	@Override
	public void update(Stream<Language> entities)
	{
		decorated.update(entities);
	}

	@Override
	public void delete(Language language)
	{
		String languageCode = language.getCode();
		if (languageCode.equalsIgnoreCase(LanguageMetaData.DEFAULT_LANGUAGE_CODE))
		{
			throw new MolgenisDataException(
					"It is not possible to delete '" + languageCode + "'. This is the default language.");
		}

		decorated.delete(language);

		// remove i18n attributes from i18n string meta data
		AttributeMetaData attrLanguageCode = i18nStringMeta.getAttribute(languageCode);

		EntityMetaData i18nMeta = EntityMetaData.newInstance(dataService.getEntityMetaData(I18nStringMetaData.I18N_STRING));
		i18nMeta.removeAttribute(attrLanguageCode);

		runAsSystem(() -> dataService.update(ENTITY_META_DATA, i18nMeta));

		// hack: update in memory representation
		EntityMetaData i18nMetaUpdated = dataService.getEntityMetaData(I18N_STRING);
		i18nMetaUpdated.removeAttribute(attrLanguageCode);

		// remove i18n attributes from entity meta data
		AttributeMetaData entityLabel = entityMetaMeta.getAttribute(LABEL + '-' + languageCode);
		AttributeMetaData entityDescription = entityMetaMeta.getAttribute(DESCRIPTION + '-' + languageCode);

		EntityMetaData entityMeta = EntityMetaData.newInstance(dataService.getEntityMetaData(ENTITY_META_DATA));
		entityMeta.removeAttribute(entityLabel);
		entityMeta.removeAttribute(entityDescription);

		runAsSystem(() -> dataService.update(ENTITY_META_DATA, entityMeta));

		// hack: update in memory representation
		EntityMetaData entityMetaUpdated = dataService.getEntityMetaData(ENTITY_META_DATA);
		entityMetaUpdated.removeAttribute(entityLabel);
		entityMetaUpdated.removeAttribute(entityDescription);

		// remove i18n attributes from attribute meta data
		EntityMetaData attrMetaMeta = attrMetaFactory.getAttributeMetaDataMetaData();
		AttributeMetaData attrLabel = attrMetaMeta.getAttribute(LABEL + '-' + languageCode);
		AttributeMetaData attrDescription = attrMetaMeta.getAttribute(DESCRIPTION + '-' + languageCode);

		EntityMetaData attrMeta = EntityMetaData.newInstance(dataService.getEntityMetaData(ATTRIBUTE_META_DATA));
		attrMeta.removeAttribute(attrLabel);
		attrMeta.removeAttribute(attrDescription);

		runAsSystem(() -> dataService.update(ENTITY_META_DATA, attrMeta));

		// hack: update in memory representation
		EntityMetaData attrMetaUpdated = dataService.getEntityMetaData(ATTRIBUTE_META_DATA);
		attrMetaUpdated.removeAttribute(attrLabel);
		attrMetaUpdated.removeAttribute(attrDescription);
	}

	@Override
	public void delete(Stream<Language> entities)
	{
		entities.forEach(this::delete);
	}

	@Override
	public void deleteById(Object id)
	{
		Language entity = findOneById(id);
		if (entity != null) delete(entity);
	}

	@Override
	public void deleteAll(Stream<Object> ids)
	{
		ids.forEach(this::deleteById);
	}

	@Override
	public void deleteAll()
	{
		forEachBatched(entities -> delete(entities.stream()), 1000);
	}

	@Override
	public void add(Language language)
	{
		// Add language
		decorated.add(language);

		if (!language.getCode().equals(DEFAULT_LANGUAGE_CODE))
		{
			// Create new label and description attributes for the added language
			String languageCode = language.getCode();

			// Add language attributes for attribute meta data
			AttributeMetaData attrLabel = attrMetaFactory.create().setName(LABEL + '-' + languageCode).setNillable(true)
					.setLabel("Label (" + languageCode + ')');
			AttributeMetaData attrDescription = attrMetaFactory.create().setName(DESCRIPTION + '-' + languageCode)
					.setNillable(true).setLabel("Description (" + languageCode + ')');
			dataService.add(ATTRIBUTE_META_DATA, Stream.of(attrLabel, attrDescription));

			EntityMetaData attrMeta = EntityMetaData.newInstance(dataService.getEntityMetaData(ATTRIBUTE_META_DATA));
			attrMeta.addAttribute(attrLabel);
			attrMeta.addAttribute(attrDescription);

			runAsSystem(() -> dataService.update(ENTITY_META_DATA, attrMeta));

			// hack: update in memory representation
			EntityMetaData attrMetaUpdated = dataService.getEntityMetaData(ATTRIBUTE_META_DATA);
			attrMetaUpdated.addAttribute(attrLabel);
			attrMetaUpdated.addAttribute(attrDescription);

			// Add language attributes for entity meta data
			AttributeMetaData entityLabel = attrMetaFactory.create()
					.setName(EntityMetaDataMetaData.LABEL + '-' + languageCode).setNillable(true)
					.setLabel("Label (" + languageCode + ')');
			AttributeMetaData entityDescription = attrMetaFactory.create()
					.setName(EntityMetaDataMetaData.DESCRIPTION + '-' + languageCode).setNillable(true)
					.setLabel("Description (" + languageCode + ')');
			dataService.add(ATTRIBUTE_META_DATA, Stream.of(entityLabel, entityDescription));

			EntityMetaData entityMeta = EntityMetaData.newInstance(dataService.getEntityMetaData(ENTITY_META_DATA));
			entityMeta.addAttribute(entityLabel);
			entityMeta.addAttribute(entityDescription);

			runAsSystem(() -> dataService.update(ENTITY_META_DATA, entityMeta));

			// hack: update in memory representation
			EntityMetaData entityMetaUpdated = dataService.getEntityMetaData(ENTITY_META_DATA);
			entityMetaUpdated.addAttribute(entityLabel);
			entityMetaUpdated.addAttribute(entityDescription);


			// remove i18n attributes from i18n string meta data
			//AttributeMetaData attrLanguageCode = i18nStringMeta.getAttribute(languageCode);

			// Add language attributes for i18n string
			AttributeMetaData languageCodeAttr = attrMetaFactory.create().setName(languageCode).setNillable(true)
					.setDataType(TEXT);
			dataService.add(ATTRIBUTE_META_DATA, languageCodeAttr);

			EntityMetaData i18nMeta = EntityMetaData.newInstance(dataService.getEntityMetaData(I18N_STRING));
			i18nMeta.addAttribute(languageCodeAttr);

			runAsSystem(() -> dataService.update(ENTITY_META_DATA, i18nMeta));

			// hack: update in memory representation
			EntityMetaData i18nMetaUpdated = dataService.getEntityMetaData(I18N_STRING);
			i18nMetaUpdated.addAttribute(languageCodeAttr);
		}
	}

	@Override
	public Integer add(Stream<Language> entities)
	{
		AtomicInteger count = new AtomicInteger();
		entities.forEach(entity -> {
			add(entity); // FIXME inefficient, apply filter to stream
			count.incrementAndGet();
		});
		return count.get();
	}

	@Override
	public void flush()
	{
		decorated.flush();
	}

	@Override
	public void clearCache()
	{
		decorated.clearCache();
	}

	@Override
	public void rebuildIndex()
	{
		decorated.rebuildIndex();
	}
}
