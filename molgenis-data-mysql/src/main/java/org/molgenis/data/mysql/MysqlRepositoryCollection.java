package org.molgenis.data.mysql;

import static org.molgenis.MolgenisFieldTypes.BOOL;
import static org.molgenis.MolgenisFieldTypes.INT;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.*;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.QueryImpl;

/**
 * Created by mswertz on 07/04/14.
 */
public class MysqlRepositoryCollection implements RepositoryCollection
{
	DataSource ds;
	Map<String, MysqlRepository> repositories = new HashMap<String, MysqlRepository>();
	MysqlRepository entities;
	MysqlRepository attributes;

	public MysqlRepositoryCollection(DataSource ds)
	{
		this.ds = ds;

		DefaultEntityMetaData entityMD = new DefaultEntityMetaData("entities");
		entityMD.addAttribute("name").setNillable(false);
		entityMD.addAttribute("idAttribute");

		DefaultEntityMetaData attributeMD = new DefaultEntityMetaData("attributes");
		attributeMD.addAttribute("identifier").setNillable(false).setDataType(INT).setAuto(true);
		attributeMD.addAttribute("entity").setNillable(false);
		attributeMD.addAttribute("name").setNillable(false);
		attributeMD.addAttribute("dataType");
		attributeMD.addAttribute("nillable").setDataType(BOOL);
		attributeMD.addAttribute("auto").setDataType(BOOL);

		entities = new MysqlRepository(ds, entityMD);
		entities.create();
		attributes = new MysqlRepository(ds, attributeMD);
		attributes.create();

		// create repositories
		for (Entity e : entities)
		{
			DefaultEntityMetaData md = new DefaultEntityMetaData(e.getString("name"));
			for (Entity a : attributes.findAll(new QueryImpl().eq("entity", e.getString("name"))))
			{
				md.addAttribute(a.getString("name")).setDataType(MolgenisFieldTypes.getType(a.getString("dataType")))
						.setNillable(a.getBoolean("nillable")).setAuto(a.getBoolean("auto"));
			}

			this.repositories.put(md.getName(), new MysqlRepository(ds,md));
		}
	}

	public void add(EntityMetaData md)
	{
		MysqlRepository repository = new MysqlRepository(ds, md);
		repository.create();
		this.add(repository);
	}

	public void add(MysqlRepository repository)
	{
		if (entities.count(new QueryImpl().eq("name", repository.getName())) > 0) throw new RuntimeException("repository '"+repository.getName()+"' already exists");
        //TODO: check if this repository is equal to existing one!

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
