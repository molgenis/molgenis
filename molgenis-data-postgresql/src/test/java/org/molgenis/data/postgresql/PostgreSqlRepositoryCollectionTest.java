package org.molgenis.data.postgresql;

import org.mockito.ArgumentCaptor;
import org.molgenis.data.*;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.EntityMetaData;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.sql.DataSource;
import java.util.stream.Stream;

import static com.google.common.collect.Lists.newArrayList;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.*;
import static org.molgenis.MolgenisFieldTypes.AttributeType.*;
import static org.molgenis.data.meta.model.EntityMetaDataMetaData.ENTITY_META_DATA;
import static org.molgenis.data.meta.model.EntityMetaDataMetaData.EXTENDS;
import static org.molgenis.data.postgresql.PostgreSqlRepositoryCollection.POSTGRESQL;
import static org.testng.Assert.assertEquals;

public class PostgreSqlRepositoryCollectionTest
{
	private PostgreSqlRepositoryCollection postgreSqlRepoCollection;
	private JdbcTemplate jdbcTemplate;
	private DataService dataService;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		PostgreSqlEntityFactory postgreSqlEntityFactory = mock(PostgreSqlEntityFactory.class);
		DataSource dataSource = mock(DataSource.class);
		jdbcTemplate = mock(JdbcTemplate.class);
		dataService = mock(DataService.class);
		PlatformTransactionManager platformTransactionManager = mock(PlatformTransactionManager.class);
		postgreSqlRepoCollection = new PostgreSqlRepositoryCollection(postgreSqlEntityFactory, dataSource, jdbcTemplate,
				dataService, platformTransactionManager);
	}

	@Test
	public void updateAttribute()
	{
		EntityMetaData entityMeta = when(mock(EntityMetaData.class).getName()).thenReturn("entity").getMock();
		String attrName = "attr";
		AttributeMetaData attr = when(mock(AttributeMetaData.class).getName()).thenReturn(attrName).getMock();
		when(entityMeta.getAttribute(attrName)).thenReturn(attr);
		when(attr.getLabel()).thenReturn("label");
		AttributeMetaData updatedAttr = when(mock(AttributeMetaData.class).getName()).thenReturn(attrName).getMock();
		when(updatedAttr.getLabel()).thenReturn("updated label");
		postgreSqlRepoCollection.updateAttribute(entityMeta, attr, updatedAttr);
		verifyZeroInteractions(jdbcTemplate);
	}

	@Test
	public void updateAttributeOrderBySet()
	{
		EntityMetaData entityMeta = when(mock(EntityMetaData.class).getName()).thenReturn("entity").getMock();
		String attrName = "attr";
		AttributeMetaData attr = when(mock(AttributeMetaData.class).getName()).thenReturn(attrName).getMock();
		when(entityMeta.getAttribute(attrName)).thenReturn(attr);
		when(attr.getDataType()).thenReturn(ONE_TO_MANY);
		String refAttrName = "refAttr";
		AttributeMetaData refAttr = when(mock(AttributeMetaData.class).getName()).thenReturn(refAttrName).getMock();
		when(attr.isMappedBy()).thenReturn(true);
		when(attr.getMappedBy()).thenReturn(refAttr);
		when(attr.getOrderBy()).thenReturn(null);
		EntityMetaData refEntityMeta = when(mock(EntityMetaData.class).getName()).thenReturn("refEntity").getMock();
		when(attr.getRefEntity()).thenReturn(refEntityMeta);
		AttributeMetaData updatedAttr = when(mock(AttributeMetaData.class).getName()).thenReturn(attrName).getMock();
		when(updatedAttr.getDataType()).thenReturn(ONE_TO_MANY);
		when(attr.isMappedBy()).thenReturn(true);
		when(attr.getMappedBy()).thenReturn(refAttr);
		when(updatedAttr.getOrderBy()).thenReturn(new Sort("orderByAttr"));
		when(updatedAttr.getRefEntity()).thenReturn(refEntityMeta);
		postgreSqlRepoCollection.updateAttribute(entityMeta, attr, updatedAttr);
		ArgumentCaptor<String> captor = forClass(String.class);
		verify(jdbcTemplate).execute(captor.capture());
		assertEquals(captor.getValue(), "ALTER TABLE \"refEntity\" DROP COLUMN \"refAttr_order\"");
	}

	@Test
	public void updateAttributeOrderByRemove()
	{
		AttributeMetaData idAttr = when(mock(AttributeMetaData.class).getName()).thenReturn("id").getMock();
		EntityMetaData entityMeta = when(mock(EntityMetaData.class).getName()).thenReturn("entity").getMock();
		when(entityMeta.getBackend()).thenReturn(POSTGRESQL);
		when(entityMeta.getIdAttribute()).thenReturn(idAttr);
		AttributeMetaData refIdAttr = when(mock(AttributeMetaData.class).getName()).thenReturn("refId").getMock();
		EntityMetaData refEntityMeta = when(mock(EntityMetaData.class).getName()).thenReturn("refEntity").getMock();
		when(refEntityMeta.getBackend()).thenReturn(POSTGRESQL);
		when(refEntityMeta.getIdAttribute()).thenReturn(refIdAttr);
		String attrName = "attr";
		AttributeMetaData attr = when(mock(AttributeMetaData.class).getName()).thenReturn(attrName).getMock();
		String refAttrName = "refAttr";
		AttributeMetaData refAttr = when(mock(AttributeMetaData.class).getName()).thenReturn(refAttrName).getMock();
		when(attr.getDataType()).thenReturn(ONE_TO_MANY);
		when(attr.isMappedBy()).thenReturn(true);
		when(attr.getMappedBy()).thenReturn(refAttr);
		when(attr.getOrderBy()).thenReturn(new Sort("orderByAttr"));
		when(attr.getRefEntity()).thenReturn(refEntityMeta);
		when(entityMeta.getAttribute(attrName)).thenReturn(attr);
		AttributeMetaData updatedAttr = when(mock(AttributeMetaData.class).getName()).thenReturn(attrName).getMock();
		when(updatedAttr.getDataType()).thenReturn(ONE_TO_MANY);
		when(updatedAttr.isMappedBy()).thenReturn(true);
		when(updatedAttr.getMappedBy()).thenReturn(refAttr);
		when(updatedAttr.getOrderBy()).thenReturn(null);
		when(updatedAttr.getRefEntity()).thenReturn(refEntityMeta);
		postgreSqlRepoCollection.updateAttribute(entityMeta, attr, updatedAttr);
		ArgumentCaptor<String> captor = forClass(String.class);
		verify(jdbcTemplate).execute(captor.capture());
		assertEquals(captor.getValue(), "ALTER TABLE \"refEntity\" ADD \"refAttr_order\" SERIAL");
	}

	@Test
	public void updateAttributeNillableToNotNillable()
	{
		EntityMetaData entityMeta = when(mock(EntityMetaData.class).getName()).thenReturn("entity").getMock();
		String attrName = "attr";
		AttributeMetaData attr = when(mock(AttributeMetaData.class).getName()).thenReturn(attrName).getMock();
		when(entityMeta.getAttribute(attrName)).thenReturn(attr);
		when(attr.isNillable()).thenReturn(true);
		AttributeMetaData updatedAttr = when(mock(AttributeMetaData.class).getName()).thenReturn(attrName).getMock();
		when(updatedAttr.isNillable()).thenReturn(false);
		postgreSqlRepoCollection.updateAttribute(entityMeta, attr, updatedAttr);
		ArgumentCaptor<String> captor = forClass(String.class);
		verify(jdbcTemplate).execute(captor.capture());
		assertEquals(captor.getValue(), "ALTER TABLE \"entity\" ALTER COLUMN \"attr\" SET NOT NULL");
	}

	@Test
	public void updateAttributeNotNillableToNillable()
	{
		EntityMetaData entityMeta = when(mock(EntityMetaData.class).getName()).thenReturn("entity").getMock();
		String attrName = "attr";
		AttributeMetaData attr = when(mock(AttributeMetaData.class).getName()).thenReturn(attrName).getMock();
		when(entityMeta.getAttribute(attrName)).thenReturn(attr);
		when(attr.isNillable()).thenReturn(false);
		AttributeMetaData updatedAttr = when(mock(AttributeMetaData.class).getName()).thenReturn(attrName).getMock();
		when(updatedAttr.isNillable()).thenReturn(true);
		postgreSqlRepoCollection.updateAttribute(entityMeta, attr, updatedAttr);
		ArgumentCaptor<String> captor = forClass(String.class);
		verify(jdbcTemplate).execute(captor.capture());
		assertEquals(captor.getValue(), "ALTER TABLE \"entity\" ALTER COLUMN \"attr\" DROP NOT NULL");
	}

	@Test(expectedExceptions = MolgenisDataException.class)
	public void updateAttributeNotNillableToNillableIdAttr()
	{
		EntityMetaData entityMeta = when(mock(EntityMetaData.class).getName()).thenReturn("entity").getMock();
		String attrName = "attr";
		AttributeMetaData attr = when(mock(AttributeMetaData.class).getName()).thenReturn(attrName).getMock();
		when(entityMeta.getAttribute(attrName)).thenReturn(attr);
		when(entityMeta.getIdAttribute()).thenReturn(attr);
		when(attr.isNillable()).thenReturn(false);
		AttributeMetaData updatedAttr = when(mock(AttributeMetaData.class).getName()).thenReturn(attrName).getMock();
		when(updatedAttr.isNillable()).thenReturn(true);
		postgreSqlRepoCollection.updateAttribute(entityMeta, attr, updatedAttr);
	}

	@Test
	public void updateAttributeUniqueToNotUnique()
	{
		EntityMetaData entityMeta = when(mock(EntityMetaData.class).getName()).thenReturn("entity").getMock();
		String attrName = "attr";
		AttributeMetaData attr = when(mock(AttributeMetaData.class).getName()).thenReturn(attrName).getMock();
		when(entityMeta.getAttribute(attrName)).thenReturn(attr);
		when(attr.isUnique()).thenReturn(true);
		AttributeMetaData updatedAttr = when(mock(AttributeMetaData.class).getName()).thenReturn(attrName).getMock();
		when(updatedAttr.isUnique()).thenReturn(false);
		postgreSqlRepoCollection.updateAttribute(entityMeta, attr, updatedAttr);
		ArgumentCaptor<String> captor = forClass(String.class);
		verify(jdbcTemplate).execute(captor.capture());
		assertEquals(captor.getValue(), "ALTER TABLE \"entity\" DROP CONSTRAINT \"entity_attr_key\"");
	}

	@Test(expectedExceptions = MolgenisDataException.class)
	public void updateAttributeUniqueToNotUniqueIdAttr()
	{
		EntityMetaData entityMeta = when(mock(EntityMetaData.class).getName()).thenReturn("entity").getMock();
		String attrName = "attr";
		AttributeMetaData attr = when(mock(AttributeMetaData.class).getName()).thenReturn("attr").getMock();
		when(entityMeta.getIdAttribute()).thenReturn(attr);
		when(entityMeta.getAttribute(attrName)).thenReturn(attr);
		when(attr.isUnique()).thenReturn(true);
		AttributeMetaData updatedAttr = when(mock(AttributeMetaData.class).getName()).thenReturn(attrName).getMock();
		when(updatedAttr.isUnique()).thenReturn(false);
		postgreSqlRepoCollection.updateAttribute(entityMeta, attr, updatedAttr);
	}

	@Test
	public void updateAttributeNotUniqueToUnique()
	{
		EntityMetaData entityMeta = when(mock(EntityMetaData.class).getName()).thenReturn("entity").getMock();
		String attrName = "attr";
		AttributeMetaData attr = when(mock(AttributeMetaData.class).getName()).thenReturn(attrName).getMock();
		when(entityMeta.getAttribute(attrName)).thenReturn(attr);
		when(attr.isUnique()).thenReturn(false);
		AttributeMetaData updatedAttr = when(mock(AttributeMetaData.class).getName()).thenReturn(attrName).getMock();
		when(updatedAttr.isUnique()).thenReturn(true);
		postgreSqlRepoCollection.updateAttribute(entityMeta, attr, updatedAttr);
		ArgumentCaptor<String> captor = forClass(String.class);
		verify(jdbcTemplate).execute(captor.capture());
		assertEquals(captor.getValue(), "ALTER TABLE \"entity\" ADD CONSTRAINT \"entity_attr_key\" UNIQUE (\"attr\")");
	}

	@Test
	public void updateAttributeDataTypeToDataType()
	{
		EntityMetaData entityMeta = when(mock(EntityMetaData.class).getName()).thenReturn("entity").getMock();
		String attrName = "attr";
		AttributeMetaData attr = when(mock(AttributeMetaData.class).getName()).thenReturn(attrName).getMock();
		when(entityMeta.getAttribute(attrName)).thenReturn(attr);
		when(attr.getDataType()).thenReturn(STRING);
		AttributeMetaData updatedAttr = when(mock(AttributeMetaData.class).getName()).thenReturn(attrName).getMock();
		when(updatedAttr.getDataType()).thenReturn(INT);
		postgreSqlRepoCollection.updateAttribute(entityMeta, attr, updatedAttr);
		ArgumentCaptor<String> captor = forClass(String.class);
		verify(jdbcTemplate).execute(captor.capture());
		assertEquals(captor.getValue(),
				"ALTER TABLE \"entity\" ALTER COLUMN \"attr\" SET DATA TYPE integer USING \"attr\"::integer");
	}

	@Test
	public void updateAttributeSingleRefDataTypeToDataType()
	{
		EntityMetaData entityMeta = when(mock(EntityMetaData.class).getName()).thenReturn("entity").getMock();
		String attrName = "attr";
		AttributeMetaData attr = when(mock(AttributeMetaData.class).getName()).thenReturn(attrName).getMock();
		when(entityMeta.getAttribute(attrName)).thenReturn(attr);
		when(attr.getDataType()).thenReturn(XREF);
		AttributeMetaData updatedAttr = when(mock(AttributeMetaData.class).getName()).thenReturn(attrName).getMock();
		when(updatedAttr.getDataType()).thenReturn(STRING);
		postgreSqlRepoCollection.updateAttribute(entityMeta, attr, updatedAttr);
		ArgumentCaptor<String> captor = forClass(String.class);
		verify(jdbcTemplate, times(2)).execute(captor.capture());
		assertEquals(captor.getAllValues(), newArrayList("ALTER TABLE \"entity\" DROP CONSTRAINT \"entity_attr_fkey\"",
				"ALTER TABLE \"entity\" ALTER COLUMN \"attr\" SET DATA TYPE character varying(255) USING \"attr\"::character varying(255)"));
	}

	@Test
	public void updateAttributeSingleRefDataTypeToSingleRefDataType()
	{
		EntityMetaData entityMeta = when(mock(EntityMetaData.class).getName()).thenReturn("entity").getMock();
		String attrName = "attr";
		AttributeMetaData attr = when(mock(AttributeMetaData.class).getName()).thenReturn(attrName).getMock();
		when(entityMeta.getAttribute(attrName)).thenReturn(attr);
		when(attr.getDataType()).thenReturn(XREF);
		AttributeMetaData updatedAttr = when(mock(AttributeMetaData.class).getName()).thenReturn(attrName).getMock();
		when(updatedAttr.getDataType()).thenReturn(CATEGORICAL);
		postgreSqlRepoCollection.updateAttribute(entityMeta, attr, updatedAttr);
		verifyZeroInteractions(jdbcTemplate);
	}

	@Test
	public void updateAttributeMultiRefDataTypeToMultiRefDataType()
	{
		EntityMetaData entityMeta = when(mock(EntityMetaData.class).getName()).thenReturn("entity").getMock();
		String attrName = "attr";
		AttributeMetaData attr = when(mock(AttributeMetaData.class).getName()).thenReturn(attrName).getMock();
		when(entityMeta.getAttribute(attrName)).thenReturn(attr);
		when(attr.getDataType()).thenReturn(MREF);
		AttributeMetaData updatedAttr = when(mock(AttributeMetaData.class).getName()).thenReturn(attrName).getMock();
		when(updatedAttr.getDataType()).thenReturn(CATEGORICAL_MREF);
		postgreSqlRepoCollection.updateAttribute(entityMeta, attr, updatedAttr);
		verifyZeroInteractions(jdbcTemplate);
	}

	@Test
	public void updateAttributeDataTypeToSingleRefDataType()
	{
		AttributeMetaData refIdAttr = when(mock(AttributeMetaData.class).getName()).thenReturn("refIdAttr").getMock();
		when(refIdAttr.getDataType()).thenReturn(STRING);
		EntityMetaData refEntityMeta = when(mock(EntityMetaData.class).getName()).thenReturn("refEntity").getMock();
		when(refEntityMeta.getIdAttribute()).thenReturn(refIdAttr);
		EntityMetaData entityMeta = when(mock(EntityMetaData.class).getName()).thenReturn("entity").getMock();
		String attrName = "attr";
		AttributeMetaData attr = when(mock(AttributeMetaData.class).getName()).thenReturn(attrName).getMock();
		when(entityMeta.getAttribute(attrName)).thenReturn(attr);
		when(attr.getDataType()).thenReturn(STRING);
		AttributeMetaData updatedAttr = when(mock(AttributeMetaData.class).getName()).thenReturn(attrName).getMock();
		when(updatedAttr.getDataType()).thenReturn(XREF);
		when(updatedAttr.getRefEntity()).thenReturn(refEntityMeta);
		postgreSqlRepoCollection.updateAttribute(entityMeta, attr, updatedAttr);
		ArgumentCaptor<String> captor = forClass(String.class);
		verify(jdbcTemplate, times(2)).execute(captor.capture());
		assertEquals(captor.getAllValues(), newArrayList(
				"ALTER TABLE \"entity\" ALTER COLUMN \"attr\" SET DATA TYPE character varying(255) USING \"attr\"::character varying(255)",
				"ALTER TABLE \"entity\" ADD CONSTRAINT \"entity_attr_fkey\" FOREIGN KEY (\"attr\") REFERENCES \"refEntity\"(\"refIdAttr\")"));
	}

	@Test(expectedExceptions = MolgenisDataException.class)
	public void updateAttributeDataTypeToDataTypeIdAttr()
	{
		EntityMetaData entityMeta = when(mock(EntityMetaData.class).getName()).thenReturn("entity").getMock();
		AttributeMetaData attr = when(mock(AttributeMetaData.class).getName()).thenReturn("attr").getMock();
		when(entityMeta.getIdAttribute()).thenReturn(attr);
		when(attr.getDataType()).thenReturn(STRING);
		AttributeMetaData updatedAttr = when(mock(AttributeMetaData.class).getName()).thenReturn("attr").getMock();
		when(updatedAttr.getDataType()).thenReturn(INT);
		postgreSqlRepoCollection.updateAttribute(entityMeta, attr, updatedAttr);
	}

	@Test
	public void updateAttributeWithExpressionBefore()
	{
		EntityMetaData entityMeta = when(mock(EntityMetaData.class).getName()).thenReturn("entity").getMock();
		String attrName = "attr";
		AttributeMetaData idAttr = when(mock(AttributeMetaData.class).getName()).thenReturn("id").getMock();
		when(entityMeta.getIdAttribute()).thenReturn(idAttr);
		AttributeMetaData attr = when(mock(AttributeMetaData.class).getName()).thenReturn(attrName).getMock();
		when(entityMeta.getAttribute(attrName)).thenReturn(attr);
		when(attr.getExpression()).thenReturn("expression");
		when(attr.getDataType()).thenReturn(STRING);
		when(attr.isNillable()).thenReturn(false);
		AttributeMetaData updatedAttr = when(mock(AttributeMetaData.class).getName()).thenReturn(attrName).getMock();
		when(updatedAttr.getExpression()).thenReturn(null);
		when(updatedAttr.getDataType()).thenReturn(STRING);
		when(updatedAttr.isNillable()).thenReturn(true);
		postgreSqlRepoCollection.updateAttribute(entityMeta, attr, updatedAttr);
		verify(jdbcTemplate).execute("ALTER TABLE \"entity\" ADD \"attr\" character varying(255)");
	}

	@Test
	public void updateAttributeWithExpressionAfter()
	{
		EntityMetaData entityMeta = when(mock(EntityMetaData.class).getName()).thenReturn("entity").getMock();
		String attrName = "attr";
		AttributeMetaData attr = when(mock(AttributeMetaData.class).getName()).thenReturn(attrName).getMock();
		when(attr.getDataType()).thenReturn(STRING);
		when(entityMeta.getAttribute(attrName)).thenReturn(attr);
		when(attr.getExpression()).thenReturn(null);
		when(attr.isNillable()).thenReturn(false);
		AttributeMetaData updatedAttr = when(mock(AttributeMetaData.class).getName()).thenReturn(attrName).getMock();
		when(updatedAttr.getExpression()).thenReturn("expression");
		when(updatedAttr.isNillable()).thenReturn(true);
		postgreSqlRepoCollection.updateAttribute(entityMeta, attr, updatedAttr);
		verify(jdbcTemplate).execute("ALTER TABLE \"entity\" DROP COLUMN \"attr\"");
	}

	@Test
	public void updateAttributeWithExpressionBeforeAfter()
	{
		EntityMetaData entityMeta = when(mock(EntityMetaData.class).getName()).thenReturn("entity").getMock();
		String attrName = "attr";
		AttributeMetaData attr = when(mock(AttributeMetaData.class).getName()).thenReturn(attrName).getMock();
		when(entityMeta.getAttribute(attrName)).thenReturn(attr);
		when(attr.getExpression()).thenReturn("expression");
		when(attr.isNillable()).thenReturn(false);
		AttributeMetaData updatedAttr = when(mock(AttributeMetaData.class).getName()).thenReturn(attrName).getMock();
		when(updatedAttr.getExpression()).thenReturn("expression");
		when(updatedAttr.isNillable()).thenReturn(true);
		postgreSqlRepoCollection.updateAttribute(entityMeta, attr, updatedAttr);
		verifyZeroInteractions(jdbcTemplate);
	}

	@Test
	public void updateAttributeCompoundBefore()
	{
		EntityMetaData entityMeta = when(mock(EntityMetaData.class).getName()).thenReturn("entity").getMock();
		String attrName = "attr";
		AttributeMetaData idAttr = when(mock(AttributeMetaData.class).getName()).thenReturn("id").getMock();
		when(entityMeta.getIdAttribute()).thenReturn(idAttr);
		AttributeMetaData attr = when(mock(AttributeMetaData.class).getName()).thenReturn(attrName).getMock();
		when(entityMeta.getAttribute(attrName)).thenReturn(attr);
		when(attr.getDataType()).thenReturn(COMPOUND);
		AttributeMetaData updatedAttr = when(mock(AttributeMetaData.class).getName()).thenReturn(attrName).getMock();
		when(updatedAttr.getDataType()).thenReturn(STRING);
		postgreSqlRepoCollection.updateAttribute(entityMeta, attr, updatedAttr);
		verify(jdbcTemplate).execute("ALTER TABLE \"entity\" ADD \"attr\" character varying(255) NOT NULL");
	}

	@Test
	public void updateAttributeCompoundAfter()
	{
		EntityMetaData entityMeta = when(mock(EntityMetaData.class).getName()).thenReturn("entity").getMock();
		String attrName = "attr";
		AttributeMetaData attr = when(mock(AttributeMetaData.class).getName()).thenReturn(attrName).getMock();
		when(entityMeta.getAttribute(attrName)).thenReturn(attr);
		when(attr.getDataType()).thenReturn(STRING);
		AttributeMetaData updatedAttr = when(mock(AttributeMetaData.class).getName()).thenReturn(attrName).getMock();
		when(updatedAttr.getDataType()).thenReturn(COMPOUND);
		postgreSqlRepoCollection.updateAttribute(entityMeta, attr, updatedAttr);
		verify(jdbcTemplate).execute("ALTER TABLE \"entity\" DROP COLUMN \"attr\"");
	}

	@Test
	public void updateAttributeCompoundBeforeAfter()
	{
		EntityMetaData entityMeta = when(mock(EntityMetaData.class).getName()).thenReturn("entity").getMock();
		String attrName = "attr";
		AttributeMetaData attr = when(mock(AttributeMetaData.class).getName()).thenReturn(attrName).getMock();
		when(entityMeta.getAttribute(attrName)).thenReturn(attr);
		when(attr.getDataType()).thenReturn(COMPOUND);
		when(attr.isNillable()).thenReturn(false);
		AttributeMetaData updatedAttr = when(mock(AttributeMetaData.class).getName()).thenReturn(attrName).getMock();
		when(updatedAttr.getDataType()).thenReturn(COMPOUND);
		when(updatedAttr.isNillable()).thenReturn(true);
		postgreSqlRepoCollection.updateAttribute(entityMeta, attr, updatedAttr);
		verifyZeroInteractions(jdbcTemplate);
	}

	@Test
	public void updateAttributeAbstractEntity()
	{
		EntityMetaData abstractEntityMeta = when(mock(EntityMetaData.class).getName()).thenReturn("root").getMock();
		when(abstractEntityMeta.isAbstract()).thenReturn(true);
		String attrName = "attr";
		AttributeMetaData attr = when(mock(AttributeMetaData.class).getName()).thenReturn(attrName).getMock();
		when(abstractEntityMeta.getAttribute(attrName)).thenReturn(attr);
		when(attr.isNillable()).thenReturn(true);
		AttributeMetaData updatedAttr = when(mock(AttributeMetaData.class).getName()).thenReturn(attrName).getMock();
		when(updatedAttr.isNillable()).thenReturn(false);

		EntityMetaData entityMeta0 = when(mock(EntityMetaData.class).getName()).thenReturn("entity0").getMock();
		when(entityMeta0.getExtends()).thenReturn(abstractEntityMeta);
		when(entityMeta0.isAbstract()).thenReturn(true);
		EntityMetaData entityMeta0a = when(mock(EntityMetaData.class).getName()).thenReturn("entity0a").getMock();
		when(entityMeta0a.getExtends()).thenReturn(entityMeta0);
		when(entityMeta0a.isAbstract()).thenReturn(false);
		EntityMetaData entityMeta0b = when(mock(EntityMetaData.class).getName()).thenReturn("entity0b").getMock();
		when(entityMeta0b.getExtends()).thenReturn(entityMeta0);
		when(entityMeta0b.isAbstract()).thenReturn(false);
		EntityMetaData entityMeta1 = when(mock(EntityMetaData.class).getName()).thenReturn("entity1").getMock();
		when(entityMeta1.getExtends()).thenReturn(abstractEntityMeta);
		when(entityMeta1.isAbstract()).thenReturn(false);

		//noinspection unchecked
		Query<EntityMetaData> entityQ = mock(Query.class);
		when(dataService.query(ENTITY_META_DATA, EntityMetaData.class)).thenReturn(entityQ);
		//noinspection unchecked
		Query<EntityMetaData> entityQ0 = mock(Query.class);
		//noinspection unchecked
		Query<EntityMetaData> entityQ1 = mock(Query.class);
		when(entityQ.eq(EXTENDS, abstractEntityMeta)).thenReturn(entityQ0);
		when(entityQ.eq(EXTENDS, entityMeta0)).thenReturn(entityQ1);
		when(entityQ0.findAll()).thenReturn(Stream.of(entityMeta0, entityMeta1));
		when(entityQ1.findAll()).thenReturn(Stream.of(entityMeta0a, entityMeta0b));

		postgreSqlRepoCollection.updateAttribute(abstractEntityMeta, attr, updatedAttr);
		ArgumentCaptor<String> captor = forClass(String.class);
		verify(jdbcTemplate, times(3)).execute(captor.capture());
		assertEquals(captor.getAllValues(), newArrayList("ALTER TABLE \"entity0a\" ALTER COLUMN \"attr\" SET NOT NULL",
				"ALTER TABLE \"entity0b\" ALTER COLUMN \"attr\" SET NOT NULL",
				"ALTER TABLE \"entity1\" ALTER COLUMN \"attr\" SET NOT NULL"));
	}

	@Test(expectedExceptions = UnknownAttributeException.class)
	public void updateAttributeDoesNotExist()
	{
		EntityMetaData entityMeta = when(mock(EntityMetaData.class).getName()).thenReturn("entity").getMock();
		AttributeMetaData attr = when(mock(AttributeMetaData.class).getName()).thenReturn("attr").getMock();
		when(attr.isNillable()).thenReturn(true);
		AttributeMetaData updatedAttr = when(mock(AttributeMetaData.class).getName()).thenReturn("attr").getMock();
		when(updatedAttr.isNillable()).thenReturn(false);
		postgreSqlRepoCollection.updateAttribute(entityMeta, attr, updatedAttr);
	}

	@Test
	public void updateAttributeRefEntityXref()
	{
		AttributeMetaData refIdAttr0 = when(mock(AttributeMetaData.class).getName()).thenReturn("refIdAttr0").getMock();
		when(refIdAttr0.getDataType()).thenReturn(STRING);
		EntityMetaData refEntityMeta0 = when(mock(EntityMetaData.class).getName()).thenReturn("refEntity0").getMock();
		when(refEntityMeta0.getIdAttribute()).thenReturn(refIdAttr0);

		AttributeMetaData refIdAttr1 = when(mock(AttributeMetaData.class).getName()).thenReturn("refIdAttr1").getMock();
		when(refIdAttr1.getDataType()).thenReturn(STRING);
		EntityMetaData refEntityMeta1 = when(mock(EntityMetaData.class).getName()).thenReturn("refEntity1").getMock();
		when(refEntityMeta1.getIdAttribute()).thenReturn(refIdAttr1);

		EntityMetaData entityMeta = when(mock(EntityMetaData.class).getName()).thenReturn("entity").getMock();
		String attrName = "attr";

		AttributeMetaData attr = when(mock(AttributeMetaData.class).getName()).thenReturn(attrName).getMock();
		when(entityMeta.getAttribute(attrName)).thenReturn(attr);
		when(attr.getDataType()).thenReturn(XREF);
		when(attr.getRefEntity()).thenReturn(refEntityMeta0);

		AttributeMetaData updatedAttr = when(mock(AttributeMetaData.class).getName()).thenReturn(attrName).getMock();
		when(updatedAttr.getDataType()).thenReturn(XREF);
		when(updatedAttr.getRefEntity()).thenReturn(refEntityMeta1);

		postgreSqlRepoCollection.updateAttribute(entityMeta, attr, updatedAttr);
		ArgumentCaptor<String> captor = forClass(String.class);
		verify(jdbcTemplate, times(2)).execute(captor.capture());
		assertEquals(captor.getAllValues(), newArrayList("ALTER TABLE \"entity\" DROP CONSTRAINT \"entity_attr_fkey\"",
				"ALTER TABLE \"entity\" ADD CONSTRAINT \"entity_attr_fkey\" FOREIGN KEY (\"attr\") REFERENCES \"refEntity1\"(\"refIdAttr1\")"));
	}

	@Test
	public void updateAttributeRefEntityXrefDifferentIdAttrType()
	{
		AttributeMetaData refIdAttr0 = when(mock(AttributeMetaData.class).getName()).thenReturn("refIdAttr0").getMock();
		when(refIdAttr0.getDataType()).thenReturn(INT);
		EntityMetaData refEntityMeta0 = when(mock(EntityMetaData.class).getName()).thenReturn("refEntity0").getMock();
		when(refEntityMeta0.getIdAttribute()).thenReturn(refIdAttr0);

		AttributeMetaData refIdAttr1 = when(mock(AttributeMetaData.class).getName()).thenReturn("refIdAttr1").getMock();
		when(refIdAttr1.getDataType()).thenReturn(STRING);
		EntityMetaData refEntityMeta1 = when(mock(EntityMetaData.class).getName()).thenReturn("refEntity1").getMock();
		when(refEntityMeta1.getIdAttribute()).thenReturn(refIdAttr1);

		EntityMetaData entityMeta = when(mock(EntityMetaData.class).getName()).thenReturn("entity").getMock();
		String attrName = "attr";

		AttributeMetaData attr = when(mock(AttributeMetaData.class).getName()).thenReturn(attrName).getMock();
		when(entityMeta.getAttribute(attrName)).thenReturn(attr);
		when(attr.getDataType()).thenReturn(XREF);
		when(attr.getRefEntity()).thenReturn(refEntityMeta0);

		AttributeMetaData updatedAttr = when(mock(AttributeMetaData.class).getName()).thenReturn(attrName).getMock();
		when(updatedAttr.getDataType()).thenReturn(XREF);
		when(updatedAttr.getRefEntity()).thenReturn(refEntityMeta1);

		postgreSqlRepoCollection.updateAttribute(entityMeta, attr, updatedAttr);
		ArgumentCaptor<String> captor = forClass(String.class);
		verify(jdbcTemplate, times(3)).execute(captor.capture());
		assertEquals(captor.getAllValues(), newArrayList("ALTER TABLE \"entity\" DROP CONSTRAINT \"entity_attr_fkey\"",
				"ALTER TABLE \"entity\" ALTER COLUMN \"attr\" SET DATA TYPE character varying(255) USING \"attr\"::character varying(255)",
				"ALTER TABLE \"entity\" ADD CONSTRAINT \"entity_attr_fkey\" FOREIGN KEY (\"attr\") REFERENCES \"refEntity1\"(\"refIdAttr1\")"));
	}

	@Test(expectedExceptions = MolgenisDataException.class, expectedExceptionsMessageRegExp = "Updating entity \\[entity\\] attribute \\[attr\\] referenced entity from \\[refEntity0\\] to \\[refEntity1\\] not allowed for type \\[MREF\\]")
	public void updateAttributeRefEntityMref()
	{
		AttributeMetaData refIdAttr0 = when(mock(AttributeMetaData.class).getName()).thenReturn("refIdAttr0").getMock();
		when(refIdAttr0.getDataType()).thenReturn(STRING);
		EntityMetaData refEntityMeta0 = when(mock(EntityMetaData.class).getName()).thenReturn("refEntity0").getMock();
		when(refEntityMeta0.getIdAttribute()).thenReturn(refIdAttr0);

		AttributeMetaData refIdAttr1 = when(mock(AttributeMetaData.class).getName()).thenReturn("refIdAttr1").getMock();
		when(refIdAttr1.getDataType()).thenReturn(STRING);
		EntityMetaData refEntityMeta1 = when(mock(EntityMetaData.class).getName()).thenReturn("refEntity1").getMock();
		when(refEntityMeta1.getIdAttribute()).thenReturn(refIdAttr1);

		AttributeMetaData idAttr = when(mock(AttributeMetaData.class).getName()).thenReturn("id").getMock();
		when(idAttr.getDataType()).thenReturn(STRING);
		EntityMetaData entityMeta = when(mock(EntityMetaData.class).getName()).thenReturn("entity").getMock();
		when(entityMeta.getIdAttribute()).thenReturn(idAttr);

		String attrName = "attr";
		AttributeMetaData attr = when(mock(AttributeMetaData.class).getName()).thenReturn(attrName).getMock();
		when(entityMeta.getAttribute(attrName)).thenReturn(attr);
		when(attr.getDataType()).thenReturn(MREF);
		when(attr.getRefEntity()).thenReturn(refEntityMeta0);

		AttributeMetaData updatedAttr = when(mock(AttributeMetaData.class).getName()).thenReturn(attrName).getMock();
		when(updatedAttr.getDataType()).thenReturn(MREF);
		when(updatedAttr.getRefEntity()).thenReturn(refEntityMeta1);

		postgreSqlRepoCollection.updateAttribute(entityMeta, attr, updatedAttr);
	}

	@Test
	public void addAttribute()
	{
		EntityMetaData entityMeta = when(mock(EntityMetaData.class).getName()).thenReturn("entity").getMock();
		AttributeMetaData idAttr = when(mock(AttributeMetaData.class).getName()).thenReturn("id").getMock();
		when(entityMeta.getIdAttribute()).thenReturn(idAttr);
		AttributeMetaData attr = when(mock(AttributeMetaData.class).getName()).thenReturn("attr").getMock();
		when(attr.getDataType()).thenReturn(STRING);
		postgreSqlRepoCollection.addAttribute(entityMeta, attr);
		verify(jdbcTemplate).execute("ALTER TABLE \"entity\" ADD \"attr\" character varying(255) NOT NULL");
	}

	@Test
	public void addAttributeUnique()
	{
		EntityMetaData entityMeta = when(mock(EntityMetaData.class).getName()).thenReturn("entity").getMock();
		AttributeMetaData idAttr = when(mock(AttributeMetaData.class).getName()).thenReturn("id").getMock();
		when(entityMeta.getIdAttribute()).thenReturn(idAttr);
		AttributeMetaData attr = when(mock(AttributeMetaData.class).getName()).thenReturn("attr").getMock();
		when(attr.getDataType()).thenReturn(STRING);
		when(attr.isUnique()).thenReturn(true);
		postgreSqlRepoCollection.addAttribute(entityMeta, attr);
		verify(jdbcTemplate).execute(
				"ALTER TABLE \"entity\" ADD \"attr\" character varying(255) NOT NULL,ADD CONSTRAINT \"entity_attr_key\" UNIQUE (\"attr\")");
	}

	@Test
	public void addAttributeAbstractEntity()
	{
		AttributeMetaData idAttr = when(mock(AttributeMetaData.class).getName()).thenReturn("id").getMock();
		EntityMetaData abstractEntityMeta = when(mock(EntityMetaData.class).getName()).thenReturn("root").getMock();
		when(abstractEntityMeta.isAbstract()).thenReturn(true);
		EntityMetaData entityMeta0 = when(mock(EntityMetaData.class).getName()).thenReturn("entity0").getMock();
		when(entityMeta0.getExtends()).thenReturn(abstractEntityMeta);
		when(entityMeta0.isAbstract()).thenReturn(true);
		EntityMetaData entityMeta0a = when(mock(EntityMetaData.class).getName()).thenReturn("entity0a").getMock();
		when(entityMeta0a.getExtends()).thenReturn(entityMeta0);
		when(entityMeta0a.isAbstract()).thenReturn(false);
		when(entityMeta0a.getIdAttribute()).thenReturn(idAttr);
		EntityMetaData entityMeta0b = when(mock(EntityMetaData.class).getName()).thenReturn("entity0b").getMock();
		when(entityMeta0b.getExtends()).thenReturn(entityMeta0);
		when(entityMeta0b.isAbstract()).thenReturn(false);
		when(entityMeta0b.getIdAttribute()).thenReturn(idAttr);
		EntityMetaData entityMeta1 = when(mock(EntityMetaData.class).getName()).thenReturn("entity1").getMock();
		when(entityMeta1.getExtends()).thenReturn(abstractEntityMeta);
		when(entityMeta1.isAbstract()).thenReturn(false);
		when(entityMeta1.getIdAttribute()).thenReturn(idAttr);

		//noinspection unchecked
		Query<EntityMetaData> entityQ = mock(Query.class);
		when(dataService.query(ENTITY_META_DATA, EntityMetaData.class)).thenReturn(entityQ);
		//noinspection unchecked
		Query<EntityMetaData> entityQ0 = mock(Query.class);
		//noinspection unchecked
		Query<EntityMetaData> entityQ1 = mock(Query.class);
		when(entityQ.eq(EXTENDS, abstractEntityMeta)).thenReturn(entityQ0);
		when(entityQ.eq(EXTENDS, entityMeta0)).thenReturn(entityQ1);
		when(entityQ0.findAll()).thenReturn(Stream.of(entityMeta0, entityMeta1));
		when(entityQ1.findAll()).thenReturn(Stream.of(entityMeta0a, entityMeta0b));

		AttributeMetaData attr = when(mock(AttributeMetaData.class).getName()).thenReturn("attr").getMock();
		when(attr.getDataType()).thenReturn(STRING);

		postgreSqlRepoCollection.addAttribute(abstractEntityMeta, attr);
		ArgumentCaptor<String> captor = forClass(String.class);
		verify(jdbcTemplate, times(3)).execute(captor.capture());
		assertEquals(captor.getAllValues(),
				newArrayList("ALTER TABLE \"entity0a\" ADD \"attr\" character varying(255) NOT NULL",
						"ALTER TABLE \"entity0b\" ADD \"attr\" character varying(255) NOT NULL",
						"ALTER TABLE \"entity1\" ADD \"attr\" character varying(255) NOT NULL"));
	}

	@Test
	public void addAttributeCompound()
	{
		EntityMetaData entityMeta = when(mock(EntityMetaData.class).getName()).thenReturn("entity").getMock();
		AttributeMetaData idAttr = when(mock(AttributeMetaData.class).getName()).thenReturn("id").getMock();
		when(entityMeta.getIdAttribute()).thenReturn(idAttr);
		AttributeMetaData attr = when(mock(AttributeMetaData.class).getName()).thenReturn("attr").getMock();
		when(attr.getDataType()).thenReturn(COMPOUND);
		postgreSqlRepoCollection.addAttribute(entityMeta, attr);
		verifyZeroInteractions(jdbcTemplate);
	}

	@Test
	public void addAttributeWithExpression()
	{
		EntityMetaData entityMeta = when(mock(EntityMetaData.class).getName()).thenReturn("entity").getMock();
		AttributeMetaData idAttr = when(mock(AttributeMetaData.class).getName()).thenReturn("id").getMock();
		when(entityMeta.getIdAttribute()).thenReturn(idAttr);
		AttributeMetaData attr = when(mock(AttributeMetaData.class).getName()).thenReturn("attr").getMock();
		when(attr.getExpression()).thenReturn("expression");
		when(attr.getDataType()).thenReturn(STRING);
		postgreSqlRepoCollection.addAttribute(entityMeta, attr);
		verifyZeroInteractions(jdbcTemplate);
	}

	@Test
	public void addAttributeOneToManyMappedBy()
	{
		EntityMetaData entityMeta = when(mock(EntityMetaData.class).getName()).thenReturn("entity").getMock();
		when(entityMeta.getBackend()).thenReturn(POSTGRESQL);
		EntityMetaData refEntityMeta = when(mock(EntityMetaData.class).getName()).thenReturn("refEntity").getMock();
		when(refEntityMeta.getBackend()).thenReturn(POSTGRESQL);
		AttributeMetaData idAttr = when(mock(AttributeMetaData.class).getName()).thenReturn("id").getMock();
		AttributeMetaData attr = when(mock(AttributeMetaData.class).getName()).thenReturn("attr").getMock();
		AttributeMetaData refAttr = when(mock(AttributeMetaData.class).getName()).thenReturn("refAttr").getMock();

		when(refAttr.getDataType()).thenReturn(XREF);
		when(refAttr.isInversedBy()).thenReturn(true);
		when(refAttr.getInversedBy()).thenReturn(attr);

		when(attr.getDataType()).thenReturn(ONE_TO_MANY);
		when(attr.getRefEntity()).thenReturn(refEntityMeta);
		when(attr.getMappedBy()).thenReturn(refAttr);
		when(attr.isMappedBy()).thenReturn(true);

		when(entityMeta.getIdAttribute()).thenReturn(idAttr);

		postgreSqlRepoCollection.addAttribute(entityMeta, attr);
		verify(jdbcTemplate).execute("ALTER TABLE \"refEntity\" ADD \"refAttr_order\" SERIAL");
	}

	@Test(expectedExceptions = MolgenisDataException.class)
	public void addAttributeAlreadyExists()
	{
		EntityMetaData entityMeta = when(mock(EntityMetaData.class).getName()).thenReturn("entity").getMock();
		String attrName = "attr";
		AttributeMetaData attr = when(mock(AttributeMetaData.class).getName()).thenReturn(attrName).getMock();
		when(entityMeta.getAttribute(attrName)).thenReturn(attr);
		postgreSqlRepoCollection.addAttribute(entityMeta, attr);
	}

	@Test
	public void deleteAttribute()
	{
		String attrName = "attr";
		AttributeMetaData attr = when(mock(AttributeMetaData.class).getName()).thenReturn(attrName).getMock();
		when(attr.getDataType()).thenReturn(STRING);
		EntityMetaData entityMeta = when(mock(EntityMetaData.class).getName()).thenReturn("entity").getMock();
		when(entityMeta.getAttribute(attrName)).thenReturn(attr);
		postgreSqlRepoCollection.deleteAttribute(entityMeta, attr);
		verify(jdbcTemplate).execute("ALTER TABLE \"entity\" DROP COLUMN \"attr\"");
	}

	@Test
	public void deleteAttributeMref()
	{
		EntityMetaData entityMeta = when(mock(EntityMetaData.class).getName()).thenReturn("entity").getMock();
		EntityMetaData refEntityMeta = when(mock(EntityMetaData.class).getName()).thenReturn("refEntity").getMock();

		String attrName = "attr";
		AttributeMetaData attr = when(mock(AttributeMetaData.class).getName()).thenReturn(attrName).getMock();

		when(attr.getDataType()).thenReturn(MREF);
		when(attr.getRefEntity()).thenReturn(refEntityMeta);

		when(entityMeta.getAttribute(attrName)).thenReturn(attr);
		postgreSqlRepoCollection.deleteAttribute(entityMeta, attr);
		verify(jdbcTemplate).execute("DROP TABLE \"entity_attr\"");
	}

	@Test
	public void deleteAttributeOneToManyMappedBy()
	{
		EntityMetaData entityMeta = when(mock(EntityMetaData.class).getName()).thenReturn("entity").getMock();
		EntityMetaData refEntityMeta = when(mock(EntityMetaData.class).getName()).thenReturn("refEntity").getMock();

		String attrName = "attr";
		AttributeMetaData attr = when(mock(AttributeMetaData.class).getName()).thenReturn(attrName).getMock();
		String refAttrName = "refAttr";
		AttributeMetaData refAttr = when(mock(AttributeMetaData.class).getName()).thenReturn(refAttrName).getMock();

		when(attr.getDataType()).thenReturn(ONE_TO_MANY);
		when(attr.getRefEntity()).thenReturn(refEntityMeta);
		when(attr.getMappedBy()).thenReturn(refAttr);
		when(attr.isMappedBy()).thenReturn(true);

		when(refAttr.getDataType()).thenReturn(XREF);
		when(refAttr.getRefEntity()).thenReturn(entityMeta);
		when(refAttr.getInversedBy()).thenReturn(attr);
		when(refAttr.isInversedBy()).thenReturn(true);

		when(entityMeta.getAttribute(attrName)).thenReturn(attr);
		postgreSqlRepoCollection.deleteAttribute(entityMeta, attr);
		verify(jdbcTemplate).execute("ALTER TABLE \"refEntity\" DROP COLUMN \"refAttr_order\"");
	}

	@Test
	public void deleteAttributeAbstractEntity()
	{
		EntityMetaData abstractEntityMeta = when(mock(EntityMetaData.class).getName()).thenReturn("root").getMock();
		when(abstractEntityMeta.isAbstract()).thenReturn(true);
		String attrName = "attr";
		AttributeMetaData attr = when(mock(AttributeMetaData.class).getName()).thenReturn(attrName).getMock();
		when(attr.getDataType()).thenReturn(STRING);
		when(abstractEntityMeta.getAttribute(attrName)).thenReturn(attr);
		when(attr.isNillable()).thenReturn(true);
		AttributeMetaData updatedAttr = when(mock(AttributeMetaData.class).getName()).thenReturn(attrName).getMock();
		when(updatedAttr.isNillable()).thenReturn(false);
		EntityMetaData entityMeta0 = when(mock(EntityMetaData.class).getName()).thenReturn("entity0").getMock();
		when(entityMeta0.getExtends()).thenReturn(abstractEntityMeta);
		when(entityMeta0.isAbstract()).thenReturn(true);
		EntityMetaData entityMeta0a = when(mock(EntityMetaData.class).getName()).thenReturn("entity0a").getMock();
		when(entityMeta0a.getExtends()).thenReturn(entityMeta0);
		when(entityMeta0a.isAbstract()).thenReturn(false);
		EntityMetaData entityMeta0b = when(mock(EntityMetaData.class).getName()).thenReturn("entity0b").getMock();
		when(entityMeta0b.getExtends()).thenReturn(entityMeta0);
		when(entityMeta0b.isAbstract()).thenReturn(false);
		EntityMetaData entityMeta1 = when(mock(EntityMetaData.class).getName()).thenReturn("entity1").getMock();
		when(entityMeta1.getExtends()).thenReturn(abstractEntityMeta);
		when(entityMeta1.isAbstract()).thenReturn(false);

		//noinspection unchecked
		Query<EntityMetaData> entityQ = mock(Query.class);
		when(dataService.query(ENTITY_META_DATA, EntityMetaData.class)).thenReturn(entityQ);
		//noinspection unchecked
		Query<EntityMetaData> entityQ0 = mock(Query.class);
		//noinspection unchecked
		Query<EntityMetaData> entityQ1 = mock(Query.class);
		when(entityQ.eq(EXTENDS, abstractEntityMeta)).thenReturn(entityQ0);
		when(entityQ.eq(EXTENDS, entityMeta0)).thenReturn(entityQ1);
		when(entityQ0.findAll()).thenReturn(Stream.of(entityMeta0, entityMeta1));
		when(entityQ1.findAll()).thenReturn(Stream.of(entityMeta0a, entityMeta0b));

		postgreSqlRepoCollection.deleteAttribute(abstractEntityMeta, attr);

		ArgumentCaptor<String> captor = forClass(String.class);
		verify(jdbcTemplate, times(3)).execute(captor.capture());
		assertEquals(captor.getAllValues(), newArrayList("ALTER TABLE \"entity0a\" DROP COLUMN \"attr\"",
				"ALTER TABLE \"entity0b\" DROP COLUMN \"attr\"", "ALTER TABLE \"entity1\" DROP COLUMN \"attr\""));
	}

	@Test
	public void deleteAttributeWithExpression()
	{
		String attrName = "attr";
		AttributeMetaData attr = when(mock(AttributeMetaData.class).getName()).thenReturn(attrName).getMock();
		when(attr.getExpression()).thenReturn("expression");
		EntityMetaData entityMeta = when(mock(EntityMetaData.class).getName()).thenReturn("entity").getMock();
		when(entityMeta.getAttribute(attrName)).thenReturn(attr);
		postgreSqlRepoCollection.deleteAttribute(entityMeta, attr);
		verifyZeroInteractions(jdbcTemplate);
	}

	@Test(expectedExceptions = UnknownAttributeException.class)
	public void deleteAttributeUnknownAttribute()
	{
		AttributeMetaData attr = when(mock(AttributeMetaData.class).getName()).thenReturn("attr").getMock();
		EntityMetaData entityMeta = when(mock(EntityMetaData.class).getName()).thenReturn("entity").getMock();
		postgreSqlRepoCollection.deleteAttribute(entityMeta, attr);
	}
}