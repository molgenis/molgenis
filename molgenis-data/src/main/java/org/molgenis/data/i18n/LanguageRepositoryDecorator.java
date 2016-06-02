package org.molgenis.data.i18n;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.i18n.I18nStringMetaData.I18N_STRING;
import static org.molgenis.data.i18n.LanguageMetaData.DEFAULT_LANGUAGE_CODE;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.ATTRIBUTE_META_DATA;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.DESCRIPTION;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.LABEL;
import static org.molgenis.data.meta.EntityMetaDataMetaData.ENTITY_META_DATA;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.molgenis.data.AggregateQuery;
import org.molgenis.data.AggregateResult;
import org.molgenis.data.DataService;
import org.molgenis.data.EntityListener;
import org.molgenis.data.Fetch;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Query;
import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCapability;
import org.molgenis.data.meta.AttributeMetaData;
import org.molgenis.data.meta.EntityMetaData;
import org.molgenis.data.meta.EntityMetaDataMetaData;
import org.molgenis.data.meta.system.SystemEntityMetaDataRegistry;

public class LanguageRepositoryDecorator implements Repository<Language>
{
	private final Repository<Language> decorated;
	private final DataService dataService;
	private final SystemEntityMetaDataRegistry systemEntityMetaDataRegistry;

	public LanguageRepositoryDecorator(Repository<Language> decorated, DataService dataService,
			SystemEntityMetaDataRegistry systemEntityMetaDataRegistry)
	{
		this.decorated = requireNonNull(decorated);
		this.dataService = requireNonNull(dataService);
		this.systemEntityMetaDataRegistry = requireNonNull(systemEntityMetaDataRegistry);
	}

	@Override
	public Iterator<Language> iterator()
	{
		return decorated.iterator();
	}

	@Override
	public Stream<Language> stream(Fetch fetch)
	{
		return decorated.stream(fetch);
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

		// Delete label-{languageCode} attr from AttributeMetaDataMetaData
		AttributeMetaData attributeLabel = getAttributeMetaDataMetaData().getAttribute(LABEL + '-' + languageCode);
		if (attributeLabel != null)
		{
			dataService.getMeta().getDefaultBackend().deleteAttribute(ATTRIBUTE_META_DATA, attributeLabel.getName());
		}

		// Delete description-{languageCode} attr from AttributeMetaDataMetaData
		AttributeMetaData attributeDescription = getAttributeMetaDataMetaData()
				.getAttribute(DESCRIPTION + '-' + languageCode);
		if (attributeDescription != null)
		{
			dataService.getMeta().getDefaultBackend()
					.deleteAttribute(ATTRIBUTE_META_DATA, attributeDescription.getName());
		}

		// Delete description-{languageCode} attr from EntityMetaDataMetaData
		AttributeMetaData entityDescription = getEntityMetaDataMetaData()
				.getAttribute(EntityMetaDataMetaData.DESCRIPTION + '-' + languageCode);
		if (entityDescription != null)
		{
			dataService.getMeta().getDefaultBackend().deleteAttribute(ENTITY_META_DATA, entityDescription.getName());
		}

		// Delete label-{languageCode} attr from EntityMetaDataMetaData
		AttributeMetaData entityLabel = getEntityMetaDataMetaData()
				.getAttribute(EntityMetaDataMetaData.LABEL + '-' + languageCode);
		if (entityLabel != null)
		{
			dataService.getMeta().getDefaultBackend().deleteAttribute(ENTITY_META_DATA, entityLabel.getName());
		}

		// Delete language attribute from I18nStringMetaData
		dataService.getMeta().getDefaultBackend().deleteAttribute(I18N_STRING, languageCode);
		getI18nStringMetaData().removeLanguage(languageCode);
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
		delete(this.stream());
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

			// Attribute label
			AttributeMetaData attrLabel = new AttributeMetaData(getAttributeMetaDataMetaData());
			attrLabel.setName(LABEL + '-' + languageCode).setNillable(true);

			// Add the attribute to the attributes table
			dataService.getMeta().getDefaultBackend().addAttribute(ATTRIBUTE_META_DATA, attrLabel);

			// Update AttributeMetaDataMetaData
			getAttributeMetaDataMetaData().addAttribute(attrLabel);

			// Attribute description
			AttributeMetaData attrDescription = new AttributeMetaData(getAttributeMetaDataMetaData());
			attrDescription.setName(DESCRIPTION + '-' + languageCode).setNillable(true);

			dataService.getMeta().getDefaultBackend().addAttribute(ATTRIBUTE_META_DATA, attrDescription);
			getAttributeMetaDataMetaData().addAttribute(attrDescription);

			// EntityMeta description
			AttributeMetaData entityDescription = new AttributeMetaData(getAttributeMetaDataMetaData());
			entityDescription.setName(EntityMetaDataMetaData.DESCRIPTION + '-' + languageCode).setNillable(true);

			dataService.getMeta().getDefaultBackend().addAttribute(ENTITY_META_DATA, entityDescription);
			getEntityMetaDataMetaData().addAttribute(entityDescription);

			// EntityMeta label
			AttributeMetaData entityLabel = new AttributeMetaData(getAttributeMetaDataMetaData());
			entityLabel.setName(EntityMetaDataMetaData.LABEL + '-' + languageCode).setNillable(true);

			dataService.getMeta().getDefaultBackend().addAttribute(ENTITY_META_DATA, entityLabel);
			getEntityMetaDataMetaData().addAttribute(entityLabel);

			// I18nString
			if (getI18nStringMetaData().addLanguage(languageCode))
			{
				dataService.getMeta().getDefaultBackend()
						.addAttribute(I18N_STRING, getI18nStringMetaData().getAttribute(languageCode));
			}
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

	@Override
	public void addEntityListener(EntityListener entityListener)
	{
		decorated.addEntityListener(entityListener);
	}

	@Override
	public void removeEntityListener(EntityListener entityListener)
	{
		decorated.removeEntityListener(entityListener);
	}

	private EntityMetaData getAttributeMetaDataMetaData()
	{
		return systemEntityMetaDataRegistry.getSystemEntityMetaData(ATTRIBUTE_META_DATA);
	}

	private EntityMetaData getEntityMetaDataMetaData()
	{
		return systemEntityMetaDataRegistry.getSystemEntityMetaData(ENTITY_META_DATA);
	}

	private I18nStringMetaData getI18nStringMetaData()
	{
		// FIXME hacky
		return (I18nStringMetaData) systemEntityMetaDataRegistry.getSystemEntityMetaData(I18N_STRING);
	}
}
