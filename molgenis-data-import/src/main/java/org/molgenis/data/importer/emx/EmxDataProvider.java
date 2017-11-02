package org.molgenis.data.importer.emx;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.data.*;
import org.molgenis.data.importer.DataProvider;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.util.UnexpectedEnumException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.StreamSupport.stream;
import static org.molgenis.data.EntityManager.CreationMode.POPULATE;
import static org.molgenis.data.meta.DefaultPackage.PACKAGE_DEFAULT;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;

class EmxDataProvider implements DataProvider
{
	private final EmxImportJob job;
	private final EntityManager entityManager;

	EmxDataProvider(EmxImportJob job, EntityManager entityManager)
	{
		this.job = requireNonNull(job);
		this.entityManager = requireNonNull(entityManager);
	}

	@Override
	public Stream<EntityType> getEntityTypes()
	{
		return job.getParsedMetaData().getEntities().stream();
	}

	@Override
	public boolean hasEntities(EntityType entityType)
	{
		if (job.getSource().hasRepository(entityType))
		{
			return true;
		}
		else if (isDefaultPackageEntityType(entityType))
		{
			String alternativeEntityTypeId = getDefaultPackageEntityTypeAlternativeId(entityType);
			return job.getSource().hasRepository(alternativeEntityTypeId);
		}
		else
		{
			return false;
		}
	}

	@Override
	public Stream<Entity> getEntities(EntityType entityType)
	{
		Repository<Entity> repository = job.getSource().getRepository(entityType);
		if (repository == null && isDefaultPackageEntityType(entityType))
		{
			String alternativeEntityTypeId = getDefaultPackageEntityTypeAlternativeId(entityType);
			repository = job.getSource().getRepository(alternativeEntityTypeId);
		}
		if (repository == null)
		{
			throw new UnknownRepositoryException(entityType.getId());
		}
		return stream(repository.spliterator(), false).map(sourceEntity -> toEntity(entityType, sourceEntity));
	}

	private boolean isDefaultPackageEntityType(EntityType entityType)
	{
		return entityType.getId().startsWith(PACKAGE_DEFAULT + PACKAGE_SEPARATOR);
	}

	private String getDefaultPackageEntityTypeAlternativeId(EntityType entityType)
	{
		return entityType.getId().substring(PACKAGE_DEFAULT.length() + PACKAGE_SEPARATOR.length());
	}

	/**
	 * Create an entity from the EMX entity
	 *
	 * @param entityType entity meta data
	 * @param emxEntity  EMX entity
	 * @return MOLGENIS entity
	 */
	private Entity toEntity(EntityType entityType, Entity emxEntity)
	{
		Entity entity = entityManager.create(entityType, POPULATE);
		for (Attribute attr : entityType.getAtomicAttributes())
		{
			if (attr.getExpression() == null && !attr.isMappedBy())
			{
				String attrName = attr.getName();
				Object emxValue = emxEntity.get(attrName);

				AttributeType attrType = attr.getDataType();
				switch (attrType)
				{
					case BOOL:
					case DATE:
					case DATE_TIME:
					case DECIMAL:
					case EMAIL:
					case ENUM:
					case HTML:
					case HYPERLINK:
					case INT:
					case LONG:
					case SCRIPT:
					case STRING:
					case TEXT:
						Object value = emxValue != null ? DataConverter.convert(emxValue, attr) : null;
						if ((!attr.isAuto() || value != null) && (!attr.hasDefaultValue() || value != null))
						{
							entity.set(attrName, value);
						}
						break;
					case CATEGORICAL:
					case FILE:
					case XREF:
						// DataConverter.convert performs no conversion for reference types
						Entity refEntity = toRefEntity(attr, emxValue);

						// do not set generated auto refEntities to null
						if ((!attr.isAuto() || refEntity != null) && (!attr.hasDefaultValue() || refEntity != null))
						{
							entity.set(attrName, refEntity);
						}
						break;
					case CATEGORICAL_MREF:
					case MREF:
						// DataConverter.convert performs no conversion for reference types
						List<Entity> refEntities = toRefEntities(attr, emxValue);

						// do not set generated auto refEntities to null
						if (!refEntities.isEmpty())
						{
							entity.set(attrName, refEntities);
						}
						break;
					case COMPOUND:
						throw new RuntimeException(format("Illegal attribute type [%s]", attrType.toString()));
					default:
						throw new UnexpectedEnumException(attrType);
				}
			}
		}
		return entity;
	}

	private List<Entity> toRefEntities(Attribute attr, Object emxValue)
	{
		List<Entity> refEntities;
		if (emxValue != null)
		{
			if (emxValue instanceof Iterable<?>)
			{
				List<Entity> mrefEntities = new ArrayList<>();
				for (Object emxValueItem : (Iterable<?>) emxValue)
				{
					Entity entityValue;
					if (emxValueItem instanceof Entity)
					{
						entityValue = toEntity(attr.getRefEntity(), (Entity) emxValueItem);
					}
					else
					{
						EntityType xrefEntity = attr.getRefEntity();
						Object entityId = DataConverter.convert(emxValueItem, xrefEntity.getIdAttribute());
						entityValue = entityManager.getReference(xrefEntity, entityId);
					}
					mrefEntities.add(entityValue);
				}
				refEntities = mrefEntities;
			}
			else
			{
				EntityType mrefEntity = attr.getRefEntity();
				Attribute refIdAttr = mrefEntity.getIdAttribute();

				String[] tokens = StringUtils.split(emxValue.toString(), ',');
				List<Entity> mrefEntities = new ArrayList<>();
				for (String token : tokens)
				{
					Object entityId = DataConverter.convert(token.trim(), refIdAttr);
					mrefEntities.add(entityManager.getReference(mrefEntity, entityId));
				}
				refEntities = mrefEntities;
			}
		}
		else
		{
			refEntities = emptyList();
		}
		return refEntities;
	}

	private Entity toRefEntity(Attribute attr, Object emxValue)
	{
		Entity refEntity;
		if (emxValue != null)
		{
			if (emxValue instanceof Entity)
			{
				refEntity = toEntity(attr.getRefEntity(), (Entity) emxValue);
			}
			else
			{
				EntityType xrefEntity = attr.getRefEntity();
				Object entityId = DataConverter.convert(emxValue, xrefEntity.getIdAttribute());
				refEntity = entityManager.getReference(xrefEntity, entityId);
			}
		}
		else
		{
			refEntity = null;
		}
		return refEntity;
	}
}
