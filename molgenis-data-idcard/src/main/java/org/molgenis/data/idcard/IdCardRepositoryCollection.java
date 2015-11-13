package org.molgenis.data.idcard;

import static java.util.Objects.requireNonNull;

import java.util.Iterator;
import java.util.Map;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.ManageableRepositoryCollection;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Repository;
import org.molgenis.data.UnknownAttributeException;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.idcard.model.IdCardBiobank;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Maps;

@Component
public class IdCardRepositoryCollection implements ManageableRepositoryCollection
{
	public static final String NAME = "ID-Card";

	private final DataService dataService;
	private final IdCardBiobankRepository idCardBiobankRepository;
	private final Map<String, Repository> repositories;

	@Autowired
	public IdCardRepositoryCollection(DataService dataService, IdCardBiobankRepository idCardBiobankRepository)
	{
		this.dataService = requireNonNull(dataService);
		this.idCardBiobankRepository = requireNonNull(idCardBiobankRepository);
		this.repositories = Maps.newLinkedHashMap();
	}

	@Override
	public String getName()
	{
		return NAME;
	}

	@Override
	public Repository addEntityMeta(EntityMetaData entityMeta)
	{
		String entityName = entityMeta.getName();
		if (!entityName.equals(IdCardBiobank.ENTITY_NAME))
		{
			throw new MolgenisDataException("Not a valid backend for entity [" + entityName + "]");
		}
		else if (repositories.containsKey(IdCardBiobank.ENTITY_NAME))
		{
			throw new MolgenisDataException(
					"ID-Card repository collection already contains repository [" + entityName + "]");
		}
		else
		{
			repositories.put(IdCardBiobank.ENTITY_NAME, idCardBiobankRepository);
		}
		return idCardBiobankRepository;
	}

	@Override
	public Iterable<String> getEntityNames()
	{
		return repositories.keySet();
	}

	@Override
	public Repository getRepository(String name)
	{
		return repositories.get(name);
	}

	@Override
	public boolean hasRepository(String name)
	{
		return repositories.containsKey(name);
	}

	@Override
	public Iterator<Repository> iterator()
	{
		return repositories.values().iterator();
	}

	@Override
	public void deleteEntityMeta(String entityName)
	{
		repositories.remove(entityName);
	}

	@Override
	public void addAttribute(String entityName, AttributeMetaData attribute)
	{
		DefaultEntityMetaData entityMetaData;
		try
		{
			entityMetaData = (DefaultEntityMetaData) dataService.getEntityMetaData(entityName);
		}
		catch (ClassCastException ex)
		{
			throw new RuntimeException("Cannot cast EntityMetaData to DefaultEntityMetadata " + ex);
		}
		if (entityMetaData == null) throw new UnknownEntityException(String.format("Unknown entity '%s'", entityName));

		entityMetaData.addAttributeMetaData(attribute);
	}

	@Override
	public void deleteAttribute(String entityName, String attributeName)
	{
		EntityMetaData entityMetaData = dataService.getMeta().getEntityMetaData(entityName);
		if (entityMetaData == null) throw new UnknownEntityException(String.format("Unknown entity '%s'", entityName));

		DefaultEntityMetaData defaultEntityMetaData = new DefaultEntityMetaData(
				dataService.getMeta().getEntityMetaData(entityName));
		AttributeMetaData attr = entityMetaData.getAttribute(attributeName);
		if (attr == null) throw new UnknownAttributeException(
				String.format("Unknown attribute '%s' of entity '%s'", attributeName, entityName));

		defaultEntityMetaData.removeAttributeMetaData(attr);
	}

	@Override
	public void addAttributeSync(String entityName, AttributeMetaData attribute)
	{
		addAttribute(entityName, attribute);
	}
}
