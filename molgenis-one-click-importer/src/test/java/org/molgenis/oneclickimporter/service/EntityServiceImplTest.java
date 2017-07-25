package org.molgenis.oneclickimporter.service;

import org.mockito.Mock;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityManager;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.*;
import org.molgenis.data.meta.model.Package;
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

	@Mock
	private OneClickImporterService oneClickImporterService;

	@Mock
	private PackageFactory packageFactory;

	private EntityService entityService;

	@BeforeMethod
	public void setup()
	{
		this.entityService = new EntityServiceImpl(entityTypeFactory, attributeFactory, idGenerator,
				dataService, metaDataService, entityManager, attributeTypeService, oneClickImporterService,
				packageFactory);

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
		DataCollection dataCollection = DataCollection.create(tableName, columns);

		String generatedId = "id_0";
		EntityType table = mock(EntityType.class);
		when(entityTypeFactory.create()).thenReturn(table);
		when(idGenerator.generateId()).thenReturn(generatedId);
		when(table.getId()).thenReturn(generatedId);

		Package package_ = mock(Package.class);
		when(metaDataService.getPackage("package_")).thenReturn(package_);

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
		when(table.getAttribute("user_name")).thenReturn(nameAttr);
		when(table.getAttribute("super_power")).thenReturn(powerAttr);

		MetaDataService meta = mock(MetaDataService.class);
		when(dataService.getMeta()).thenReturn(meta);
		Entity row1 = mock(Entity.class);
		when(row1.getEntityType()).thenReturn(table);
		Entity row2 = mock(Entity.class);
		when(row2.getEntityType()).thenReturn(table);
		Entity row3 = mock(Entity.class);
		when(row3.getEntityType()).thenReturn(table);
		when(entityManager.create(table, NO_POPULATE)).thenReturn(row1, row2, row3);

		EntityType entityType = entityService.createEntityType(dataCollection, "package_");
		assertEquals(entityType.getId(), generatedId);

		verify(table).setPackage(package_);
		verify(table).setId(generatedId);
		verify(table).setLabel(tableName);
	}
}