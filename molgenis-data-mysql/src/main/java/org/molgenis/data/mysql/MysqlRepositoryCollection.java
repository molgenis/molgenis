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
import org.molgenis.data.MolgenisDataException;
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

	public void refreshRepositories()
	{
		repositories = new LinkedHashMap<String, MysqlRepository>();

		DefaultEntityMetaData entitiesMetaData = new DefaultEntityMetaData("entities");
		entitiesMetaData.setIdAttribute("name");
		entitiesMetaData.addAttribute("name").setNillable(false);
		entitiesMetaData.addAttribute("idAttribute");
		entitiesMetaData.addAttribute("abstract").setDataType(BOOL);
		entitiesMetaData.addAttribute("label");
		entitiesMetaData.addAttribute("extends");// TODO create XREF to entityMD when dependency resolving is fixed
		entitiesMetaData.addAttribute("description").setDataType(TEXT);

		entities = createMysqlRepsitory();
		entities.setMetaData(entitiesMetaData);

		DefaultEntityMetaData attributesMetaData = new DefaultEntityMetaData("attributes");
		attributesMetaData.setIdAttribute("identifier");
		attributesMetaData.addAttribute("identifier").setNillable(false).setDataType(INT).setAuto(true);
		attributesMetaData.addAttribute("entity").setNillable(false);
		attributesMetaData.addAttribute("name").setNillable(false);
		attributesMetaData.addAttribute("dataType");
		attributesMetaData.addAttribute("refEntity").setDataType(XREF).setRefEntity(entitiesMetaData);
		attributesMetaData.addAttribute("nillable").setDataType(BOOL);
		attributesMetaData.addAttribute("auto").setDataType(BOOL);
		attributesMetaData.addAttribute("idAttribute").setDataType(BOOL);
		attributesMetaData.addAttribute("lookupAttribute").setDataType(BOOL);
		attributesMetaData.addAttribute("visible").setDataType(BOOL);
		attributesMetaData.addAttribute("label");
		attributesMetaData.addAttribute("description").setDataType(TEXT);

		attributes = createMysqlRepsitory();
		attributes.setMetaData(attributesMetaData);

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
		for (Entity attribute : attributes)
		{
			DefaultEntityMetaData entityMetaData = metadata.get(attribute.getString("entity"));
			if (entityMetaData == null)
			{
				entityMetaData = new DefaultEntityMetaData(attribute.getString("entity"));
				metadata.put(attribute.getString("entity"), entityMetaData);
			}

			DefaultAttributeMetaData attributeMetaData = new DefaultAttributeMetaData(attribute.getString("name"));
			attributeMetaData.setDataType(MolgenisFieldTypes.getType(attribute.getString("dataType")));
			attributeMetaData.setNillable(attribute.getBoolean("nillable"));
			attributeMetaData.setAuto(attribute.getBoolean("auto"));
			attributeMetaData.setIdAttribute(attribute.getBoolean("idAttribute"));
			attributeMetaData.setLookupAttribute(attribute.getBoolean("lookupAttribute"));
			attributeMetaData.setVisible(attribute.getBoolean("visible"));
			attributeMetaData.setLabel(attribute.getString("label"));
			attributeMetaData.setDescription(attribute.getString("description"));

			entityMetaData.addAttributeMetaData(attributeMetaData);
		}

		// read the entities
		for (Entity entity : entities)
		{
			DefaultEntityMetaData entityMetaData = metadata.get(entity.getString("name"));
			if (entityMetaData == null)
			{
				entityMetaData = new DefaultEntityMetaData(entity.getString("name"));
				metadata.put(entity.getString("name"), entityMetaData);
			}

			entityMetaData.setAbstract(entity.getBoolean("abstract"));
			entityMetaData.setIdAttribute(entity.getString("idAttribute"));
			entityMetaData.setLabel(entity.getString("label"));
			entityMetaData.setDescription(entity.getString("description"));
		}

		// read extends
		for (Entity entity : entities)
		{
			String extendsEntityName = entity.getString("extends");
			if (extendsEntityName != null)
			{
				String entityName = entity.getString("name");
				DefaultEntityMetaData emd = metadata.get(entityName);
				DefaultEntityMetaData extendsEmd = metadata.get(extendsEntityName);
				if (extendsEmd == null) throw new RuntimeException("Missing super entity [" + extendsEntityName
						+ "] of entity [" + entityName + "]");
				emd.setExtends(extendsEmd);
			}
		}

		// read the refEntity
		for (Entity attribute : attributes)
		{
			if (attribute.getString("refEntity") != null)
			{
				EntityMetaData entityMetaData = metadata.get(attribute.getString("entity"));
				DefaultAttributeMetaData attributeMetaData = (DefaultAttributeMetaData) entityMetaData
						.getAttribute(attribute
						.getString("name"));
				EntityMetaData ref = metadata.get(attribute.getString("refEntity"));
				if (ref == null) throw new RuntimeException("refEntity '" + attribute.getString("refEntity")
						+ "' missing for " + entityMetaData.getName() + "." + attributeMetaData.getName());
				attributeMetaData.setRefEntity(ref);
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
			addAttribute(emd, att);
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

	public void addAttribute(EntityMetaData emd, AttributeMetaData att)
	{
		Entity a = new MapEntity();
		a.set("entity", emd.getName());
		a.set("name", att.getName());
		a.set("defaultValue", att.getDefaultValue());
		a.set("dataType", att.getDataType());
		a.set("idAttribute", att.isIdAtrribute());

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

	public void update(EntityMetaData metadata)
	{
		MysqlRepository repository = repositories.get(metadata.getName());
		EntityMetaData entityMetaData = repository.getEntityMetaData();
		for (AttributeMetaData attr : metadata.getAttributes())
		{
			AttributeMetaData currentAttribute = entityMetaData.getAttribute(attr.getName());
			if (currentAttribute != null)
			{
				if (!currentAttribute.getDataType().equals(attr.getDataType()))
				{
					throw new MolgenisDataException("Changing type for existing attributes is not currently supported");
				}
			}
			else if (!attr.isNillable())
			{
				throw new MolgenisDataException("Adding non-nillable attributes is not currently supported");
			}
			else
			{
				addAttribute(metadata, attr);
				DefaultEntityMetaData metaData = (DefaultEntityMetaData) repository.getEntityMetaData();
				metaData.addAttributeMetaData(attr);
				repository.addAttribute(attr);
			}
		}
	}
}
