package org.molgenis.data.mysql;

import static org.molgenis.MolgenisFieldTypes.*;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.*;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.fieldtypes.CompoundField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("MysqlRepositoryCollection")
public class MysqlRepositoryCollection implements RepositoryCollection
{
	DataSource ds;

    @Autowired
    DataService dataService;

	Map<String, MysqlRepository> repositories;
	MysqlRepository entities;
	MysqlRepository attributes;

	public MysqlRepositoryCollection()
	{
	}

	public MysqlRepositoryCollection(DataSource ds)
	{
		this.setDataSource(ds);
	}

	public DataSource getDataSource()
	{
		return ds;
	}

	@Autowired
	public void setDataSource(DataSource ds)
	{
		this.ds = ds;
		System.out.println("MysqlRepositoryCollection initatied with ds=" + ds);
		refreshRepositories();
	}

	private void refreshRepositories()
	{
		repositories = new LinkedHashMap<String, MysqlRepository>();

		DefaultEntityMetaData entityMD = new DefaultEntityMetaData("entities").setIdAttribute("name");
		entityMD.addAttribute("name").setNillable(false);
		entityMD.addAttribute("idAttribute");
		entities = new MysqlRepository(this, entityMD);
		if (!this.tableExists("entities"))
		{
			entities.create();
		}

		DefaultEntityMetaData attributeMD = new DefaultEntityMetaData("attributes").setIdAttribute("identifier");
		attributeMD.addAttribute("identifier").setNillable(false).setDataType(INT).setAuto(true);
		attributeMD.addAttribute("entity").setNillable(false);
		attributeMD.addAttribute("name").setNillable(false);
		attributeMD.addAttribute("dataType");
		attributeMD.addAttribute("refEntity").setDataType(XREF).setRefEntity(entityMD);
		attributeMD.addAttribute("nillable").setDataType(BOOL);
		attributeMD.addAttribute("auto").setDataType(BOOL);

		attributes = new MysqlRepository(this, attributeMD);
		if (!this.tableExists("attributes"))
		{
			attributes.create();
		}

		Map<String, DefaultEntityMetaData> metadata = new LinkedHashMap<String, DefaultEntityMetaData>();

		// read the attributes
		for (Entity a : attributes)
		{
			if (metadata.get(a.getString("entity")) == null)
			{
				metadata.put(a.getString("entity"), new DefaultEntityMetaData(a.getString("entity")));
			}
			DefaultEntityMetaData md = metadata.get(a.getString("entity"));

			DefaultAttributeMetaData am = new DefaultAttributeMetaData(a.getString("name"));
			am.setDataType(MolgenisFieldTypes.getType(a.getString("dataType")));
			am.setNillable(a.getBoolean("nillable"));
			am.setAuto(a.getBoolean("auto"));
			md.addAttributeMetaData(am);
		}

		// read the entities
		for (Entity e : entities)
		{
			if (metadata.get(e.getString("name")) == null)
			{
				metadata.put(e.getString("name"), new DefaultEntityMetaData(e.getString("name")));
			}
			DefaultEntityMetaData md = metadata.get(e.getString("name"));
			md.setIdAttribute(e.getString("idAttribute"));
		}

		// read the refEntity
		for (Entity a : attributes)
		{
			if (a.getString("refEntity") != null)
			{
				EntityMetaData emd = metadata.get(a.getString("entity"));
				DefaultAttributeMetaData amd = (DefaultAttributeMetaData) emd.getAttribute(a.getString("name"));
				EntityMetaData ref = metadata.get(a.getString("refEntity"));
				if (ref == null) throw new RuntimeException("refEntity '" + a.getString("refEntity") + "' missing for "
						+ emd.getName() + "." + amd.getName());
				amd.setRefEntity(ref);
			}
		}

		// instantiate the repos
		for (EntityMetaData emd : metadata.values())
		{
			System.out.println(emd);
			this.repositories.put(emd.getName(), new MysqlRepository(this, emd));
		}
	}

	private boolean tableExists(String table)
	{
		Connection conn = null;
		try
		{

			conn = ds.getConnection();
			DatabaseMetaData dbm = conn.getMetaData();
			ResultSet tables = dbm.getTables(null, null, table, null);
			if (tables.next())
			{
				return true;
			}
			else
			{
				return false;
			}
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
		finally
		{
			try
			{
				conn.close();
			}
			catch (Exception e2)
			{
				e2.printStackTrace();
			}
		}
	}

	public MysqlRepository add(EntityMetaData emd)
	{
		this.refreshRepositories();
		if (this.getRepositoryByEntityName(emd.getName()) != null) throw new RuntimeException(
				"MysqlRepositorCollection.add() failed: table '" + emd.getName() + "' exists");

		this.refreshRepositories();
		if (this.getRepositoryByEntityName(emd.getName()) != null) throw new RuntimeException(
				"MysqlRepositorCollection.add() failed: table '" + emd.getName() + "' exists");
		// TODO: check if this repository is equal to existing one!

		Entity e = new MapEntity();
		e.set("name", emd.getName());
		if (emd.getIdAttribute() != null) e.set("idAttribute", emd.getIdAttribute().getName());
		entities.add(e);

		// add attribute metadata
		for (AttributeMetaData att : emd.getAttributes())
		{
			Entity a = new MapEntity();
			a.set("entity", emd.getName());
			a.set("name", att.getName());
			a.set("defaultValue", att.getDefaultValue());
			a.set("dataType", att.getDataType());
			if (att.getRefEntity() != null) a.set("refEntity", att.getRefEntity().getName());
			// add compound entities unless already there
			if (att.getDataType() instanceof CompoundField
					&& entities.count(new QueryImpl().eq("name", att.getRefEntity().getName())) == 0)
			{
				this.add(att.getRefEntity());
			}
			a.set("nillable", att.isNillable());
			a.set("auto", att.isAuto());
			attributes.add(a);
		}

		// if not abstract add to repositories
		if (!emd.isAbstract())
		{
			MysqlRepository repository = new MysqlRepository(this, emd);
			repository.create();
			repositories.put(emd.getName(), repository);
            dataService.addRepository(repository);
			return repository;
		}
		return null;
	}

	@Override
	public Iterable<String> getEntityNames()
	{
		return repositories.keySet();
	}

	@Override
	public Repository getRepositoryByEntityName(String name)
	{
		return repositories.get(name);
	}

	public void drop(EntityMetaData md)
	{
		assert md != null;
		this.drop(md.getName());
	}

	public void drop(String name)
	{
		// remove the repo
		MysqlRepository r = this.repositories.get(name);
		if (r != null) {
            r.drop();
            this.repositories.remove(name);
            dataService.removeRepository(r.getName());
        }

		// delete metadata
		attributes.delete(attributes.findAll(new QueryImpl().eq("entity", name)));
		entities.delete(entities.findAll(new QueryImpl().eq("name", name)));
	}
}
