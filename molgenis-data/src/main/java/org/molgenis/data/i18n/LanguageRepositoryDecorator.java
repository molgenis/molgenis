package org.molgenis.data.i18n;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.molgenis.data.AggregateQuery;
import org.molgenis.data.AggregateResult;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityListener;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Fetch;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Query;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCapability;
import org.molgenis.data.meta.AttributeMetaDataMetaData;
import org.molgenis.data.meta.EntityMetaDataMetaData;
import org.molgenis.data.support.DefaultAttributeMetaData;

public class LanguageRepositoryDecorator implements Repository
{
	private final Repository decorated;
	private final DataService dataService;

	public LanguageRepositoryDecorator(Repository decorated, DataService dataService)
	{
		this.decorated = decorated;
		this.dataService = dataService;
	}

	@Override
	public Iterator<Entity> iterator()
	{
		return decorated.iterator();
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
	public Query query()
	{
		return decorated.query();
	}

	@Override
	public long count(Query q)
	{
		return decorated.count(q);
	}

	@Override
	public Stream<Entity> findAll(Query q)
	{
		return decorated.findAll(q);
	}

	@Override
	public Entity findOne(Query q)
	{
		return decorated.findOne(q);
	}

	@Override
	public Entity findOne(Object id)
	{
		return decorated.findOne(id);
	}

	@Override
	public Entity findOne(Object id, Fetch fetch)
	{
		return decorated.findOne(id, fetch);
	}

	@Override
	public Stream<Entity> findAll(Stream<Object> ids)
	{
		return decorated.findAll(ids);
	}

	@Override
	public Stream<Entity> findAll(Stream<Object> ids, Fetch fetch)
	{
		return decorated.findAll(ids, fetch);
	}

	@Override
	public AggregateResult aggregate(AggregateQuery aggregateQuery)
	{
		return decorated.aggregate(aggregateQuery);
	}

	@Override
	public void update(Entity entity)
	{
		decorated.update(entity);
	}

	@Override
	public void update(Stream<? extends Entity> entities)
	{
		decorated.update(entities);
	}

	@Override
	public void delete(Entity entity)
	{
		String languageCode = entity.getString(LanguageMetaData.CODE);
		if (languageCode.equalsIgnoreCase(LanguageService.FALLBACK_LANGUAGE))
		{
			throw new MolgenisDataException(
					"It is not possible to delete '" + languageCode + "'. This is the default language.");
		}

		decorated.delete(entity);

		// Delete label-{languageCode} attr from AttributeMetaDataMetaData
		AttributeMetaData attributeLabel = AttributeMetaDataMetaData.INSTANCE
				.getAttribute(AttributeMetaDataMetaData.LABEL + '-' + languageCode);
		if (attributeLabel != null)
		{
			dataService.getMeta().getDefaultBackend().deleteAttribute(AttributeMetaDataMetaData.ENTITY_NAME,
					attributeLabel.getName());
		}

		// Delete description-{languageCode} attr from AttributeMetaDataMetaData
		AttributeMetaData attributeDescription = AttributeMetaDataMetaData.INSTANCE
				.getAttribute(AttributeMetaDataMetaData.DESCRIPTION + '-' + languageCode);
		if (attributeDescription != null)
		{
			dataService.getMeta().getDefaultBackend().deleteAttribute(AttributeMetaDataMetaData.ENTITY_NAME,
					attributeDescription.getName());
		}

		// Delete description-{languageCode} attr from EntityMetaDataMetaData
		AttributeMetaData entityDescription = EntityMetaDataMetaData.INSTANCE
				.getAttribute(EntityMetaDataMetaData.DESCRIPTION + '-' + languageCode);
		if (entityDescription != null)
		{
			dataService.getMeta().getDefaultBackend().deleteAttribute(EntityMetaDataMetaData.ENTITY_NAME,
					entityDescription.getName());
		}

		// Delete label-{languageCode} attr from EntityMetaDataMetaData
		AttributeMetaData entityLabel = EntityMetaDataMetaData.INSTANCE
				.getAttribute(EntityMetaDataMetaData.LABEL + '-' + languageCode);
		if (entityLabel != null)
		{
			dataService.getMeta().getDefaultBackend().deleteAttribute(EntityMetaDataMetaData.ENTITY_NAME,
					entityLabel.getName());
		}

		// Delete language attribute from I18nStringMetaData
		dataService.getMeta().getDefaultBackend().deleteAttribute(I18nStringMetaData.ENTITY_NAME, languageCode);
		I18nStringMetaData.INSTANCE.removeLanguage(languageCode);
	}

	@Override
	public void delete(Stream<? extends Entity> entities)
	{
		entities.forEach(this::delete);
	}

	@Override
	public void deleteById(Object id)
	{
		Entity entity = findOne(id);
		if (entity != null) delete(entity);
	}

	@Override
	public void deleteById(Stream<Object> ids)
	{
		ids.forEach(this::deleteById);
	}

	@Override
	public void deleteAll()
	{
		delete(this.stream());
	}

	@Override
	public void add(Entity entity)
	{
		// Add language
		decorated.add(entity);

		// Create new label and description attributes for the added language
		String languageCode = entity.getString(LanguageMetaData.CODE);

		// Atribute label
		AttributeMetaData attrLabel = new DefaultAttributeMetaData(AttributeMetaDataMetaData.LABEL + '-' + languageCode)
				.setNillable(true);

		// Add the attribute to the attributes table
		dataService.getMeta().getDefaultBackend().addAttribute(AttributeMetaDataMetaData.ENTITY_NAME, attrLabel);

		// Update AttributeMetaDataMetaData
		AttributeMetaDataMetaData.INSTANCE.addAttributeMetaData(attrLabel);

		// Attribute description
		AttributeMetaData attrDescription = new DefaultAttributeMetaData(
				AttributeMetaDataMetaData.DESCRIPTION + '-' + languageCode).setNillable(true);
		dataService.getMeta().getDefaultBackend().addAttribute(AttributeMetaDataMetaData.ENTITY_NAME, attrDescription);
		AttributeMetaDataMetaData.INSTANCE.addAttributeMetaData(attrDescription);

		// EntityMeta description
		AttributeMetaData entityDescription = new DefaultAttributeMetaData(
				EntityMetaDataMetaData.DESCRIPTION + '-' + languageCode).setNillable(true);
		dataService.getMeta().getDefaultBackend().addAttribute(EntityMetaDataMetaData.ENTITY_NAME, entityDescription);
		EntityMetaDataMetaData.INSTANCE.addAttributeMetaData(entityDescription);

		// EntityMeta label
		AttributeMetaData entityLabel = new DefaultAttributeMetaData(EntityMetaDataMetaData.LABEL + '-' + languageCode)
				.setNillable(true);
		dataService.getMeta().getDefaultBackend().addAttribute(EntityMetaDataMetaData.ENTITY_NAME, entityLabel);
		EntityMetaDataMetaData.INSTANCE.addAttributeMetaData(entityLabel);

		// I18nString
		if (I18nStringMetaData.INSTANCE.addLanguage(languageCode))
		{
			dataService.getMeta().getDefaultBackend().addAttribute(I18nStringMetaData.ENTITY_NAME,
					I18nStringMetaData.INSTANCE.getAttribute(languageCode));
		}
	}

	@Override
	public Integer add(Stream<? extends Entity> entities)
	{
		AtomicInteger count = new AtomicInteger();
		entities.forEach(entity -> {
			add(entity);
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
	public void create()
	{
		decorated.create();
	}

	@Override
	public void drop()
	{
		decorated.drop();
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

}
