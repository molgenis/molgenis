package org.molgenis.data.mysql;

import static org.molgenis.MolgenisFieldTypes.BOOL;
import static org.molgenis.MolgenisFieldTypes.INT;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.*;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.QueryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("MysqlRepositoryCollection")
public class MysqlRepositoryCollection implements RepositoryCollection
{
	DataSource ds;
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

	@Autowired
	public void setDataSource(DataSource ds)
	{
		this.ds = ds;
		System.out.println("MysqlRepositoryCollection initatied with ds=" + ds);
		refreshRepositories();
	}

	private void refreshRepositories()
	{
		repositories = new HashMap<String, MysqlRepository>();

		DefaultEntityMetaData entityMD = new DefaultEntityMetaData("entities").setIdAttribute("name");
		entityMD.addAttribute("name").setNillable(false);
		entityMD.addAttribute("idAttribute");
		entities = new MysqlRepository(ds, entityMD);
		if (!this.tableExists("entities"))
		{
			entities.create();
		}

		DefaultEntityMetaData attributeMD = new DefaultEntityMetaData("attributes").setIdAttribute("identifier");
		attributeMD.addAttribute("identifier").setNillable(false).setDataType(INT).setAuto(true);
		attributeMD.addAttribute("entity").setNillable(false);
		attributeMD.addAttribute("name").setNillable(false);
		attributeMD.addAttribute("dataType");
		attributeMD.addAttribute("nillable").setDataType(BOOL);
		attributeMD.addAttribute("auto").setDataType(BOOL);

		attributes = new MysqlRepository(ds, attributeMD);
		if (!this.tableExists("attributes"))
		{
			attributes.create();
		}

		for (Entity e : entities)
		{
			DefaultEntityMetaData md = new DefaultEntityMetaData(e.getString("name"));
			md.setIdAttribute(e.getString("idAttribute"));
			for (Entity a : attributes.findAll(new QueryImpl().eq("entity", e.getString("name"))))
			{
				md.addAttribute(a.getString("name")).setDataType(MolgenisFieldTypes.getType(a.getString("dataType")))
						.setNillable(a.getBoolean("nillable")).setAuto(a.getBoolean("auto"));
			}

			this.repositories.put(md.getName(), new MysqlRepository(ds, md));
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

	public void add(EntityMetaData md)
	{
		this.refreshRepositories();
		if (this.getRepositoryByEntityName(md.getName()) != null) throw new RuntimeException(
				"MysqlRepositorCollection.add() failed: table '" + md.getName() + "' exists");
		MysqlRepository repository = new MysqlRepository(ds, md);
		repository.create();
		this.add(repository);
	}

	public void add(MysqlRepository repository)
	{
		this.refreshRepositories();
		if (this.getRepositoryByEntityName(repository.getName()) != null) throw new RuntimeException(
				"MysqlRepositorCollection.add() failed: table '" + repository.getName() + "' exists");
		// TODO: check if this repository is equal to existing one!

		// add entity metadata
		EntityMetaData emd = repository.getEntityMetaData();
		Entity e = new MapEntity();
		e.set("name", emd.getName());
		e.set("idAttribute", emd.getIdAttribute().getName());
		entities.add(e);

		// add attribute metadata
		for (AttributeMetaData att : emd.getAttributes())
		{
			Entity a = new MapEntity();
			a.set("entity", emd.getName());
			a.set("name", att.getName());
			a.set("defaultValue", att.getDefaultValue());
			a.set("dataType", att.getDataType());
			a.set("nillable", att.isNillable());
			a.set("auto", att.isAuto());
			attributes.add(a);
		}

		// add tot repository
		repositories.put(repository.getName(), repository);
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
}
