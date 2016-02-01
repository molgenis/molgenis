package org.molgenis.data.meta;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.StreamSupport.stream;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.NAME;
import static org.molgenis.data.meta.EntityMetaDataMetaData.ABSTRACT;
import static org.molgenis.data.meta.EntityMetaDataMetaData.ATTRIBUTES;
import static org.molgenis.data.meta.EntityMetaDataMetaData.BACKEND;
import static org.molgenis.data.meta.EntityMetaDataMetaData.DESCRIPTION;
import static org.molgenis.data.meta.EntityMetaDataMetaData.EXTENDS;
import static org.molgenis.data.meta.EntityMetaDataMetaData.FULL_NAME;
import static org.molgenis.data.meta.EntityMetaDataMetaData.ID_ATTRIBUTE;
import static org.molgenis.data.meta.EntityMetaDataMetaData.LABEL;
import static org.molgenis.data.meta.EntityMetaDataMetaData.LABEL_ATTRIBUTE;
import static org.molgenis.data.meta.EntityMetaDataMetaData.LOOKUP_ATTRIBUTES;
import static org.molgenis.data.meta.EntityMetaDataMetaData.PACKAGE;
import static org.molgenis.data.meta.EntityMetaDataMetaData.SIMPLE_NAME;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.ManageableRepositoryCollection;
import org.molgenis.data.Repository;
import org.molgenis.data.i18n.LanguageService;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.util.DependencyResolver;

import com.google.common.collect.Lists;

/**
 * Helper class around the {@link EntityMetaDataMetaData} repository. Caches the metadata in
 * {@link DefaultEntityMetaData}. Internal implementation class, use {@link MetaDataServiceImpl} instead.
 * 
 */
class EntityMetaDataRepository
{
	public static final EntityMetaDataMetaData META_DATA = EntityMetaDataMetaData.INSTANCE;
	private final Repository repository;
	private final PackageRepository packageRepository;
	private final ManageableRepositoryCollection collection;
	private final Map<String, DefaultEntityMetaData> entityMetaDataCache = new HashMap<>();
	private final AttributeMetaDataRepository attributeRepository;
	private final LanguageService languageService;

	public EntityMetaDataRepository(ManageableRepositoryCollection collection, PackageRepository packageRepository,
			AttributeMetaDataRepository attributeRepository, LanguageService languageService)
	{
		this.packageRepository = packageRepository;
		this.attributeRepository = attributeRepository;
		this.repository = collection.addEntityMeta(META_DATA);
		this.collection = collection;
		this.languageService = languageService;
	}

	Repository getRepository()
	{
		return repository;
	}

	/**
	 * Fills the {@link #entityMetaDataCache} with {@link EntityMetaData}, based on the entities in {@link #repository}
	 * and the {@link PackageImpl}s in {@link #packageRepository}. Adds the entities to the {@link #packageRepository}'s
	 * {@link PackageImpl}s.
	 */
	void fillEntityMetaDataCache()
	{
		List<Entity> entities = new ArrayList<>();
		// Fill the cache with EntityMetaData objects
		for (Entity entity : repository)
		{
			entities.add(entity);
			String name = entity.getString(SIMPLE_NAME);
			DefaultEntityMetaData entityMetaData = new DefaultEntityMetaData(name);
			entityMetaData.setAbstract(entity.getBoolean(ABSTRACT));
			entityMetaData.setLabel(entity.getString(LABEL));
			entityMetaData.setDescription(entity.getString(DESCRIPTION));
			entityMetaData.setBackend(entity.getString(BACKEND));

			// Language attributes
			for (String languageCode : languageService.getLanguageCodes())
			{
				String attributeName = DESCRIPTION + '-' + languageCode;
				String description = entity.getString(attributeName);
				if (description != null) entityMetaData.setDescription(languageCode, description);

				attributeName = LABEL + '-' + languageCode;
				String label = entity.getString(attributeName);
				if (label != null) entityMetaData.setLabel(languageCode, label);
			}

			entityMetaDataCache.put(entity.getString(FULL_NAME), entityMetaData);
		}
		// Only then create the AttributeMetaData objects, so that lookups of refEntity values work.
		for (Entity entity : entities)
		{
			DefaultEntityMetaData entityMetaData = entityMetaDataCache.get(entity.getString(FULL_NAME));
			Iterable<Entity> attributeEntities = entity.getEntities(EntityMetaDataMetaData.ATTRIBUTES);
			stream(attributeEntities.spliterator(), false).map(attributeRepository::toAttributeMetaData)
					.forEach(entityMetaData::addAttributeMetaData);
		}
		for (Entity entity : entities)
		{
			final Entity extendsEntity = entity.getEntity(EXTENDS);
			final DefaultEntityMetaData entityMetaData = entityMetaDataCache.get(entity.getString(FULL_NAME));
			if (extendsEntity != null)
			{
				final DefaultEntityMetaData extendsEntityMetaData = entityMetaDataCache
						.get(extendsEntity.getString(FULL_NAME));
				entityMetaData.setExtends(extendsEntityMetaData);
			}
			final Entity packageEntity = entity.getEntity(PACKAGE);

			PackageImpl p = (PackageImpl) packageRepository
					.getPackage(packageEntity.getString(PackageMetaData.FULL_NAME));
			if (null != p)
			{
				entityMetaData.setPackage(p);
				p.addEntity(entityMetaData);
			}

			// set id, label and lookup attrs
			Entity idAttr = entity.getEntity(ID_ATTRIBUTE);
			if (idAttr != null)
			{
				entityMetaData
						.setIdAttribute(entityMetaData.getAttribute(idAttr.getString(AttributeMetaDataMetaData.NAME)));
			}
			Entity labelAttr = entity.getEntity(LABEL_ATTRIBUTE);
			if (labelAttr != null)
			{
				entityMetaData.setLabelAttribute(
						entityMetaData.getAttribute(labelAttr.getString(AttributeMetaDataMetaData.NAME)));
			}
			Stream<Entity> lookupAttrs = stream(entity.getEntities(LOOKUP_ATTRIBUTES).spliterator(), false);
			entityMetaData.setLookupAttributes(lookupAttrs.map(lookupAttrEntity -> {
				return entityMetaData.getAttribute(lookupAttrEntity.getString(AttributeMetaDataMetaData.NAME));
			}));
		}

	}

	/**
	 * Retrieves an EntityMetaData.
	 * 
	 * @param fullyQualifiedName
	 *            the fully qualified name of the entityMetaData
	 * @return the EntityMetaData or null if none found
	 */
	public DefaultEntityMetaData get(String fullyQualifiedName)
	{
		return entityMetaDataCache.get(fullyQualifiedName);
	}

	/**
	 * Adds an {@link EntityMetaData} to the {@link #repository}. Will also add its attributes to the
	 * {@link #attributeRepository}.
	 * 
	 * @param entityMetaData
	 *            the {@link EntityMetaData} to add.
	 */
	public void add(EntityMetaData entityMetaData)
	{
		DefaultEntityMetaData emd = new DefaultEntityMetaData(entityMetaData.getSimpleName());
		emd.setLabel(entityMetaData.getLabel());
		emd.setAbstract(entityMetaData.isAbstract());
		emd.setDescription(entityMetaData.getDescription());
		emd.setBackend(entityMetaData.getBackend() == null ? collection.getName() : entityMetaData.getBackend());

		// Language attributes
		for (String languageCode : entityMetaData.getDescriptionLanguageCodes())
		{
			String description = entityMetaData.getDescription(languageCode);
			if (description != null) emd.setDescription(languageCode, description);
		}

		for (String languageCode : entityMetaData.getLabelLanguageCodes())
		{
			String label = entityMetaData.getLabel(languageCode);
			if (label != null) emd.setLabel(languageCode, label);
		}

		if (entityMetaData.getExtends() != null)
		{
			emd.setExtends(entityMetaDataCache.get(entityMetaData.getExtends().getName()));
		}
		if (entityMetaData.getPackage() == null)
		{
			emd.setPackage(PackageImpl.defaultPackage);
		}
		else
		{
			emd.setPackage(entityMetaData.getPackage());
		}
		entityMetaDataCache.put(emd.getName(), emd);
		if (packageRepository.getPackage(emd.getPackage().getName()) == null)
		{
			packageRepository.add(emd.getPackage());
		}
		((PackageImpl) packageRepository.getPackage(emd.getPackage().getName())).addEntity(emd);
		Entity entity = toEntity(emd);
		Iterable<AttributeMetaData> attributes = entityMetaData.getOwnAttributes();
		if (attributes != null)
		{
			Map<String, Entity> attrs = stream(attributeRepository.add(attributes).spliterator(), false)
					.collect(toMap(attrEntity -> attrEntity.getString(AttributeMetaDataMetaData.NAME),
							Function.<Entity> identity(), (u, v) -> {
								throw new IllegalStateException(String.format("Duplicate key %s", u));
							} , LinkedHashMap::new));
			entity.set(ATTRIBUTES, attrs.values());
			emd.addAllAttributeMetaData(attributes);

			AttributeMetaData idAttribute = entityMetaData.getOwnIdAttribute();
			if (idAttribute != null)
			{
				emd.setIdAttribute(idAttribute);
				entity.set(ID_ATTRIBUTE, attrs.get(idAttribute.getName()));
			}
			AttributeMetaData labelAttribute = entityMetaData.getOwnLabelAttribute();
			if (labelAttribute != null)
			{
				emd.setLabelAttribute(labelAttribute);
				entity.set(LABEL_ATTRIBUTE, attrs.get(labelAttribute.getName()));
			}

			List<Entity> lookupAttrEntities = stream(entityMetaData.getOwnLookupAttributes().spliterator(), false)
					.map(lookupAttr -> attrs.get(lookupAttr.getName())).collect(toList());
			entity.set(LOOKUP_ATTRIBUTES, lookupAttrEntities);
		}
		else
		{
			entity.set(ATTRIBUTES, Collections.emptyList());
		}

		repository.add(entity);
	}

	public void update(DefaultEntityMetaData entityMeta)
	{
		repository.update(toEntity(entityMeta));
		entityMetaDataCache.put(entityMeta.getName(), entityMeta);
	}

	private Entity toEntity(EntityMetaData emd)
	{
		Entity entityMetaDataEntity = new MapEntity(META_DATA);
		entityMetaDataEntity.set(FULL_NAME, emd.getName());
		entityMetaDataEntity.set(SIMPLE_NAME, emd.getSimpleName());
		if (emd.getPackage() != null)
		{
			entityMetaDataEntity.set(PACKAGE, packageRepository.getEntity(emd.getPackage().getName()));
		}
		entityMetaDataEntity.set(DESCRIPTION, emd.getDescription());
		entityMetaDataEntity.set(ABSTRACT, emd.isAbstract());
		entityMetaDataEntity.set(LABEL, emd.getLabel());
		entityMetaDataEntity.set(BACKEND, emd.getBackend());
		if (emd.getExtends() != null)
		{
			entityMetaDataEntity.set(EXTENDS, getEntity(emd.getExtends().getName()));
		}

		// Language attributes
		for (String languageCode : emd.getDescriptionLanguageCodes())
		{
			String attributeName = DESCRIPTION + '-' + languageCode;
			String description = emd.getDescription(languageCode);
			if (description != null) entityMetaDataEntity.set(attributeName, description);
		}

		for (String languageCode : emd.getLabelLanguageCodes())
		{
			String attributeName = LABEL + '-' + languageCode;
			String label = emd.getLabel(languageCode);
			if (label != null) entityMetaDataEntity.set(attributeName, label);
		}
		return entityMetaDataEntity;
	}

	public void delete(String entityName)
	{
		Entity entity = getRepository().findOne(entityName);
		if (entity != null)
		{
			repository.deleteById(entityName);
			attributeRepository.deleteAttributes(entity.getEntities(ATTRIBUTES));
		}

		entityMetaDataCache.remove(entityName);
	}

	/**
	 * Deletes all entities, in the right order
	 */
	public void deleteAll()
	{
		List<Entity> entities = Lists
				.newLinkedList(new DependencyResolver().resolveSelfReferences(repository, META_DATA));
		Collections.reverse(entities);
		for (Entity entity : entities)
		{
			delete(entity.getString(EntityMetaDataMetaData.FULL_NAME));
		}
	}

	public Entity getEntity(String fullyQualifiedName)
	{
		if (!entityMetaDataCache.containsKey(fullyQualifiedName))
		{
			return null;
		}
		return toEntity(entityMetaDataCache.get(fullyQualifiedName));
	}

	public Collection<EntityMetaData> getMetaDatas()
	{
		return Collections.<EntityMetaData> unmodifiableCollection(entityMetaDataCache.values());
	}

	public void removeAttribute(String entityName, String attributeName)
	{
		Entity entity = getEntity(entityName);
		List<Entity> attributes = Lists.newArrayList(entity.getEntities(ATTRIBUTES));
		Entity attributeEntity = attributes.stream().filter(att -> attributeName.equals(att.getString(NAME)))
				.findFirst().get();
		attributes.remove(attributeEntity);
		repository.update(entity);
		attributeRepository.deleteAttributes(Collections.singletonList(attributeEntity));
	}

	public EntityMetaData addAttribute(String fullyQualifiedEntityName, AttributeMetaData attr)
	{
		DefaultEntityMetaData entityMetaData = get(fullyQualifiedEntityName);
		delete(fullyQualifiedEntityName);
		entityMetaData.addAttributeMetaData(attr);
		add(entityMetaData);
		return entityMetaData;
	}
}
