package org.molgenis.util;

import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.EntityMetaData;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.lang.reflect.Type;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.MolgenisFieldTypes.AttributeType.MANY_TO_ONE;
import static org.molgenis.MolgenisFieldTypes.AttributeType.ONE_TO_MANY;
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
		EntityMetaData entityMeta = mock(EntityMetaData.class);
		when(entityMeta.getName()).thenReturn("entity");

		AttributeMetaData oneToManyAttr = mock(AttributeMetaData.class);
		String oneToManyAttrName = "oneToManyAttr";
		when(oneToManyAttr.getName()).thenReturn(oneToManyAttrName);
		when(oneToManyAttr.getDataType()).thenReturn(ONE_TO_MANY);
		AttributeMetaData manyToOneAttr = mock(AttributeMetaData.class);
		String manyToOneAttrName = "manyToOneAttr";
		when(manyToOneAttr.getName()).thenReturn(manyToOneAttrName);
		when(manyToOneAttr.getDataType()).thenReturn(MANY_TO_ONE);
		when(entityMeta.getAtomicAttributes()).thenReturn(newArrayList(oneToManyAttr, manyToOneAttr));

		EntityMetaData refEntityMeta = mock(EntityMetaData.class);
		when(refEntityMeta.getName()).thenReturn("refEntity");

		String oneToManyEntity0IdValue = "oneToManyEntity0";
		String oneToManyEntity0LabelValue = "oneToManyEntityLabel0";
		Entity oneToManyEntity0 = mock(Entity.class);
		when(oneToManyEntity0.getEntityMetaData()).thenReturn(refEntityMeta);
		when(oneToManyEntity0.getIdValue()).thenReturn(oneToManyEntity0IdValue);
		when(oneToManyEntity0.getLabelValue()).thenReturn(oneToManyEntity0LabelValue);

		String oneToManyEntity1IdValue = "oneToManyEntity1";
		String oneToManyEntity1LabelValue = "oneToManyEntityLabel1";
		Entity oneToManyEntity1 = mock(Entity.class);
		when(oneToManyEntity1.getIdValue()).thenReturn(oneToManyEntity1IdValue);
		when(oneToManyEntity1.getLabelValue()).thenReturn(oneToManyEntity1LabelValue);
		when(oneToManyEntity1.getEntityMetaData()).thenReturn(refEntityMeta);

		List<Entity> oneToManyEntities = newArrayList(oneToManyEntity0, oneToManyEntity1);

		String manyToOneEntityIdValue = "manyToOneEntity0";
		String manyToOneEntityLabelValue = "manyToOneEntityLabel0";
		Entity manyToOneEntity = mock(Entity.class);
		when(manyToOneEntity.getEntityMetaData()).thenReturn(refEntityMeta);
		when(manyToOneEntity.getIdValue()).thenReturn(manyToOneEntityIdValue);
		when(manyToOneEntity.getLabelValue()).thenReturn(manyToOneEntityLabelValue);

		Entity entity = mock(Entity.class);
		when(entity.getEntityMetaData()).thenReturn(entityMeta);
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

		String expectedJson = "{\"__entityName\":\"entity\",\"oneToManyAttr\":[{\"__entityName\":\"refEntity\",\"__idValue\":\"oneToManyEntity0\",\"__labelValue\":\"oneToManyEntityLabel0\"},{\"__entityName\":\"refEntity\",\"__idValue\":\"oneToManyEntity1\",\"__labelValue\":\"oneToManyEntityLabel1\"}],\"manyToOneAttr\":{\"__entityName\":\"refEntity\",\"__idValue\":\"manyToOneEntity0\",\"__labelValue\":\"manyToOneEntityLabel0\"}}";
		assertEquals(entitySerializer.serialize(entity, type, context).toString(), expectedJson);
	}
}