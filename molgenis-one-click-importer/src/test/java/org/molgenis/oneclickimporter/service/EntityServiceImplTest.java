package org.molgenis.oneclickimporter.service;

import org.mockito.Mock;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityManager;
import org.molgenis.data.meta.DefaultPackage;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.populate.IdGenerator;
import org.molgenis.oneclickimporter.model.Column;
import org.molgenis.oneclickimporter.model.DataCollection;
import org.molgenis.oneclickimporter.service.Impl.EntityServiceImpl;
import org.molgenis.test.AbstractMockitoTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.molgenis.data.EntityManager.CreationMode.NO_POPULATE;
import static org.molgenis.data.meta.AttributeType.STRING;
import static org.testng.Assert.assertEquals;

public class EntityServiceImplTest extends AbstractMockitoTest
{

	@Mock
	private DefaultPackage defaultPackage;

	@Mock
	private EntityTypeFactory entityTypeFactory;

	@Mock
	private AttributeFactory attributeFactory;

	@Mock
	private IdGenerator idGenerator;

	@Mock
	private DataService dataService;

	@Mock
	private MetaDataService metaDataService;

	@Mock
	private EntityManager entityManager;

	@Mock
	private AttributeTypeService attributeTypeService;

	private EntityService entityService;

	@BeforeMethod
	public void setup()
	{
		this.entityService = new EntityServiceImpl(defaultPackage, entityTypeFactory, attributeFactory, idGenerator,
				dataService, metaDataService, entityManager, attributeTypeService);

		when(attributeTypeService.guessAttributeType(any())).thenReturn(STRING);
	}

	@Test
	public void testCreateEntity() throws Exception
	{
		String tableName = "super-powers";
		List<Object> userNames = Arrays.asList("Mark", "Mariska", "Bart");
		List<Object> superPowers = Arrays.asList("Arrow functions", "Cookies", "Knots");
		List<Column> columns = Arrays.asList(Column.create("user name", 0, userNames),
				Column.create("super power", 1, superPowers));
		DataCollection dataCollection = DataCollection.create(tableName, columns, 3);

		String generatedId = "id_0";
		EntityType table = mock(EntityType.class);
		when(entityTypeFactory.create()).thenReturn(table);
		when(idGenerator.generateId()).thenReturn(generatedId);

		Attribute idAttr = mock(Attribute.class);
		//row.getEntityType().getAttribute(column.getName()).getDataType()
		Attribute nameAttr = mock(Attribute.class);
		when(nameAttr.getDataType()).thenReturn(STRING);
		Attribute powerAttr = mock(Attribute.class);
		when(powerAttr.getDataType()).thenReturn(STRING);
		when(attributeFactory.create()).thenReturn(idAttr, nameAttr, powerAttr);
		when(idAttr.setName(anyString())).thenReturn(idAttr);
		when(idAttr.setVisible(anyBoolean())).thenReturn(idAttr);
		when(idAttr.setAuto(anyBoolean())).thenReturn(idAttr);
		when(idAttr.setIdAttribute(anyBoolean())).thenReturn(idAttr);
		when(table.getAttribute("user name")).thenReturn(nameAttr);
		when(table.getAttribute("super power")).thenReturn(powerAttr);

		MetaDataService meta = mock(MetaDataService.class);
		when(dataService.getMeta()).thenReturn(meta);
		Entity row1 = mock(Entity.class);
		when(row1.getEntityType()).thenReturn(table);
		Entity row2 = mock(Entity.class);
		when(row2.getEntityType()).thenReturn(table);
		Entity row3 = mock(Entity.class);
		when(row3.getEntityType()).thenReturn(table);
		when(entityManager.create(table, NO_POPULATE)).thenReturn(row1, row2, row3);

		String entityTypeId = entityService.createEntityType(dataCollection);
		assertEquals(entityTypeId, generatedId);

		verify(table).setPackage(defaultPackage);
		verify(table).setId(generatedId);
		verify(table).setLabel(tableName);
	}
}