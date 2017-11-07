package org.molgenis.data.postgresql;

import org.mockito.Mock;
import org.mockito.MockitoSession;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.QueryRule;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.validation.MolgenisValidationException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.sql.DataSource;
import java.util.stream.Stream;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.mockito.Mockito.*;
import static org.mockito.quality.Strictness.STRICT_STUBS;
import static org.molgenis.data.QueryRule.Operator.EQUALS;
import static org.molgenis.data.meta.AttributeType.*;
import static org.testng.Assert.assertEquals;

public class PostgreSqlRepositoryTest
{
	private PostgreSqlRepository postgreSqlRepo;
	@Mock
	private JdbcTemplate jdbcTemplate;
	@Mock
	private PostgreSqlEntityFactory postgreSqlEntityFactory;
	@Mock
	private DataSource dataSource;
	@Mock
	private EntityType entityType;
	@Mock
	private Query<Entity> query;
	@Mock
	private RowMapper<Entity> rowMapper;

	private MockitoSession mockitoSession;

	@BeforeMethod
	public void setUpBeforeMethod() throws Exception
	{
		mockitoSession = mockitoSession().initMocks(this).strictness(STRICT_STUBS).startMocking();
		postgreSqlRepo = new PostgreSqlRepository(postgreSqlEntityFactory, jdbcTemplate, dataSource, entityType);
	}

	@AfterMethod
	public void afterMethod()
	{
		mockitoSession.finishMocking();
	}

	// TODO test all query operators for one-to-many
	@Test
	public void countQueryOneToManyEquals() throws Exception
	{
		String oneToManyAttrName = "oneToManyAttr";
		String refIdAttrName = "refEntityId";
		Attribute refIdAttr = mock(Attribute.class);
		when(refIdAttr.getName()).thenReturn(refIdAttrName);
		when(refIdAttr.getDataType()).thenReturn(INT);

		String xrefAttrName = "xrefAttr";
		Attribute xrefAttr = mock(Attribute.class);
		when(xrefAttr.getName()).thenReturn(xrefAttrName);

		EntityType refEntityMeta = mock(EntityType.class);
		when(refEntityMeta.getId()).thenReturn("refEntityId");
		when(refEntityMeta.getIdAttribute()).thenReturn(refIdAttr);

		String idAttrName = "entityId";
		Attribute idAttr = mock(Attribute.class);
		when(idAttr.getName()).thenReturn(idAttrName);

		Attribute oneToManyAttr = mock(Attribute.class);
		when(oneToManyAttr.getName()).thenReturn(oneToManyAttrName);
		when(oneToManyAttr.getDataType()).thenReturn(ONE_TO_MANY);
		when(oneToManyAttr.getRefEntity()).thenReturn(refEntityMeta);
		when(oneToManyAttr.isMappedBy()).thenReturn(true);
		when(oneToManyAttr.getMappedBy()).thenReturn(xrefAttr);

		when(entityType.getId()).thenReturn("entityId");
		when(entityType.getIdAttribute()).thenReturn(idAttr);
		doReturn(oneToManyAttr).when(entityType).getAttribute(oneToManyAttrName);

		int queryValue = 2;
		QueryRule queryRule = new QueryRule(oneToManyAttrName, EQUALS, queryValue);
		when(query.getRules()).thenReturn(singletonList(queryRule));

		String sql = "SELECT COUNT(DISTINCT this.\"entityId\") FROM \"entityId#fc2928f6\" AS this LEFT JOIN \"refEntityId#07f902bf\" AS \"oneToManyAttr_filter1\" ON (this.\"entityId\" = \"oneToManyAttr_filter1\".\"xrefAttr\") WHERE \"oneToManyAttr_filter1\".\"refEntityId\" = ?";
		long count = 123L;
		when(jdbcTemplate.queryForObject(sql, new Object[] { queryValue }, Long.class)).thenReturn(count);

		assertEquals(postgreSqlRepo.count(query), count);
	}

	@Test
	public void findAllQueryOneToManyEquals() throws Exception
	{
		String oneToManyAttrName = "oneToManyAttr";
		String refIdAttrName = "refEntityId";
		Attribute refIdAttr = mock(Attribute.class);
		when(refIdAttr.getName()).thenReturn(refIdAttrName);
		when(refIdAttr.getDataType()).thenReturn(INT);

		String xrefAttrName = "xrefAttr";
		Attribute xrefAttr = mock(Attribute.class);
		when(xrefAttr.getName()).thenReturn(xrefAttrName);

		EntityType refEntityMeta = mock(EntityType.class);
		when(refEntityMeta.getId()).thenReturn("refEntityId");
		when(refEntityMeta.getIdAttribute()).thenReturn(refIdAttr);
		doReturn(refIdAttr).when(refEntityMeta).getAttribute(refIdAttrName);

		String idAttrName = "entityId";
		Attribute idAttr = mock(Attribute.class);
		when(idAttr.getName()).thenReturn(idAttrName);
		when(idAttr.getDataType()).thenReturn(STRING);

		Attribute oneToManyAttr = mock(Attribute.class);
		when(oneToManyAttr.getName()).thenReturn(oneToManyAttrName);
		when(oneToManyAttr.getDataType()).thenReturn(ONE_TO_MANY);
		when(oneToManyAttr.getRefEntity()).thenReturn(refEntityMeta);
		when(oneToManyAttr.isMappedBy()).thenReturn(true);
		when(oneToManyAttr.getMappedBy()).thenReturn(xrefAttr);

		when(entityType.getId()).thenReturn("entityId");
		when(entityType.getIdAttribute()).thenReturn(idAttr);
		doReturn(oneToManyAttr).when(entityType).getAttribute(oneToManyAttrName);
		when(entityType.getAtomicAttributes()).thenReturn(newArrayList(idAttr, oneToManyAttr));
		EntityType entityType = this.entityType;
		postgreSqlRepo = new PostgreSqlRepository(postgreSqlEntityFactory, jdbcTemplate, dataSource, entityType);

		int queryValue = 2;
		QueryRule queryRule = new QueryRule(oneToManyAttrName, EQUALS, queryValue);
		when(query.getRules()).thenReturn(singletonList(queryRule));

		String sql = "SELECT DISTINCT this.\"entityId\", (SELECT array_agg(\"refEntityId\" ORDER BY \"refEntityId\" ASC) FROM \"refEntityId#07f902bf\" WHERE this.\"entityId\" = \"refEntityId#07f902bf\".\"xrefAttr\") AS \"oneToManyAttr\" FROM \"entityId#fc2928f6\" AS this LEFT JOIN \"refEntityId#07f902bf\" AS \"oneToManyAttr_filter1\" ON (this.\"entityId\" = \"oneToManyAttr_filter1\".\"xrefAttr\") WHERE \"oneToManyAttr_filter1\".\"refEntityId\" = ?  LIMIT 1000";

		when(postgreSqlEntityFactory.createRowMapper(entityType, null)).thenReturn(rowMapper);
		Entity entity0 = mock(Entity.class);
		when(jdbcTemplate.query(sql, new Object[] { queryValue }, rowMapper)).thenReturn(singletonList(entity0));
		assertEquals(postgreSqlRepo.findAll(query).collect(toList()), singletonList(entity0));
	}

	@Test
	public void testUpdateEntitiesExist()
	{
		Attribute idAttr = mock(Attribute.class);
		when(idAttr.getName()).thenReturn("attr");
		when(idAttr.getDataType()).thenReturn(STRING);

		when(entityType.getIdAttribute()).thenReturn(idAttr);
		when(entityType.getAtomicAttributes()).thenReturn(singletonList(idAttr));
		when(entityType.getId()).thenReturn("entity");

		Entity entity0 = mock(Entity.class);
		Entity entity1 = mock(Entity.class);

		when(jdbcTemplate.batchUpdate(any(String.class), any(BatchPreparedStatementSetter.class))).thenReturn(
				new int[] { 2 });

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

		when(entityType.getIdAttribute()).thenReturn(idAttr);
		when(entityType.getAtomicAttributes()).thenReturn(singletonList(idAttr));
		when(entityType.getId()).thenReturn("entity");
		when(entityType.getAttribute("attr")).thenReturn(idAttr);

		Entity entity0 = mock(Entity.class);
		when(entity0.getIdValue()).thenReturn("id0");
		Entity entity1 = mock(Entity.class);
		when(entity1.getIdValue()).thenReturn("id1");

		when(jdbcTemplate.query(any(String.class), any(Object[].class), (RowMapper) isNull())).thenReturn(
				singletonList(entity0));
		when(jdbcTemplate.batchUpdate(any(String.class), any(BatchPreparedStatementSetter.class))).thenReturn(
				new int[] { 1 });

		postgreSqlRepo.update(Stream.of(entity0, entity1));
	}

}