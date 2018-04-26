package org.molgenis.data.populate;

import org.mockito.Mockito;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.DynamicEntity;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.meta.AttributeType.*;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

public class AutoValuePopulatorTest
{
	private static final String ATTR_ID = "id";
	private static final String ATTR_DATE_AUTO_DEFAULT = "date_auto-default";
	private static final String ATTR_DATE_AUTO_FALSE = "date_auto-false";
	private static final String ATTR_DATE_AUTO_TRUE = "date_auto-true";
	private static final String ATTR_DATETIME_AUTO_DEFAULT = "datetime_auto-default";
	private static final String ATTR_DATETIME_AUTO_FALSE = "datetime_auto-false";
	private static final String ATTR_DATETIME_AUTO_TRUE = "datetime_auto-true";

	private EntityType entityType;
	private AutoValuePopulator autoValuePopulator;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		entityType = when(mock(EntityType.class).getId()).thenReturn("entity").getMock();
		Attribute attrId = when(mock(Attribute.class).getName()).thenReturn(ATTR_ID).getMock();
		when(attrId.getDataType()).thenReturn(STRING);
		when(attrId.isAuto()).thenReturn(true);
		Attribute attrDateAutoDefault = when(mock(Attribute.class).getName()).thenReturn(ATTR_DATE_AUTO_DEFAULT)
																			 .getMock();
		when(attrDateAutoDefault.getDataType()).thenReturn(DATE);
		Attribute attrDateAutoFalse = when(mock(Attribute.class).getName()).thenReturn(ATTR_DATE_AUTO_FALSE).getMock();
		when(attrDateAutoFalse.getDataType()).thenReturn(DATE);
		when(attrDateAutoFalse.isAuto()).thenReturn(false);
		Attribute attrDateAutoTrue = when(mock(Attribute.class).getName()).thenReturn(ATTR_DATE_AUTO_TRUE).getMock();
		when(attrDateAutoTrue.getDataType()).thenReturn(DATE);
		when(attrDateAutoTrue.isAuto()).thenReturn(true);
		Attribute attrDateTimeAutoDefault = when(mock(Attribute.class).getName()).thenReturn(ATTR_DATETIME_AUTO_DEFAULT)
																				 .getMock();
		when(attrDateTimeAutoDefault.getDataType()).thenReturn(DATE_TIME);
		Attribute attrDateTimeAutoFalse = when(mock(Attribute.class).getName()).thenReturn(ATTR_DATETIME_AUTO_FALSE)
																			   .getMock();
		when(attrDateTimeAutoFalse.getDataType()).thenReturn(DATE_TIME);
		when(attrDateTimeAutoFalse.isAuto()).thenReturn(false);
		Attribute attrDateTimeAutoTrue = when(mock(Attribute.class).getName()).thenReturn(ATTR_DATETIME_AUTO_TRUE)
																			  .getMock();
		when(attrDateTimeAutoTrue.getDataType()).thenReturn(DATE_TIME);
		when(attrDateTimeAutoTrue.isAuto()).thenReturn(true);
		when(entityType.getIdAttribute()).thenReturn(attrId);
		when(entityType.getAttributes()).thenReturn(
				asList(attrId, attrDateAutoDefault, attrDateAutoFalse, attrDateAutoTrue, attrDateTimeAutoDefault,
						attrDateTimeAutoFalse, attrDateTimeAutoTrue));
		when(entityType.getAtomicAttributes()).thenReturn(
				asList(attrId, attrDateAutoDefault, attrDateAutoFalse, attrDateAutoTrue, attrDateTimeAutoDefault,
						attrDateTimeAutoFalse, attrDateTimeAutoTrue));
		when(entityType.getAttribute(ATTR_ID)).thenReturn(attrId);
		when(entityType.getAttribute(ATTR_DATE_AUTO_DEFAULT)).thenReturn(attrDateAutoDefault);
		when(entityType.getAttribute(ATTR_DATE_AUTO_FALSE)).thenReturn(attrDateAutoFalse);
		when(entityType.getAttribute(ATTR_DATE_AUTO_TRUE)).thenReturn(attrDateAutoTrue);
		when(entityType.getAttribute(ATTR_DATETIME_AUTO_DEFAULT)).thenReturn(attrDateTimeAutoDefault);
		when(entityType.getAttribute(ATTR_DATETIME_AUTO_FALSE)).thenReturn(attrDateTimeAutoFalse);
		when(entityType.getAttribute(ATTR_DATETIME_AUTO_TRUE)).thenReturn(attrDateTimeAutoTrue);
		IdGenerator idGenerator = mock(IdGenerator.class);
		Mockito.when(idGenerator.generateId()).thenReturn("ID1").thenReturn("ID2");
		autoValuePopulator = new AutoValuePopulator(idGenerator);
	}

	@Test
	public void populateAutoValues()
	{
		Entity entity = new DynamicEntity(entityType);
		autoValuePopulator.populate(entity);

		assertNotNull(entity.getIdValue());
		assertNull(entity.getLocalDate(ATTR_DATE_AUTO_DEFAULT));
		assertNull(entity.getLocalDate(ATTR_DATE_AUTO_FALSE));
		assertNotNull(entity.getLocalDate(ATTR_DATE_AUTO_TRUE));
		assertNull(entity.getInstant(ATTR_DATETIME_AUTO_DEFAULT));
		assertNull(entity.getInstant(ATTR_DATETIME_AUTO_FALSE));
		assertNotNull(entity.getInstant(ATTR_DATETIME_AUTO_TRUE));
	}
}