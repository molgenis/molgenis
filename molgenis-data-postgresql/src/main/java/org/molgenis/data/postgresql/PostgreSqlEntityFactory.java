package org.molgenis.data.postgresql;

import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityManager;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Fetch;
import org.molgenis.data.support.DefaultEntity;
import org.molgenis.fieldtypes.MrefField;
import org.molgenis.fieldtypes.XrefField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

@Component
public class PostgreSqlEntityFactory
{
	@SuppressWarnings("unused")
	private static final Logger LOG = LoggerFactory.getLogger(PostgreSqlEntityFactory.class);

	private final EntityManager entityManager;
	private final DataService dataService;

	@Autowired
	public PostgreSqlEntityFactory(EntityManager entityManager, DataService dataService)
	{
		this.entityManager = requireNonNull(entityManager);
		this.dataService = requireNonNull(dataService);
	}

	public RowMapper<Entity> createRowMapper(EntityMetaData entityMeta, Fetch fetch)
	{
		return new EntityMapper(entityMeta, fetch);
	}

	private class EntityMapper implements RowMapper<Entity>
	{
		private final EntityMetaData entityMetaData;
		private final Fetch fetch;

		private EntityMapper(EntityMetaData entityMetaData, Fetch fetch)
		{
			this.entityMetaData = requireNonNull(entityMetaData);
			this.fetch = fetch; // can be null
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
						// ResultSet contains a two dimensional array for MREF attribute values:
						// [[<order_nr_as_string>,<mref_id_as_string>],[<order_nr_as_string>,<mref_id_as_string>], ...]
						// In case there are no MREF attribute values the ResulSet is:
						// [[null,null]]
						EntityMetaData refEntityMeta = att.getRefEntity();
						String[][] mrefIdsAndOrder = (String[][]) resultSet.getArray(att.getName()).getArray();
						if (mrefIdsAndOrder.length > 0 && mrefIdsAndOrder[0][0] != null)
						{
							Object[] mrefIds = new Object[mrefIdsAndOrder.length];
							for (String[] mrefIdAndOrder : mrefIdsAndOrder)
							{
								Integer seqNr = Integer.valueOf(mrefIdAndOrder[0]);
								Object mrefId = refEntityMeta.getIdAttribute().getDataType().convert(mrefIdAndOrder[1]);
								mrefIds[seqNr] = mrefId;
							}

							// convert ids to (lazy) entities
							e.set(att.getName(), entityManager.getReferences(refEntityMeta, asList(mrefIds)));
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
	}
}
