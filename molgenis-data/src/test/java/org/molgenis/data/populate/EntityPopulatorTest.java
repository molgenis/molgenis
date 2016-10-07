package org.molgenis.data.populate;

import org.mockito.Mockito;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.support.DynamicEntity;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.AttributeType.*;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

public class EntityPopulatorTest
{
	private static final String ATTR_ID = "id";
	private static final String ATTR_DATE_AUTO_DEFAULT = "date_auto-default";
	private static final String ATTR_DATE_AUTO_FALSE = "date_auto-false";
	private static final String ATTR_DATE_AUTO_TRUE = "date_auto-true";
	private static final String ATTR_DATETIME_AUTO_DEFAULT = "datetime_auto-default";
	private static final String ATTR_DATETIME_AUTO_FALSE = "datetime_auto-false";
	private static final String ATTR_DATETIME_AUTO_TRUE = "datetime_auto-true";

	private EntityMetaData entityMeta;
	private EntityPopulator entityPopulator;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		entityMeta = when(mock(EntityMetaData.class).getName()).thenReturn("entity").getMock();
		AttributeMetaData attrId = when(mock(AttributeMetaData.class).getName()).thenReturn(ATTR_ID).getMock();
		when(attrId.getDataType()).thenReturn(STRING);
		when(attrId.isAuto()).thenReturn(true);
		AttributeMetaData attrDateAutoDefault = when(mock(AttributeMetaData.class).getName())
				.thenReturn(ATTR_DATE_AUTO_DEFAULT).getMock();
		when(attrDateAutoDefault.getDataType()).thenReturn(DATE);
		AttributeMetaData attrDateAutoFalse = when(mock(AttributeMetaData.class).getName())
				.thenReturn(ATTR_DATE_AUTO_FALSE).getMock();
		when(attrDateAutoFalse.getDataType()).thenReturn(DATE);
		when(attrDateAutoFalse.isAuto()).thenReturn(false);
		AttributeMetaData attrDateAutoTrue = when(mock(AttributeMetaData.class).getName())
				.thenReturn(ATTR_DATE_AUTO_TRUE).getMock();
		when(attrDateAutoTrue.getDataType()).thenReturn(DATE);
		when(attrDateAutoTrue.isAuto()).thenReturn(true);
		AttributeMetaData attrDateTimeAutoDefault = when(mock(AttributeMetaData.class).getName())
				.thenReturn(ATTR_DATETIME_AUTO_DEFAULT).getMock();
		when(attrDateTimeAutoDefault.getDataType()).thenReturn(DATE_TIME);
		AttributeMetaData attrDateTimeAutoFalse = when(mock(AttributeMetaData.class).getName())
				.thenReturn(ATTR_DATETIME_AUTO_FALSE).getMock();
		when(attrDateTimeAutoFalse.getDataType()).thenReturn(DATE_TIME);
		when(attrDateTimeAutoFalse.isAuto()).thenReturn(false);
		AttributeMetaData attrDateTimeAutoTrue = when(mock(AttributeMetaData.class).getName())
				.thenReturn(ATTR_DATETIME_AUTO_TRUE).getMock();
		when(attrDateTimeAutoTrue.getDataType()).thenReturn(DATE_TIME);
		when(attrDateTimeAutoTrue.isAuto()).thenReturn(true);
		when(entityMeta.getIdAttribute()).thenReturn(attrId);
		when(entityMeta.getAttributes()).thenReturn(
				asList(attrId, attrDateAutoDefault, attrDateAutoFalse, attrDateAutoTrue, attrDateTimeAutoDefault,
						attrDateTimeAutoFalse, attrDateTimeAutoTrue));
		when(entityMeta.getAtomicAttributes()).thenReturn(
				asList(attrId, attrDateAutoDefault, attrDateAutoFalse, attrDateAutoTrue, attrDateTimeAutoDefault,
						attrDateTimeAutoFalse, attrDateTimeAutoTrue));
		when(entityMeta.getAttribute(ATTR_ID)).thenReturn(attrId);
		when(entityMeta.getAttribute(ATTR_DATE_AUTO_DEFAULT)).thenReturn(attrDateAutoDefault);
		when(entityMeta.getAttribute(ATTR_DATE_AUTO_FALSE)).thenReturn(attrDateAutoFalse);
		when(entityMeta.getAttribute(ATTR_DATE_AUTO_TRUE)).thenReturn(attrDateAutoTrue);
		when(entityMeta.getAttribute(ATTR_DATETIME_AUTO_DEFAULT)).thenReturn(attrDateTimeAutoDefault);
		when(entityMeta.getAttribute(ATTR_DATETIME_AUTO_FALSE)).thenReturn(attrDateTimeAutoFalse);
		when(entityMeta.getAttribute(ATTR_DATETIME_AUTO_TRUE)).thenReturn(attrDateTimeAutoTrue);
		IdGenerator idGenerator = mock(IdGenerator.class);
		Mockito.when(idGenerator.generateId()).thenReturn("ID1").thenReturn("ID2");
		entityPopulator = new EntityPopulator(idGenerator);
	}

	@Test
	public void populateAutoValues()
	{
		Entity entity = new DynamicEntity(entityMeta);
		entityPopulator.populate(entity);

		assertNotNull(entity.getIdValue());
		assertNull(entity.getDate(ATTR_DATE_AUTO_DEFAULT));
		assertNull(entity.getDate(ATTR_DATE_AUTO_FALSE));
		assertNotNull(entity.getDate(ATTR_DATE_AUTO_TRUE));
		assertNull(entity.getDate(ATTR_DATETIME_AUTO_DEFAULT));
		assertNull(entity.getDate(ATTR_DATETIME_AUTO_FALSE));
		assertNotNull(entity.getDate(ATTR_DATETIME_AUTO_TRUE));
	}
}