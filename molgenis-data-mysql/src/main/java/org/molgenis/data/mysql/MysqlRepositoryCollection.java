package org.molgenis.data.mysql;

import static org.molgenis.MolgenisFieldTypes.BOOL;
import static org.molgenis.MolgenisFieldTypes.INT;
import static org.molgenis.MolgenisFieldTypes.TEXT;
import static org.molgenis.MolgenisFieldTypes.XREF;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.fieldtypes.CompoundField;

public abstract class MysqlRepositoryCollection implements RepositoryCollection
{
	private final DataSource ds;
	private final DataService dataService;
	private Map<String, MysqlRepository> repositories;
	private MysqlRepository entities;
	private MysqlRepository attributes;

	public MysqlRepositoryCollection(DataSource ds, DataService dataService)
	{
		this.ds = ds;
		this.dataService = dataService;

		refreshRepositories();
	}

	public DataSource getDataSource()
	{
		return ds;
	}

	/**
	 * Return a spring managed prototype bean
	 */
	protected abstract MysqlRepository createMysqlRepsitory();

	private void refreshRepositories()
	{
		repositories = new LinkedHashMap<String, MysqlRepository>();

		DefaultEntityMetaData entityMD = new DefaultEntityMetaData("entities");
		entityMD.setIdAttribute("name");
		entityMD.addAttribute("name").setNillable(false);
		entityMD.addAttribute("idAttribute");
		entityMD.addAttribute("abstract").setDataType(BOOL);
		entityMD.addAttribute("label");
		entityMD.addAttribute("extends");// TODO create XREF to entityMD when dependency resolving is fixed
		entityMD.addAttribute("description").setDataType(TEXT);

		entities = createMysqlRepsitory();
		entities.setMetaData(entityMD);

		DefaultEntityMetaData attributeMD = new DefaultEntityMetaData("attributes");
		attributeMD.setIdAttribute("identifier");
		attributeMD.addAttribute("identifier").setNillable(false).setDataType(INT).setAuto(true);
		attributeMD.addAttribute("entity").setNillable(false);
		attributeMD.addAttribute("name").setNillable(false);
		attributeMD.addAttribute("dataType");
		attributeMD.addAttribute("refEntity").setDataType(XREF).setRefEntity(entityMD);
		attributeMD.addAttribute("nillable").setDataType(BOOL);
		attributeMD.addAttribute("auto").setDataType(BOOL);
		attributeMD.addAttribute("lookupAttribute").setDataType(BOOL);
		attributeMD.addAttribute("visible").setDataType(BOOL);
		attributeMD.addAttribute("label");
		attributeMD.addAttribute("description").setDataType(TEXT);

		attributes = createMysqlRepsitory();
		attributes.setMetaData(attributeMD);

		if (!tableExists("entities"))
		{
			entities.create();

			if (!tableExists("attributes"))
			{
				attributes.create();
			}
		}
		else if (attributes.count() == 0)
		{
			// Update table structure to prevent errors is apps that don't use emx
			attributes.drop();
			entities.drop();
			entities.create();
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
			am.setLookupAttribute(a.getBoolean("lookupAttribute"));
			am.setVisible(a.getBoolean("visible"));
			am.setLabel(a.getString("label"));
			am.setDescription(a.getString("description"));

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
			md.setAbstract(e.getBoolean("abstract"));
			md.setIdAttribute(e.getString("idAttribute"));
			md.setLabel(e.getString("label"));
			md.setDescription(e.getString("description"));
		}

		// read extends
		for (Entity e : entities)
		{
			String extendsEntityName = e.getString("extends");
			if (extendsEntityName != null)
			{
				String entityName = e.getString("name");
				DefaultEntityMetaData emd = metadata.get(entityName);
				DefaultEntityMetaData extendsEmd = metadata.get(extendsEntityName);
				if (extendsEmd == null) throw new RuntimeException("Missing super entity [" + extendsEntityName
						+ "] of entity [" + entityName + "]");
				emd.setExtends(extendsEmd);
			}
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
			if (!emd.isAbstract())
			{
				MysqlRepository repo = createMysqlRepsitory();
				repo.setMetaData(emd);
				repositories.put(emd.getName(), repo);
			}
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
		if (entities.query().eq("name", emd.getName()).count() > 0)
		{
			return repositories.get(emd.getName());
		}

		Entity e = new MapEntity();
		e.set("name", emd.getName());
		e.set("description", emd.getDescription());
		e.set("abstract", emd.isAbstract());
		if (emd.getIdAttribute() != null) e.set("idAttribute", emd.getIdAttribute().getName());
		e.set("label", emd.getLabel());
		if (emd.getExtends() != null) e.set("extends", emd.getExtends().getName());
		entities.add(e);

		// add attribute metadata
		for (AttributeMetaData att : emd.getAttributes())
		{
			Entity a = new MapEntity();
			a.set("entity", emd.getName());
			a.set("name", att.getName());
			a.set("defaultValue", att.getDefaultValue());
			a.set("dataType", att.getDataType());

			boolean lookupAttribute = att.isLookupAttribute();
			if (att.isIdAtrribute() || att.isLabelAttribute())
			{
				lookupAttribute = true;
			}
			a.set("lookupAttribute", lookupAttribute);

			if (att.getRefEntity() != null) a.set("refEntity", att.getRefEntity().getName());

			// add compound entities unless already there
			if (att.getDataType() instanceof CompoundField
					&& entities.count(new QueryImpl().eq("name", att.getRefEntity().getName())) == 0)
			{
				add(att.getRefEntity());
			}
			a.set("nillable", att.isNillable());
			a.set("auto", att.isAuto());
			a.set("visible", att.isVisible());
			a.set("label", att.getLabel());
			a.set("description", att.getDescription());

			attributes.add(a);
		}

		// if not abstract add to repositories
		if (!emd.isAbstract())
		{
			MysqlRepository repository = createMysqlRepsitory();
			repository.setMetaData(emd);
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
		drop(md.getName());
	}

	public void drop(String name)
	{
		// remove the repo
		MysqlRepository r = repositories.get(name);
		if (r != null)
		{
			r.drop();
			repositories.remove(name);
			dataService.removeRepository(r.getName());
		}

		// delete metadata
		attributes.delete(attributes.findAll(new QueryImpl().eq("entity", name)));
		entities.delete(entities.findAll(new QueryImpl().eq("name", name)));
	}
}
