package org.molgenis.data.elasticsearch.index;

import static java.util.Objects.requireNonNull;

import java.util.Iterator;
import java.util.Map;
import java.util.stream.StreamSupport;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityManager;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.UnknownAttributeException;
import org.molgenis.data.elasticsearch.ElasticsearchService;
import org.molgenis.data.support.DefaultEntity;
import org.molgenis.util.MolgenisDateFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SourceToEntityConverter
{
	private final DataService dataService;
	private final EntityManager entityManager;

	@Autowired
	public SourceToEntityConverter(DataService dataService, EntityManager entityManager)
	{
		this.dataService = requireNonNull(dataService);
		this.entityManager = requireNonNull(entityManager);
	}

	public Entity convert(Map<String, Object> source, EntityMetaData entityMeta)
	{
		DefaultEntity entity = new DefaultEntity(entityMeta, dataService);
		source.entrySet().forEach(entry -> {
			String attrName = entry.getKey();
			if (attrName.equals(ElasticsearchService.CRUD_TYPE_FIELD_NAME))
			{
				// this entity was retrieved from a transaction index, ignore 'meta' crud type attribute
				return;
			}
			AttributeMetaData attr = entityMeta.getAttribute(attrName);
			if (attr == null)
			{
				throw new UnknownAttributeException(
						"Unknown attribute [" + attrName + "] of entity [" + entityMeta.getName());
			}

			Object sourceValue = entry.getValue();
			Object entityValue;
			if (sourceValue != null)
			{
				FieldTypeEnum attrType = attr.getDataType().getEnumType();
				switch (attr.getDataType().getEnumType())
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
							EntityMetaData refEntity = attr.getRefEntity();
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
							EntityMetaData refEntity = attr.getRefEntity();
							String refIdAttrName = refEntity.getIdAttribute().getName();
							Iterable<Object> sourceRefEntityIds = new Iterable<Object>()
							{
								@Override
								public Iterator<Object> iterator()
								{
									return StreamSupport.stream(sourceRefEntities.spliterator(), false)
											.map(sourceRefEntity -> sourceRefEntity.get(refIdAttrName)).iterator();
								}
							};
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
					case IMAGE:
						throw new MolgenisDataException("Unsupported data type for indexing [" + attrType + "]");
					default:
						throw new RuntimeException("Unknown data type [" + attrType + "]");
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
