package org.molgenis.data.postgresql;

import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.QueryRule;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.PlatformTransactionManager;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.sql.DataSource;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
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

		//noinspection unchecked
		Query<Entity> query = mock(Query.class);
		int queryValue = 2;
		QueryRule queryRule = new QueryRule(oneToManyAttrName, EQUALS, queryValue);
		when(query.getRules()).thenReturn(singletonList(queryRule));

		String sql = "SELECT COUNT(DISTINCT this.\"entityId\") FROM \"Entity\" AS this LEFT JOIN \"RefEntity\" AS \"oneToManyAttr_filter1\" ON (this.\"entityId\" = \"oneToManyAttr_filter1\".\"xrefAttr\") WHERE \"oneToManyAttr_filter1\".\"refEntityId\" = ?";
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

		//noinspection unchecked
		Query<Entity> query = mock(Query.class);
		int queryValue = 2;
		QueryRule queryRule = new QueryRule(oneToManyAttrName, EQUALS, queryValue);
		when(query.getRules()).thenReturn(singletonList(queryRule));

		String sql = "SELECT DISTINCT this.\"entityId\", (SELECT array_agg(\"refEntityId\" ORDER BY \"refEntityId\" ASC) FROM \"RefEntity\" WHERE this.\"entityId\" = \"RefEntity\".\"xrefAttr\") AS \"oneToManyAttr\" FROM \"Entity\" AS this LEFT JOIN \"RefEntity\" AS \"oneToManyAttr_filter1\" ON (this.\"entityId\" = \"oneToManyAttr_filter1\".\"xrefAttr\") WHERE \"oneToManyAttr_filter1\".\"refEntityId\" = ?  LIMIT 1000";
		//noinspection unchecked
		RowMapper<Entity> rowMapper = mock(RowMapper.class);
		when(postgreSqlEntityFactory.createRowMapper(entityType, null)).thenReturn(rowMapper);
		Entity entity0 = mock(Entity.class);
		when(jdbcTemplate.query(sql, new Object[] { queryValue }, rowMapper)).thenReturn(singletonList(entity0));
		postgreSqlRepo.setEntityType(entityType);
		assertEquals(postgreSqlRepo.findAll(query).collect(toList()), singletonList(entity0));
	}

	private static EntityType createEntityMetaOneToMany(String oneToManyAttrName)
	{
		String refIdAttrName = "refEntityId";
		Attribute refIdAttr = mock(Attribute.class);
		when(refIdAttr.getName()).thenReturn(refIdAttrName);
		when(refIdAttr.getDataType()).thenReturn(INT);

		String xrefAttrName = "xrefAttr";
		Attribute xrefAttr = mock(Attribute.class);
		when(xrefAttr.getName()).thenReturn(xrefAttrName);
		when(xrefAttr.getDataType()).thenReturn(XREF);

		EntityType refEntityMeta = mock(EntityType.class);
		String refEntityName = "RefEntity";
		when(refEntityMeta.getName()).thenReturn(refEntityName);
		when(refEntityMeta.getIdAttribute()).thenReturn(refIdAttr);
		when(refEntityMeta.getAttribute(refIdAttrName)).thenReturn(refIdAttr);
		when(refEntityMeta.getAttribute(xrefAttrName)).thenReturn(xrefAttr);

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

		EntityType entityType = mock(EntityType.class);
		String entityName = "Entity";
		when(entityType.getName()).thenReturn(entityName);
		when(entityType.getIdAttribute()).thenReturn(idAttr);
		when(entityType.getAttribute(idAttrName)).thenReturn(idAttr);
		when(entityType.getAttribute(oneToManyAttrName)).thenReturn(oneToManyAttr);
		when(entityType.getAtomicAttributes()).thenReturn(newArrayList(idAttr, oneToManyAttr));
		return entityType;
	}
}