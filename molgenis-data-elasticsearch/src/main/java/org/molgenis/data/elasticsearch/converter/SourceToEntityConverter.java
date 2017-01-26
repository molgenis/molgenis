package org.molgenis.data.elasticsearch.converter;

import org.molgenis.data.Entity;
import org.molgenis.data.EntityManager;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.UnknownAttributeException;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.util.MolgenisDateFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.stream.StreamSupport;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.molgenis.data.EntityManager.CreationMode.NO_POPULATE;

@Component
public class SourceToEntityConverter
{
	private final EntityManager entityManager;

	@Autowired
	public SourceToEntityConverter(EntityManager entityManager)
	{
		this.entityManager = requireNonNull(entityManager);
	}

	public Entity convert(Map<String, Object> source, EntityType entityType)
	{
		Entity entity = entityManager.create(entityType, NO_POPULATE);
		source.entrySet().forEach(entry ->
		{
			String attrName = entry.getKey();
			Attribute attr = entityType.getAttribute(attrName);
			if (attr == null)
			{
				throw new UnknownAttributeException(
						"Unknown attribute [" + attrName + "] of entity [" + entityType.getFullyQualifiedName());
			}

			Object sourceValue = entry.getValue();
			Object entityValue;
			if (sourceValue != null)
			{
				AttributeType attrType = attr.getDataType();
				switch (attr.getDataType())
				{
					case BOOL:
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
						entityValue = sourceValue;
						break;
					case CATEGORICAL:
					case FILE:
					case XREF:
						// TODO store id for xrefs
						if (sourceValue instanceof Map<?, ?>)
						{
							@SuppressWarnings("unchecked")
							Map<String, Object> sourceRefEntity = (Map<String, Object>) sourceValue;
							EntityType refEntity = attr.getRefEntity();
							String refIdAttrName = refEntity.getIdAttribute().getName();
							Object sourceRefEntityId = sourceRefEntity.get(refIdAttrName);
							entityValue = entityManager.getReference(refEntity, sourceRefEntityId);
						}
						else
						{
							throw new RuntimeException("Unexpected type [" + sourceValue.getClass().getSimpleName()
									+ "], expected [Map<String, Object]");
						}
						break;
					case CATEGORICAL_MREF:
					case MREF:
						if (sourceValue instanceof Iterable<?>)
						{
							// TODO store list of ids for mrefs
							@SuppressWarnings("unchecked")
							Iterable<Map<String, Object>> sourceRefEntities = (Iterable<Map<String, Object>>) sourceValue;
							EntityType refEntity = attr.getRefEntity();
							String refIdAttrName = refEntity.getIdAttribute().getName();
							Iterable<Object> sourceRefEntityIds = () -> StreamSupport
									.stream(sourceRefEntities.spliterator(), false)
									.map(sourceRefEntity -> sourceRefEntity.get(refIdAttrName)).iterator();
							entityValue = entityManager.getReferences(refEntity, sourceRefEntityIds);
						}
						else
						{
							throw new RuntimeException("Unexpected type [" + sourceValue.getClass().getSimpleName()
									+ "], expected [Iterable<Map<String, Object>>]");
						}
						break;
					case COMPOUND:
						throw new RuntimeException("Compound attribute is not an atomic attribute");
					case DATE:
						try
						{
							entityValue = MolgenisDateFormat.getDateFormat().parse((String) sourceValue);
						}
						catch (Exception e)
						{
							throw new MolgenisDataException(e);
						}
						break;
					case DATE_TIME:
						try
						{
							entityValue = MolgenisDateFormat.getDateTimeFormat().parse((String) sourceValue);
						}
						catch (Exception e)
						{
							throw new MolgenisDataException(e);
						}
						break;
					default:
						throw new RuntimeException(format("Unknown data type [%s]", attrType.toString()));
				}
			}
			else
			{
				entityValue = null;
			}
			entity.set(attrName, entityValue);
		});
		return entity;
	}
}
