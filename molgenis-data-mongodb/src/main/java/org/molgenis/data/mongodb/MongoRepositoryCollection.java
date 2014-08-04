package org.molgenis.data.mongodb;

import static org.molgenis.MolgenisFieldTypes.BOOL;
import static org.molgenis.MolgenisFieldTypes.INT;
import static org.molgenis.MolgenisFieldTypes.TEXT;
import static org.molgenis.MolgenisFieldTypes.XREF;

import java.util.Map;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mongodb.DB;

public class MongoRepositoryCollection implements RepositoryCollection
{
	private final Map<String, MongoRepository> repositories = Maps.newLinkedHashMap();
	private MongoRepository entitiesRepo;
	private MongoRepository attributesRepo;
	private final DB database;
	private final DataService dataService;

	protected MongoRepositoryCollection(DB database)
	{
		this(database, null);
	}

	public MongoRepositoryCollection(DB database, DataService dataService)
	{
		this.database = database;
		this.dataService = dataService;
		createRepositories();
	}

	public DB getMongoDB()
	{
		return database;
	}

	@Override
	public Iterable<String> getEntityNames()
	{
		return Lists.newArrayList(repositories.keySet());
	}

	@Override
	public MongoRepository getRepositoryByEntityName(String name)
	{
		return repositories.get(name);
	}

	public MongoRepository add(EntityMetaData emd)
	{
		Entity e = new MapEntity();
		e.set("name", emd.getName());
		e.set("description", emd.getDescription());
		e.set("abstract", emd.isAbstract());
		if (emd.getIdAttribute() != null) e.set("idAttribute", emd.getIdAttribute().getName());
		e.set("label", emd.getLabel());
		if (emd.getExtends() != null) e.set("extends", emd.getExtends().getName());
		entitiesRepo.add(e);

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
			a.set("visible", att.isVisible());
			a.set("label", att.getLabel());
			a.set("description", att.getDescription());

			if (att.getRefEntity() != null) a.set("refEntity", att.getRefEntity().getName());
			boolean lookupAttribute = att.isLookupAttribute();
			if (att.isIdAtrribute() || att.isLabelAttribute())
			{
				lookupAttribute = true;
			}
			a.set("lookupAttribute", lookupAttribute);

			attributesRepo.add(a);
		}

		// if not abstract add to repositories
		if (!emd.isAbstract())
		{
			MongoRepository repository = createRepository(emd);
			repositories.put(emd.getName(), repository);

			if (dataService != null)
			{
				dataService.addRepository(repository);
			}

			return repository;
		}

		return null;
	}

	private MongoRepository createRepository(EntityMetaData emd)
	{
		return new MongoRepositorySecurityDecorator(new MongoRepositoryImpl(emd, this));
	}

	private void createRepositories()
	{
		DefaultEntityMetaData entityMD = new DefaultEntityMetaData("entities");
		entityMD.setIdAttribute("name");
		entityMD.addAttribute("name").setNillable(false);
		entityMD.addAttribute("idAttribute");
		entityMD.addAttribute("abstract").setDataType(BOOL);
		entityMD.addAttribute("label");
		entityMD.addAttribute("extends");
		entityMD.addAttribute("description").setDataType(TEXT);
		entitiesRepo = new MongoRepositoryImpl(entityMD, this);

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
		attributesRepo = new MongoRepositoryImpl(entityMD, this);

		for (Entity emd : entitiesRepo)
		{
			// TODO
		}
	}

}
