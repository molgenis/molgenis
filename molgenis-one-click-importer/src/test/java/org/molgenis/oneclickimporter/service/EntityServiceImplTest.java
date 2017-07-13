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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.*;

public class EntityServiceImplTest extends AbstractMockitoTest
{

	@Mock
	private DefaultPackage defaultPackage = mock(DefaultPackage.class);

	@Mock
	private EntityTypeFactory entityTypeFactory = mock(EntityTypeFactory.class);

	@Mock
	private AttributeFactory attributeFactory = mock(AttributeFactory.class);

	@Mock
	private IdGenerator idGenerator = mock(IdGenerator.class);

	@Mock
	private DataService dataService = mock(DataService.class);

	@Mock
	private EntityManager entityManager = mock(EntityManager.class);

	private EntityService entityService;

	@BeforeMethod
	public void setup()
	{
		this.entityService = new EntityServiceImpl(defaultPackage, entityTypeFactory, attributeFactory, idGenerator,
				dataService, entityManager);
	}


	@Test
	public void testCreateEntity() throws Exception
	{
		String tableName = "super-powers";
		List<Object> userNames = Arrays.asList("Mark", "Mariska", "Bart");
		List<Object> superPowers = Arrays.asList("Arrow functions", "Cookies", "Knots");
		List<Column> columns = Arrays.asList(
				Column.create("user name", 0, userNames),
				Column.create("super power", 1, superPowers)
		);
		DataCollection dataCollection = DataCollection.create(tableName, columns);

		String generatedId = "id_0";
		EntityType table = mock(EntityType.class);
		when(entityTypeFactory.create()).thenReturn(table);
		when(idGenerator.generateId()).thenReturn(generatedId);

		Attribute idAttr = mock(Attribute.class);
		Attribute nameAttr = mock(Attribute.class);
		Attribute powerAttr = mock(Attribute.class);
		when(attributeFactory.create()).thenReturn(idAttr, nameAttr, powerAttr);
		when(idAttr.setName(anyString())).thenReturn(idAttr);
		when(idAttr.setVisible(anyBoolean())).thenReturn(idAttr);
		when(idAttr.setAuto(anyBoolean())).thenReturn(idAttr);
		when(idAttr.setIdAttribute(anyBoolean())).thenReturn(idAttr);

		MetaDataService meta = mock(MetaDataService.class);
		when(dataService.getMeta()).thenReturn(meta);

		Entity row1 = mock(Entity.class);
		Entity row2 = mock(Entity.class);
		Entity row3 = mock(Entity.class);
		when(entityManager.create(table, EntityManager.CreationMode.NO_POPULATE))
				.thenReturn(row1, row2, row3);

		EntityType dataTable = entityService.createEntity(dataCollection);
		assertEquals(dataTable, table);

		verify(table).setPackage(defaultPackage);
		verify(table).setId(generatedId);
		verify(table).setLabel(tableName);
	}

}