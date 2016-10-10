package org.molgenis.data.idcard;

import com.google.common.collect.Maps;
import org.molgenis.data.*;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.AbstractRepositoryCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Sets.immutableEnumSet;
import static java.util.Objects.requireNonNull;
import static org.molgenis.data.RepositoryCollectionCapability.WRITABLE;
import static org.molgenis.data.idcard.model.IdCardBiobankMetaData.ID_CARD_BIOBANK;

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
	public Repository<Entity> createRepository(EntityType entityType)
	{
		String entityName = entityType.getName();
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
	public Repository<Entity> getRepository(EntityType entityType)
	{
		return getRepository(entityType.getName());
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
	public void deleteRepository(EntityType entityType)
	{
		repositories.remove(entityType.getName());
	}

	@Override
	public boolean hasRepository(EntityType entityType)
	{
		return hasRepository(entityType.getName());
	}
}
