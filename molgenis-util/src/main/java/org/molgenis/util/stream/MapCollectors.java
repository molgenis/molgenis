package org.molgenis.util.stream;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static java.lang.String.format;

/**
 * Collectors that collect to {@link Map}.
 *
 * @see Collectors
 */
public class MapCollectors
{
	private MapCollectors()
	{
	}

	/**
	 * Based on <a href="https://stackoverflow.com/a/29090335/8579801">https://stackoverflow.com/a/29090335/8579801</a>
	 */
	@SuppressWarnings("squid:S1452")
	public static <T, K, U> Collector<T, ?, Map<K, U>> toLinkedMap(Function<? super T, ? extends K> keyMapper,
			Function<? super T, ? extends U> valueMapper)
	{
		return Collectors.toMap(keyMapper, valueMapper, throwingMerger(), LinkedHashMap::new);
	}

	private static <T> BinaryOperator<T> throwingMerger()
	{
		return (u, v) ->
		{
			throw new IllegalStateException(format("Duplicate key detected with values '%s' and '%s'", u, v));
		};
	}
}
