package org.molgenis.web.support;

import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.lang.reflect.Type;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.meta.AttributeType.ONE_TO_MANY;
import static org.molgenis.data.meta.AttributeType.XREF;
import static org.testng.Assert.assertEquals;

public class EntitySerializerTest
{
	private EntitySerializer entitySerializer;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		entitySerializer = new EntitySerializer();
	}

	@Test
	public void testSerialize() throws Exception
	{
		EntityType entityType = mock(EntityType.class);
		when(entityType.getId()).thenReturn("entity");

		Attribute oneToManyAttr = mock(Attribute.class);
		String oneToManyAttrName = "oneToManyAttr";
		when(oneToManyAttr.getName()).thenReturn(oneToManyAttrName);
		when(oneToManyAttr.getDataType()).thenReturn(ONE_TO_MANY);
		Attribute manyToOneAttr = mock(Attribute.class);
		String manyToOneAttrName = "xrefAttr";
		when(manyToOneAttr.getName()).thenReturn(manyToOneAttrName);
		when(manyToOneAttr.getDataType()).thenReturn(XREF);
		when(entityType.getAtomicAttributes()).thenReturn(newArrayList(oneToManyAttr, manyToOneAttr));

		EntityType refEntityType = mock(EntityType.class);
		when(refEntityType.getId()).thenReturn("refEntity");

		String oneToManyEntity0IdValue = "oneToManyEntity0";
		String oneToManyEntity0LabelValue = "oneToManyEntityLabel0";
		Entity oneToManyEntity0 = mock(Entity.class);
		when(oneToManyEntity0.getEntityType()).thenReturn(refEntityType);
		when(oneToManyEntity0.getIdValue()).thenReturn(oneToManyEntity0IdValue);
		when(oneToManyEntity0.getLabelValue()).thenReturn(oneToManyEntity0LabelValue);

		String oneToManyEntity1IdValue = "oneToManyEntity1";
		String oneToManyEntity1LabelValue = "oneToManyEntityLabel1";
		Entity oneToManyEntity1 = mock(Entity.class);
		when(oneToManyEntity1.getIdValue()).thenReturn(oneToManyEntity1IdValue);
		when(oneToManyEntity1.getLabelValue()).thenReturn(oneToManyEntity1LabelValue);
		when(oneToManyEntity1.getEntityType()).thenReturn(refEntityType);

		List<Entity> oneToManyEntities = newArrayList(oneToManyEntity0, oneToManyEntity1);

		String manyToOneEntityIdValue = "xrefEntity0";
		String manyToOneEntityLabelValue = "xrefEntityLabel0";
		Entity manyToOneEntity = mock(Entity.class);
		when(manyToOneEntity.getEntityType()).thenReturn(refEntityType);
		when(manyToOneEntity.getIdValue()).thenReturn(manyToOneEntityIdValue);
		when(manyToOneEntity.getLabelValue()).thenReturn(manyToOneEntityLabelValue);

		Entity entity = mock(Entity.class);
		when(entity.getEntityType()).thenReturn(entityType);
		when(entity.get(oneToManyAttrName)).thenReturn(oneToManyEntities);
		when(entity.get(manyToOneAttrName)).thenReturn(manyToOneEntity);
		when(entity.getEntities(oneToManyAttrName)).thenReturn(oneToManyEntities);
		when(entity.getEntity(manyToOneAttrName)).thenReturn(manyToOneEntity);

		Type type = mock(Type.class);
		JsonSerializationContext context = mock(JsonSerializationContext.class);
		when(context.serialize(oneToManyEntity0IdValue)).thenReturn(new JsonPrimitive(oneToManyEntity0IdValue));
		when(context.serialize(oneToManyEntity0LabelValue)).thenReturn(new JsonPrimitive(oneToManyEntity0LabelValue));
		when(context.serialize(oneToManyEntity1IdValue)).thenReturn(new JsonPrimitive(oneToManyEntity1IdValue));
		when(context.serialize(oneToManyEntity1LabelValue)).thenReturn(new JsonPrimitive(oneToManyEntity1LabelValue));
		when(context.serialize(manyToOneEntityIdValue)).thenReturn(new JsonPrimitive(manyToOneEntityIdValue));
		when(context.serialize(manyToOneEntityLabelValue)).thenReturn(new JsonPrimitive(manyToOneEntityLabelValue));

		String expectedJson = "{\"__entityTypeId\":\"entity\",\"oneToManyAttr\":[{\"__entityTypeId\":\"refEntity\",\"__idValue\":\"oneToManyEntity0\",\"__labelValue\":\"oneToManyEntityLabel0\"},{\"__entityTypeId\":\"refEntity\",\"__idValue\":\"oneToManyEntity1\",\"__labelValue\":\"oneToManyEntityLabel1\"}],\"xrefAttr\":{\"__entityTypeId\":\"refEntity\",\"__idValue\":\"xrefEntity0\",\"__labelValue\":\"xrefEntityLabel0\"}}";
		assertEquals(entitySerializer.serialize(entity, type, context).toString(), expectedJson);
	}
}