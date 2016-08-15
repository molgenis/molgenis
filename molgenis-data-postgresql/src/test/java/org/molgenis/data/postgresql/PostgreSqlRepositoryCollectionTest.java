package org.molgenis.data.postgresql;

import org.mockito.ArgumentCaptor;
import org.molgenis.data.DataService;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.EntityMetaData;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.sql.DataSource;

import static com.google.common.collect.Lists.newArrayList;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.*;
import static org.molgenis.MolgenisFieldTypes.AttributeType.*;
import static org.testng.Assert.assertEquals;

public class PostgreSqlRepositoryCollectionTest
{
	private PostgreSqlRepositoryCollection postgreSqlRepoCollection;
	private JdbcTemplate jdbcTemplate;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		PostgreSqlEntityFactory postgreSqlEntityFactory = mock(PostgreSqlEntityFactory.class);
		DataSource dataSource = mock(DataSource.class);
		jdbcTemplate = mock(JdbcTemplate.class);
		DataService dataService = mock(DataService.class);
		PlatformTransactionManager platformTransactionManager = mock(PlatformTransactionManager.class);
		postgreSqlRepoCollection = new PostgreSqlRepositoryCollection(postgreSqlEntityFactory, dataSource, jdbcTemplate,
				dataService, platformTransactionManager);
	}

	@Test
	public void updateAttributeNillableToNotNillable() throws Exception
	{
		EntityMetaData entityMeta = when(mock(EntityMetaData.class).getName()).thenReturn("entity").getMock();
		AttributeMetaData attr = when(mock(AttributeMetaData.class).getName()).thenReturn("attr").getMock();
		when(attr.isNillable()).thenReturn(true);
		AttributeMetaData updatedAttr = when(mock(AttributeMetaData.class).getName()).thenReturn("attrNew").getMock();
		when(updatedAttr.isNillable()).thenReturn(false);
		postgreSqlRepoCollection.updateAttribute(entityMeta, attr, updatedAttr);
		ArgumentCaptor<String> captor = forClass(String.class);
		verify(jdbcTemplate).execute(captor.capture());
		assertEquals(captor.getValue(), "ALTER TABLE \"entity\" ALTER COLUMN \"attrNew\" SET NOT NULL");
	}

	@Test
	public void updateAttributeNotNillableToNillable() throws Exception
	{
		EntityMetaData entityMeta = when(mock(EntityMetaData.class).getName()).thenReturn("entity").getMock();
		AttributeMetaData attr = when(mock(AttributeMetaData.class).getName()).thenReturn("attr").getMock();
		when(attr.isNillable()).thenReturn(false);
		AttributeMetaData updatedAttr = when(mock(AttributeMetaData.class).getName()).thenReturn("attrNew").getMock();
		when(updatedAttr.isNillable()).thenReturn(true);
		postgreSqlRepoCollection.updateAttribute(entityMeta, attr, updatedAttr);
		ArgumentCaptor<String> captor = forClass(String.class);
		verify(jdbcTemplate).execute(captor.capture());
		assertEquals(captor.getValue(), "ALTER TABLE \"entity\" ALTER COLUMN \"attrNew\" DROP NOT NULL");
	}

	@Test(expectedExceptions = MolgenisDataException.class)
	public void updateAttributeNotNillableToNillableIdAttr() throws Exception
	{
		EntityMetaData entityMeta = when(mock(EntityMetaData.class).getName()).thenReturn("entity").getMock();
		AttributeMetaData attr = when(mock(AttributeMetaData.class).getName()).thenReturn("attr").getMock();
		when(entityMeta.getIdAttribute()).thenReturn(attr);
		when(attr.isNillable()).thenReturn(false);
		AttributeMetaData updatedAttr = when(mock(AttributeMetaData.class).getName()).thenReturn("attrNew").getMock();
		when(updatedAttr.isNillable()).thenReturn(true);
		postgreSqlRepoCollection.updateAttribute(entityMeta, attr, updatedAttr);
	}

	@Test
	public void updateAttributeUniqueToNotUnique() throws Exception
	{
		EntityMetaData entityMeta = when(mock(EntityMetaData.class).getName()).thenReturn("entity").getMock();
		AttributeMetaData attr = when(mock(AttributeMetaData.class).getName()).thenReturn("attr").getMock();
		when(attr.isUnique()).thenReturn(true);
		AttributeMetaData updatedAttr = when(mock(AttributeMetaData.class).getName()).thenReturn("attrNew").getMock();
		when(updatedAttr.isUnique()).thenReturn(false);
		postgreSqlRepoCollection.updateAttribute(entityMeta, attr, updatedAttr);
		ArgumentCaptor<String> captor = forClass(String.class);
		verify(jdbcTemplate).execute(captor.capture());
		assertEquals(captor.getValue(), "ALTER TABLE \"entity\" DROP CONSTRAINT \"entity_attrNew_key\"");
	}

	@Test(expectedExceptions = MolgenisDataException.class)
	public void updateAttributeUniqueToNotUniqueIdAttr() throws Exception
	{
		EntityMetaData entityMeta = when(mock(EntityMetaData.class).getName()).thenReturn("entity").getMock();
		AttributeMetaData attr = when(mock(AttributeMetaData.class).getName()).thenReturn("attr").getMock();
		when(entityMeta.getIdAttribute()).thenReturn(attr);
		when(attr.isUnique()).thenReturn(true);
		AttributeMetaData updatedAttr = when(mock(AttributeMetaData.class).getName()).thenReturn("attrNew").getMock();
		when(updatedAttr.isUnique()).thenReturn(false);
		postgreSqlRepoCollection.updateAttribute(entityMeta, attr, updatedAttr);
	}

	@Test
	public void updateAttributeNotUniqueToUnique() throws Exception
	{
		EntityMetaData entityMeta = when(mock(EntityMetaData.class).getName()).thenReturn("entity").getMock();
		AttributeMetaData attr = when(mock(AttributeMetaData.class).getName()).thenReturn("attr").getMock();
		when(attr.isUnique()).thenReturn(false);
		AttributeMetaData updatedAttr = when(mock(AttributeMetaData.class).getName()).thenReturn("attrNew").getMock();
		when(updatedAttr.isUnique()).thenReturn(true);
		postgreSqlRepoCollection.updateAttribute(entityMeta, attr, updatedAttr);
		ArgumentCaptor<String> captor = forClass(String.class);
		verify(jdbcTemplate).execute(captor.capture());
		assertEquals(captor.getValue(),
				"ALTER TABLE \"entity\" ADD CONSTRAINT \"entity_attrNew_key\" UNIQUE (\"attrNew\")");
	}

	@Test
	public void updateAttributeDataTypeToDataType() throws Exception
	{
		EntityMetaData entityMeta = when(mock(EntityMetaData.class).getName()).thenReturn("entity").getMock();
		AttributeMetaData attr = when(mock(AttributeMetaData.class).getName()).thenReturn("attr").getMock();
		when(attr.getDataType()).thenReturn(STRING);
		AttributeMetaData updatedAttr = when(mock(AttributeMetaData.class).getName()).thenReturn("attrNew").getMock();
		when(updatedAttr.getDataType()).thenReturn(INT);
		postgreSqlRepoCollection.updateAttribute(entityMeta, attr, updatedAttr);
		ArgumentCaptor<String> captor = forClass(String.class);
		verify(jdbcTemplate).execute(captor.capture());
		assertEquals(captor.getValue(),
				"ALTER TABLE \"entity\" ALTER COLUMN \"attrNew\" SET DATA TYPE integer USING \"attrNew\"::integer");
	}

	@Test
	public void updateAttributeSingleRefDataTypeToDataType() throws Exception
	{
		EntityMetaData entityMeta = when(mock(EntityMetaData.class).getName()).thenReturn("entity").getMock();
		AttributeMetaData attr = when(mock(AttributeMetaData.class).getName()).thenReturn("attr").getMock();
		when(attr.getDataType()).thenReturn(XREF);
		AttributeMetaData updatedAttr = when(mock(AttributeMetaData.class).getName()).thenReturn("attrNew").getMock();
		when(updatedAttr.getDataType()).thenReturn(STRING);
		postgreSqlRepoCollection.updateAttribute(entityMeta, attr, updatedAttr);
		ArgumentCaptor<String> captor = forClass(String.class);
		verify(jdbcTemplate, times(2)).execute(captor.capture());
		assertEquals(captor.getAllValues(), newArrayList("ALTER TABLE \"entity\" DROP CONSTRAINT \"entity_attr_fkey\"",
				"ALTER TABLE \"entity\" ALTER COLUMN \"attrNew\" SET DATA TYPE character varying(255) USING \"attrNew\"::character varying(255)"));
	}

	@Test
	public void updateAttributeDataTypeToSingleRefDataType() throws Exception
	{
		AttributeMetaData refIdAttr = when(mock(AttributeMetaData.class).getName()).thenReturn("refIdAttr").getMock();
		when(refIdAttr.getDataType()).thenReturn(STRING);
		EntityMetaData refEntityMeta = when(mock(EntityMetaData.class).getName()).thenReturn("refEntity").getMock();
		when(refEntityMeta.getIdAttribute()).thenReturn(refIdAttr);
		EntityMetaData entityMeta = when(mock(EntityMetaData.class).getName()).thenReturn("entity").getMock();
		AttributeMetaData attr = when(mock(AttributeMetaData.class).getName()).thenReturn("attr").getMock();
		when(attr.getDataType()).thenReturn(STRING);
		AttributeMetaData updatedAttr = when(mock(AttributeMetaData.class).getName()).thenReturn("attrNew").getMock();
		when(updatedAttr.getDataType()).thenReturn(XREF);
		when(updatedAttr.getRefEntity()).thenReturn(refEntityMeta);
		postgreSqlRepoCollection.updateAttribute(entityMeta, attr, updatedAttr);
		ArgumentCaptor<String> captor = forClass(String.class);
		verify(jdbcTemplate, times(2)).execute(captor.capture());
		assertEquals(captor.getAllValues(), newArrayList(
				"ALTER TABLE \"entity\" ALTER COLUMN \"attrNew\" SET DATA TYPE character varying(255) USING \"attrNew\"::character varying(255)",
				"ALTER TABLE \"entity\" ADD CONSTRAINT \"entity_attrNew_fkey\" FOREIGN KEY (\"attrNew\") REFERENCES \"refEntity\"(\"refIdAttr\")"));
	}

	@Test(expectedExceptions = MolgenisDataException.class)
	public void updateAttributeDataTypeToDataTypeIdAttr() throws Exception
	{
		EntityMetaData entityMeta = when(mock(EntityMetaData.class).getName()).thenReturn("entity").getMock();
		AttributeMetaData attr = when(mock(AttributeMetaData.class).getName()).thenReturn("attr").getMock();
		when(entityMeta.getIdAttribute()).thenReturn(attr);
		when(attr.getDataType()).thenReturn(STRING);
		AttributeMetaData updatedAttr = when(mock(AttributeMetaData.class).getName()).thenReturn("attrNew").getMock();
		when(updatedAttr.getDataType()).thenReturn(INT);
		postgreSqlRepoCollection.updateAttribute(entityMeta, attr, updatedAttr);
	}
}