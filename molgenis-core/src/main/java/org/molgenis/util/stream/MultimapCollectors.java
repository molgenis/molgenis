package org.molgenis.util.stream;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * Collectors that collect to {@link Multimap}.
 *
 * @see {@link Collectors#toMap(Function, Function)}, the equivalent of which is not (yet?) present in guava for the
 * {@link Multimap}.
 */
public class MultimapCollectors
{
	public static <K, V, E> Collector<E, ArrayListMultimap<K, V>, ArrayListMultimap<K, V>> toArrayListMultimap(
			Function<E, K> keyGenerator, Function<E, V> valueGenerator)
	{
		return toMultimap(ArrayListMultimap::<K, V>create, keyGenerator, valueGenerator);
	}

	public static <K, V, A extends Multimap<K, V>, E> Collector<E, A, A> toMultimap(Supplier<A> supplier,
			Function<E, K> keyGenerator, Function<E, V> valueGenerator)
	{
		return Collector.of(supplier, (map, entry) -> map.put(keyGenerator.apply(entry), valueGenerator.apply(entry)),
				(map1, map2) ->
				{
					map1.putAll(map2);
					return map1;
				});
	}

	public static <K, V, A extends Multimap<K, V>> Collector<Entry<K, V>, A, A> toMultimap(Supplier<A> supplier)
	{
		return toMultimap(supplier, Entry::getKey, Entry::getValue);
	}

	public static <K, V> Collector<Entry<K, V>, LinkedHashMultimap<K, V>, LinkedHashMultimap<K, V>> toLinkedHashMultimap()
	{
		return toMultimap(LinkedHashMultimap::<K, V>create);
	}
}