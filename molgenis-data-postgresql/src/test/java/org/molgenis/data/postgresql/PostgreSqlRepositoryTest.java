package org.molgenis.data.postgresql;

import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.QueryRule;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.validation.MolgenisValidationException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.PlatformTransactionManager;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.sql.DataSource;
import java.util.stream.Stream;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.molgenis.data.QueryRule.Operator.EQUALS;
import static org.molgenis.data.meta.AttributeType.*;
import static org.testng.Assert.assertEquals;

public class PostgreSqlRepositoryTest
{
	private PostgreSqlRepository postgreSqlRepo;
	private JdbcTemplate jdbcTemplate;
	private PostgreSqlEntityFactory postgreSqlEntityFactory;

	@BeforeMethod
	public void setUpBeforeMethod() throws Exception
	{
		postgreSqlEntityFactory = mock(PostgreSqlEntityFactory.class);
		jdbcTemplate = mock(JdbcTemplate.class);
		DataSource dataSource = mock(DataSource.class);
		PlatformTransactionManager transactionManager = mock(PlatformTransactionManager.class);
		postgreSqlRepo = new PostgreSqlRepository(postgreSqlEntityFactory, jdbcTemplate, dataSource);
	}

	// TODO test all query operators for one-to-many
	@Test
	public void countQueryOneToManyEquals() throws Exception
	{
		String oneToManyAttrName = "oneToManyAttr";
		EntityType entityType = createEntityMetaOneToMany(oneToManyAttrName);

		@SuppressWarnings("unchecked")
		Query<Entity> query = mock(Query.class);
		int queryValue = 2;
		QueryRule queryRule = new QueryRule(oneToManyAttrName, EQUALS, queryValue);
		when(query.getRules()).thenReturn(singletonList(queryRule));

		String sql = "SELECT COUNT(DISTINCT this.\"entityId\") FROM \"entityId#fc2928f6\" AS this LEFT JOIN \"refEntityId#07f902bf\" AS \"oneToManyAttr_filter1\" ON (this.\"entityId\" = \"oneToManyAttr_filter1\".\"xrefAttr\") WHERE \"oneToManyAttr_filter1\".\"refEntityId\" = ?";
		long count = 123L;
		when(jdbcTemplate.queryForObject(sql, new Object[] { queryValue }, Long.class)).thenReturn(count);
		postgreSqlRepo.setEntityType(entityType);
		assertEquals(postgreSqlRepo.count(query), count);
	}

	@Test
	public void findAllQueryOneToManyEquals() throws Exception
	{
		String oneToManyAttrName = "oneToManyAttr";
		EntityType entityType = createEntityMetaOneToMany(oneToManyAttrName);

		@SuppressWarnings("unchecked")
		Query<Entity> query = mock(Query.class);
		int queryValue = 2;
		QueryRule queryRule = new QueryRule(oneToManyAttrName, EQUALS, queryValue);
		when(query.getRules()).thenReturn(singletonList(queryRule));

		String sql = "SELECT DISTINCT this.\"entityId\", (SELECT array_agg(\"refEntityId\" ORDER BY \"refEntityId\" ASC) FROM \"refEntityId#07f902bf\" WHERE this.\"entityId\" = \"refEntityId#07f902bf\".\"xrefAttr\") AS \"oneToManyAttr\" FROM \"entityId#fc2928f6\" AS this LEFT JOIN \"refEntityId#07f902bf\" AS \"oneToManyAttr_filter1\" ON (this.\"entityId\" = \"oneToManyAttr_filter1\".\"xrefAttr\") WHERE \"oneToManyAttr_filter1\".\"refEntityId\" = ?  LIMIT 1000";
		@SuppressWarnings("unchecked")
		RowMapper<Entity> rowMapper = mock(RowMapper.class);
		when(postgreSqlEntityFactory.createRowMapper(entityType, null)).thenReturn(rowMapper);
		Entity entity0 = mock(Entity.class);
		when(jdbcTemplate.query(sql, new Object[] { queryValue }, rowMapper)).thenReturn(singletonList(entity0));
		postgreSqlRepo.setEntityType(entityType);
		assertEquals(postgreSqlRepo.findAll(query).collect(toList()), singletonList(entity0));
	}

	@Test
	public void testUpdateEntitiesExist()
	{
		Attribute idAttr = mock(Attribute.class);
		when(idAttr.getName()).thenReturn("attr");
		when(idAttr.getDataType()).thenReturn(STRING);

		EntityType entityType = mock(EntityType.class);
		when(entityType.getIdAttribute()).thenReturn(idAttr);
		when(entityType.getAtomicAttributes()).thenReturn(singletonList(idAttr));
		when(entityType.getId()).thenReturn("entity");
		when(entityType.getIdValue()).thenReturn("entity");
		when(entityType.getAttribute("attr")).thenReturn(idAttr);

		Entity entity0 = mock(Entity.class);
		when(entity0.getIdValue()).thenReturn("id0");
		Entity entity1 = mock(Entity.class);
		when(entity1.getIdValue()).thenReturn("id1");

		when(jdbcTemplate.batchUpdate(any(String.class), any(BatchPreparedStatementSetter.class))).thenReturn(
				new int[] { 2 });
		postgreSqlRepo.setEntityType(entityType);

		postgreSqlRepo.update(Stream.of(entity0, entity1));
		verify(jdbcTemplate).batchUpdate(any(String.class), any(BatchPreparedStatementSetter.class));
		verifyNoMoreInteractions(jdbcTemplate);
	}

	@SuppressWarnings("unchecked")
	@Test(expectedExceptions = MolgenisValidationException.class, expectedExceptionsMessageRegExp = "Cannot update \\[entity\\] with id \\[id1\\] because it does not exist")
	public void testUpdateEntityDoesNotExist()
	{
		Attribute idAttr = mock(Attribute.class);
		when(idAttr.getName()).thenReturn("attr");
		when(idAttr.getDataType()).thenReturn(STRING);

		EntityType entityType = mock(EntityType.class);
		when(entityType.getIdAttribute()).thenReturn(idAttr);
		when(entityType.getAtomicAttributes()).thenReturn(singletonList(idAttr));
		when(entityType.getId()).thenReturn("entity");
		when(entityType.getIdValue()).thenReturn("entity");
		when(entityType.getAttribute("attr")).thenReturn(idAttr);

		Entity entity0 = mock(Entity.class);
		when(entity0.getIdValue()).thenReturn("id0");
		Entity entity1 = mock(Entity.class);
		when(entity1.getIdValue()).thenReturn("id1");

		when(jdbcTemplate.query(any(String.class), any(Object[].class), any(RowMapper.class))).thenReturn(
				singletonList(entity0));
		when(jdbcTemplate.batchUpdate(any(String.class), any(BatchPreparedStatementSetter.class))).thenReturn(
				new int[] { 1 });
		postgreSqlRepo.setEntityType(entityType);

		postgreSqlRepo.update(Stream.of(entity0, entity1));
	}

	private static EntityType createEntityMetaOneToMany(String oneToManyAttrName)
	{
		String refIdAttrName = "refEntityId";
		Attribute refIdAttr = mock(Attribute.class);
		when(refIdAttr.getIdentifier()).thenReturn(refIdAttrName);
		when(refIdAttr.getName()).thenReturn(refIdAttrName);
		when(refIdAttr.getDataType()).thenReturn(INT);

		String xrefAttrName = "xrefAttr";
		Attribute xrefAttr = mock(Attribute.class);
		when(xrefAttr.getIdentifier()).thenReturn(xrefAttrName);
		when(xrefAttr.getName()).thenReturn(xrefAttrName);
		when(xrefAttr.getDataType()).thenReturn(XREF);

		EntityType refEntityMeta = mock(EntityType.class);
		String refEntityName = "RefEntity";
		when(refEntityMeta.getId()).thenReturn("refEntityId");
		when(refEntityMeta.getIdValue()).thenReturn("refEntityId");
		when(refEntityMeta.getIdAttribute()).thenReturn(refIdAttr);
		when(refEntityMeta.getAttribute(refIdAttrName)).thenReturn(refIdAttr);
		when(refEntityMeta.getAttribute(xrefAttrName)).thenReturn(xrefAttr);

		String idAttrName = "entityId";
		Attribute idAttr = mock(Attribute.class);
		when(idAttr.getIdentifier()).thenReturn(idAttrName);
		when(idAttr.getName()).thenReturn(idAttrName);
		when(idAttr.getDataType()).thenReturn(STRING);

		Attribute oneToManyAttr = mock(Attribute.class);
		when(oneToManyAttr.getIdentifier()).thenReturn(oneToManyAttrName);
		when(oneToManyAttr.getName()).thenReturn(oneToManyAttrName);
		when(oneToManyAttr.getDataType()).thenReturn(ONE_TO_MANY);
		when(oneToManyAttr.getRefEntity()).thenReturn(refEntityMeta);
		when(oneToManyAttr.isMappedBy()).thenReturn(true);
		when(oneToManyAttr.getMappedBy()).thenReturn(xrefAttr);

		EntityType entityType = mock(EntityType.class);
		String entityTypeId = "Entity";
		when(entityType.getId()).thenReturn("entityId");
		when(entityType.getIdValue()).thenReturn("entityId");
		when(entityType.getIdAttribute()).thenReturn(idAttr);
		when(entityType.getAttribute(idAttrName)).thenReturn(idAttr);
		when(entityType.getAttribute(oneToManyAttrName)).thenReturn(oneToManyAttr);
		when(entityType.getAtomicAttributes()).thenReturn(newArrayList(idAttr, oneToManyAttr));
		return entityType;
	}
}