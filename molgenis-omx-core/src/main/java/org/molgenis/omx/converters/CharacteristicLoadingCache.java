package org.molgenis.omx.converters;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.Query;
import org.molgenis.omx.observ.Characteristic;

import com.google.common.base.Function;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

class CharacteristicLoadingCache
{
	private static final long MAX_CACHE_ITEMS = 10000;

	private final Database database;
	private final LoadingCache<String, Integer> characteristicLoadingCache;

	public CharacteristicLoadingCache(Database database)
	{
		if (database == null) throw new IllegalArgumentException("Database is null");
		this.database = database;
		this.characteristicLoadingCache = CacheBuilder.newBuilder().maximumSize(MAX_CACHE_ITEMS).softValues()
				.build(new CacheLoader<String, Integer>()
				{
					@Override
					public Integer load(String identifier) throws Exception
					{
						return findCharacteristicId(identifier);
					}

					@Override
					public Map<String, Integer> loadAll(Iterable<? extends String> identifiers) throws Exception
					{
						return findCharacteristicIds(identifiers);
					}
				});
	}

	public Characteristic findCharacteristic(String identifier) throws DatabaseException
	{
		Integer primaryKey;
		try
		{
			primaryKey = this.characteristicLoadingCache.get(identifier);
		}
		catch (ExecutionException e)
		{
			throw new DatabaseException(e);
		}
		return database.getEntityManager().getReference(Characteristic.class, primaryKey);
	}

	public List<Characteristic> findCharacteristics(List<String> identifiers) throws DatabaseException
	{
		final ImmutableMap<String, Integer> characteristicIdMap;
		try
		{
			characteristicIdMap = this.characteristicLoadingCache.getAll(identifiers);
		}
		catch (ExecutionException e)
		{
			throw new DatabaseException(e);
		}

		return Lists.transform(identifiers, new Function<String, Characteristic>()
		{
			@Override
			public Characteristic apply(String identifier)
			{
				Integer primaryKey = characteristicIdMap.get(identifier);
				return database.getEntityManager().getReference(Characteristic.class, primaryKey);
			}
		});
	}

	private Integer findCharacteristicId(String identifier) throws DatabaseException, ValueConverterException
	{
		Characteristic characteristic = Characteristic.findByIdentifier(database, identifier);
		if (characteristic == null)
		{
			throw new ValueConverterException("unknown characteristic identifier [" + identifier + ']');
		}
		return characteristic.getId();
	}

	private Map<String, Integer> findCharacteristicIds(Iterable<? extends String> identifiersIterable)
			throws DatabaseException, ValueConverterException
	{
		List<String> identifiers = Lists.newArrayList(identifiersIterable);
		Query<Characteristic> query = database.query(Characteristic.class).in(Characteristic.IDENTIFIER, identifiers);
		List<Characteristic> values = query.find();

		final int nrIdentifiers = identifiers.size();
		if (nrIdentifiers != values.size())
		{
			String identifiersStr = StringUtils.join(identifiers, ',');
			throw new ValueConverterException("one or more characteristics do not exist [" + identifiersStr + "]");
		}

		Map<String, Integer> characteristicMap = Maps.<String, Integer> newHashMapWithExpectedSize(nrIdentifiers);
		for (int i = 0; i < nrIdentifiers; ++i)
			characteristicMap.put(identifiers.get(i), values.get(i).getId());
		return characteristicMap;
	}
}
