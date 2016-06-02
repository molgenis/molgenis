package org.molgenis.data.idcard;

import static autovalue.shaded.com.google.common.common.collect.Sets.immutableEnumSet;
import static java.util.Objects.requireNonNull;
import static org.molgenis.data.RepositoryCollectionCapability.WRITABLE;
import static org.molgenis.data.idcard.model.IdCardBiobankMetaData.ID_CARD_BIOBANK;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCollectionCapability;
import org.molgenis.data.meta.EntityMetaData;
import org.molgenis.data.support.AbstractRepositoryCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Maps;

@Component
public class IdCardRepositoryCollection extends AbstractRepositoryCollection
{
	public static final String NAME = "ID-Card";

	private final DataService dataService;
	private final IdCardBiobankRepository idCardBiobankRepository;
	private final Map<String, Repository<Entity>> repositories;

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
	public Set<RepositoryCollectionCapability> getCapabilities()
	{
		return immutableEnumSet(EnumSet.of(WRITABLE));
	}

	@Override
	public Repository<Entity> createRepository(EntityMetaData entityMeta)
	{
		String entityName = entityMeta.getName();
		if (!entityName.equals(ID_CARD_BIOBANK))
		{
			throw new MolgenisDataException("Not a valid backend for entity [" + entityName + "]");
		}
		else if (repositories.containsKey(ID_CARD_BIOBANK))
		{
			throw new MolgenisDataException(
					"ID-Card repository collection already contains repository [" + entityName + "]");
		}
		else
		{
			repositories.put(ID_CARD_BIOBANK, idCardBiobankRepository);
		}
		return idCardBiobankRepository;
	}

	@Override
	public Iterable<String> getEntityNames()
	{
		return repositories.keySet();
	}

	@Override
	public Repository<Entity> getRepository(String name)
	{
		//return repositories.get(name);
		return idCardBiobankRepository; // FIXME
	}

	@Override
	public Repository<Entity> getRepository(EntityMetaData entityMetaData)
	{
		return getRepository(entityMetaData.getName());
	}

	@Override
	public boolean hasRepository(String name)
	{
		return repositories.containsKey(name);
	}

	@Override
	public Iterator<Repository<Entity>> iterator()
	{
		return repositories.values().iterator();
	}

	@Override
	public void deleteRepository(EntityMetaData entityMeta)
	{
		repositories.remove(entityMeta.getName());
	}

	@Override
	public boolean hasRepository(EntityMetaData entityMeta)
	{
		return hasRepository(entityMeta.getName());
	}
}
