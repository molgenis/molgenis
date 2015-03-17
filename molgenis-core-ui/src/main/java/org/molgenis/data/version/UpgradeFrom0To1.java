package org.molgenis.data.version;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.IndexedRepository;
import org.molgenis.data.ManageableRepositoryCollection;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.meta.AttributeMetaDataMetaData;
import org.molgenis.data.meta.EntityMetaDataMetaData;
import org.molgenis.data.meta.MetaDataServiceImpl;
import org.molgenis.fieldtypes.MrefField;
import org.molgenis.fieldtypes.XrefField;
import org.springframework.jdbc.core.JdbcTemplate;

public class UpgradeFrom0To1 extends MetaDataUpgrade
{
	private final RepositoryCollection jpaBackend;
	private final JdbcTemplate jdbcTemplate;

	public UpgradeFrom0To1(DataService dataService, RepositoryCollection jpaBackend, DataSource dataSource)
	{
		super(0, 1, dataService);
		this.jpaBackend = jpaBackend;
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	@Override
	public void upgrade()
	{
		ManageableRepositoryCollection defaultBackend = dataService.getMeta().getDefaultBackend();
		MetaDataServiceImpl metaDataService = (MetaDataServiceImpl) dataService.getMeta();

		// Add expression attribute to AttributeMetaData
		defaultBackend.addAttribute(AttributeMetaDataMetaData.ENTITY_NAME,
				new AttributeMetaDataMetaData().getAttribute(AttributeMetaDataMetaData.EXPRESSION));

		// Add backend attribute to EntityMetaData
		defaultBackend.addAttribute(EntityMetaDataMetaData.ENTITY_NAME,
				new EntityMetaDataMetaData().getAttribute(EntityMetaDataMetaData.BACKEND));

		// All entities in the entities repo are MySQL backend
		for (EntityMetaData emd : dataService.getMeta().getEntityMetaDatas())
		{
			metaDataService.updateEntityMetaBackend(emd.getName(), "MySQL");
		}

		List<String> statements = new ArrayList<>();

		// We got no mrefs in JPA in the standard molgenis -> not supported in upgrate
		// JPA ids from int to string

		// Drop foreign keys
		for (Repository repo : jpaBackend)
		{
			EntityMetaData emd = repo.getEntityMetaData();
			for (AttributeMetaData attr : emd.getAtomicAttributes())
			{
				if (attr.getDataType() instanceof MrefField) throw new MolgenisDataException(
						"Mref not supported in upgrade");

				if (attr.getDataType() instanceof XrefField)
				{
					statements.add(String.format("ALTER TABLE %s DROP FOREIGN KEY FK_%s_%s", emd.getName(),
							emd.getName(), attr.getName()));
				}
			}
		}
		jdbcTemplate.batchUpdate(statements.toArray(new String[statements.size()]));
		statements.clear();

		// Update key from int to varchar
		for (Repository repo : jpaBackend)
		{
			EntityMetaData emd = repo.getEntityMetaData();
			for (AttributeMetaData attr : emd.getAtomicAttributes())
			{
				if (attr.isIdAtrribute() || (attr.getDataType() instanceof XrefField))
				{
					statements.add(String.format("ALTER TABLE %s MODIFY COLUMN %s VARCHAR(255)", emd.getName(),
							attr.getName()));
				}
			}
		}
		jdbcTemplate.batchUpdate(statements.toArray(new String[statements.size()]));

		// Reanable foreing keys
		statements.clear();
		for (Repository repo : jpaBackend)
		{
			EntityMetaData emd = repo.getEntityMetaData();
			for (AttributeMetaData attr : repo.getEntityMetaData().getAtomicAttributes())
			{
				if (attr.getDataType() instanceof XrefField)
				{
					statements.add(String.format("ALTER TABLE %s ADD FOREIGN KEY (FK_%s_%s) REFERENCES %s",
							emd.getName(), emd.getName(), attr.getName(), attr.getRefEntity().getName()));
				}
			}
		}

		for (Repository repo : jpaBackend)
		{
			((IndexedRepository) repo).rebuildIndex();
		}

	}
}
