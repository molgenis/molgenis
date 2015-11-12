package org.molgenis.data.mysql;

import static java.util.Objects.requireNonNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataConverter;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityManager;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Fetch;
import org.molgenis.data.support.DefaultEntity;
import org.molgenis.fieldtypes.IntField;
import org.molgenis.fieldtypes.MrefField;
import org.molgenis.fieldtypes.XrefField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

@Component
public class MySqlEntityFactory
{
	private static final Logger LOG = LoggerFactory.getLogger(MySqlEntityFactory.class);

	private final EntityManager entityManager;
	private final DataService dataService;

	@Autowired
	public MySqlEntityFactory(EntityManager entityManager, DataService dataService)
	{
		this.entityManager = requireNonNull(entityManager);
		this.dataService = requireNonNull(dataService);
	}

	public RowMapper<Entity> createRowMapper(EntityMetaData entityMeta, Fetch fetch, JdbcTemplate jdbcTemplate,
			String tableName)
	{
		return new EntityMapper(entityMeta, fetch, jdbcTemplate, tableName);
	}

	private class EntityMapper implements RowMapper<Entity>
	{
		private static final int GROUP_CONCAT_MAX_LEN = 1024;
		private final EntityMetaData entityMetaData;
		private final Fetch fetch;
		private final JdbcTemplate jdbcTemplate;
		private final String tableName;

		private EntityMapper(EntityMetaData entityMetaData, Fetch fetch, JdbcTemplate jdbcTemplate, String tableName)
		{
			this.entityMetaData = requireNonNull(entityMetaData);
			this.fetch = fetch; // can be null
			this.jdbcTemplate = requireNonNull(jdbcTemplate);
			this.tableName = requireNonNull(tableName);
		}

		@Override
		public Entity mapRow(ResultSet resultSet, int i) throws SQLException
		{
			Entity e = new DefaultEntity(entityMetaData, dataService);

			// TODO performance, iterate over fetch if available
			for (AttributeMetaData att : entityMetaData.getAtomicAttributes())
			{
				if (fetch == null || fetch.hasField(att.getName()))
				{
					if (att.getExpression() != null)
					{
						continue;
					}

					if (att.getDataType() instanceof MrefField)
					{
						EntityMetaData refEntityMeta = att.getRefEntity();
						String mrefIds = resultSet.getString(att.getName());
						if (mrefIds != null)
						{
							Iterable<Entity> mrefEntities;
							if (refEntityMeta.getIdAttribute().getDataType() instanceof IntField)
							{
								List<Integer> mrefIntegerIds;
								if (mrefIds.length() >= GROUP_CONCAT_MAX_LEN)
								{
									// this list is just as long as it's allowed to be so it probably got truncated.
									// Retrieve the IDs explicitly in a separate query.
									String mrefSelectSql = getMrefSelectSql(e, att);
									if (LOG.isDebugEnabled())
									{
										LOG.debug("Fetching MySQL [{}] data for SQL [{}]", refEntityMeta.getName(),
												mrefSelectSql);
									}
									mrefIntegerIds = jdbcTemplate.queryForList(mrefSelectSql, Integer.class);
								}
								else
								{
									mrefIntegerIds = DataConverter.toIntList(mrefIds);
								}

								// convert ids to (lazy) entities
								mrefEntities = entityManager.getReferences(refEntityMeta, mrefIntegerIds);

							}
							else
							{
								List<Object> mrefObjectIds;
								if (mrefIds.length() >= GROUP_CONCAT_MAX_LEN)
								{
									// this list is just as long as it's allowed to be so it probably got truncated.
									// Retrieve the IDs explicitly in a separate query.
									String mrefSelectSql = getMrefSelectSql(e, att);
									if (LOG.isDebugEnabled())
									{
										LOG.debug("Fetching MySQL [{}] data for SQL [{}]", refEntityMeta.getName(),
												mrefSelectSql);
									}
									mrefObjectIds = jdbcTemplate.queryForList(mrefSelectSql, Object.class);
								}
								else
								{
									mrefObjectIds = DataConverter.toObjectList(mrefIds);
								}

								// convert ids to (lazy) entities
								mrefEntities = entityManager.getReferences(refEntityMeta, mrefObjectIds);
							}
							e.set(att.getName(), mrefEntities);
						}
					}
					else if (att.getDataType() instanceof XrefField)
					{
						EntityMetaData refEntityMeta = att.getRefEntity();
						Object xrefId = refEntityMeta.getIdAttribute().getDataType()
								.convert(resultSet.getObject(att.getName()));

						Entity xrefEntity = xrefId != null ? entityManager.getReference(refEntityMeta, xrefId) : null;
						e.set(att.getName(), xrefEntity);
					}
					else
					{
						e.set(att.getName(), att.getDataType().convert(resultSet.getObject(att.getName())));
					}
				}
			}

			if (fetch != null)
			{
				return entityManager.createEntityForPartialEntity(e, fetch);
			}
			else
			{
				return e;
			}
		}

		private String getMrefSelectSql(Entity e, AttributeMetaData att)
		{
			return String.format("SELECT `%s` FROM `%s_%1$s` WHERE `%s` = '%s' ORDER BY `order`", att.getName(),
					tableName, entityMetaData.getIdAttribute().getName().toLowerCase(),
					e.get(entityMetaData.getIdAttribute().getName()));
		}
	}
}
