package org.molgenis.pathways.service;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.molgenis.pathways.model.Pathway;

import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Helper methods to create {@link LoadingCache}s.
 */
public class CacheFactory
{

	/**
	 * Creates a {@link LoadingCache} for a {@link FunctionalInterface} that may throw a {@link RemoteException}
	 *
	 * @param loader {@link RemoteFunction} that loads a value for the cache
	 * @return {@link LoadingCache}
	 */
	public static <K, V> LoadingCache<K, V> loadingCache(RemoteFunction<K, V> loader)
	{
		return CacheBuilder.newBuilder()
						   .maximumSize(Integer.MAX_VALUE)
						   .refreshAfterWrite(1, TimeUnit.DAYS)
						   .build(new CacheLoader<K, V>()
						   {
							   @Override
							   public V load(K key) throws Exception
							   {
								   return loader.apply(key);
							   }

						   });
	}

	/**
	 * Creates a {@link LoadingCache} for {@link Pathway}s
	 *
	 * @param loader             {@link RemoteFunction} that loads the pathways
	 * @param filter             {@link BiPredicate} to filter the loaded pathways
	 * @param pathwayTransformer {@link Function} that transforms the loaded pathways to {@link Pathway}s
	 * @return {@link List} containing the filtered and transformed {@link Pathway}s
	 */
	public static <Params, Result> LoadingCache<Params, Set<Pathway>> loadingPathwayCache(
			RemoteFunction<Params, Result[]> loader, BiPredicate<Params, Result> filter,
			Function<Result, Pathway> pathwayTransformer)
	{
		return loadingCache(params -> Arrays.stream(loader.apply(params))
											.filter((result) -> filter.test(params, result))
											.map(pathwayTransformer)
											.collect(Collectors.toCollection(LinkedHashSet::new)));
	}

}
