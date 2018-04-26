package org.molgenis.data;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Optional;
import java.util.Spliterator;
import java.util.function.*;
import java.util.stream.*;

import static java.util.Objects.requireNonNull;

/**
 * Entity {@link java.util.stream.Stream Stream}
 */
public class EntityStream implements Stream<Entity>
{
	private final Stream<Entity> stream;
	private final boolean isLazy;

	public EntityStream(Stream<Entity> stream, boolean isLazy)
	{
		this.stream = requireNonNull(stream);
		this.isLazy = requireNonNull(isLazy);
	}

	/**
	 * Returns whether this entity collection is lazy, i.e. all entities are references to entities (= lazy entities)
	 *
	 * @return whether this entity collection is lazy
	 */
	public boolean isLazy()
	{
		return isLazy;
	}

	// delegate methods to decorated stream
	@Override
	public Iterator<Entity> iterator()
	{
		return stream.iterator();
	}

	@Override
	public Spliterator<Entity> spliterator()
	{
		return stream.spliterator();
	}

	@Override
	public boolean isParallel()
	{
		return stream.isParallel();
	}

	@Override
	public Stream<Entity> sequential()
	{
		return stream.sequential();
	}

	@Override
	public Stream<Entity> parallel()
	{
		return stream.parallel();
	}

	@Override
	public Stream<Entity> unordered()
	{
		return stream.unordered();
	}

	@Override
	public Stream<Entity> onClose(Runnable closeHandler)
	{
		return stream.onClose(closeHandler);
	}

	@Override
	public void close()
	{
		stream.close();
	}

	@Override
	public Stream<Entity> filter(Predicate<? super Entity> predicate)
	{
		return stream.filter(predicate);
	}

	@Override
	public <R> Stream<R> map(Function<? super Entity, ? extends R> mapper)
	{
		return stream.map(mapper);
	}

	@Override
	public IntStream mapToInt(ToIntFunction<? super Entity> mapper)
	{
		return stream.mapToInt(mapper);
	}

	@Override
	public LongStream mapToLong(ToLongFunction<? super Entity> mapper)
	{
		return stream.mapToLong(mapper);
	}

	@Override
	public DoubleStream mapToDouble(ToDoubleFunction<? super Entity> mapper)
	{
		return stream.mapToDouble(mapper);
	}

	@Override
	public <R> Stream<R> flatMap(Function<? super Entity, ? extends Stream<? extends R>> mapper)
	{
		return stream.flatMap(mapper);
	}

	@Override
	public IntStream flatMapToInt(Function<? super Entity, ? extends IntStream> mapper)
	{
		return stream.flatMapToInt(mapper);
	}

	@Override
	public LongStream flatMapToLong(Function<? super Entity, ? extends LongStream> mapper)
	{
		return stream.flatMapToLong(mapper);
	}

	@Override
	public DoubleStream flatMapToDouble(Function<? super Entity, ? extends DoubleStream> mapper)
	{
		return stream.flatMapToDouble(mapper);
	}

	@Override
	public Stream<Entity> distinct()
	{
		return stream.distinct();
	}

	@Override
	public Stream<Entity> sorted()
	{
		return stream.sorted();
	}

	@Override
	public Stream<Entity> sorted(Comparator<? super Entity> comparator)
	{
		return stream.sorted(comparator);
	}

	@Override
	public Stream<Entity> peek(Consumer<? super Entity> action)
	{
		return stream.peek(action);
	}

	@Override
	public Stream<Entity> limit(long maxSize)
	{
		return stream.limit(maxSize);
	}

	@Override
	public Stream<Entity> skip(long n)
	{
		return stream.skip(n);
	}

	@Override
	public void forEach(Consumer<? super Entity> action)
	{
		stream.forEach(action);
	}

	@Override
	public void forEachOrdered(Consumer<? super Entity> action)
	{
		stream.forEachOrdered(action);
	}

	@Override
	public Object[] toArray()
	{
		return stream.toArray();
	}

	@Override
	public <A> A[] toArray(IntFunction<A[]> generator)
	{
		return stream.toArray(generator);
	}

	@Override
	public Entity reduce(Entity identity, BinaryOperator<Entity> accumulator)
	{
		return stream.reduce(identity, accumulator);
	}

	@Override
	public Optional<Entity> reduce(BinaryOperator<Entity> accumulator)
	{
		return stream.reduce(accumulator);
	}

	@Override
	public <U> U reduce(U identity, BiFunction<U, ? super Entity, U> accumulator, BinaryOperator<U> combiner)
	{
		return stream.reduce(identity, accumulator, combiner);
	}

	@Override
	public <R> R collect(Supplier<R> supplier, BiConsumer<R, ? super Entity> accumulator, BiConsumer<R, R> combiner)
	{
		return stream.collect(supplier, accumulator, combiner);
	}

	@Override
	public <R, A> R collect(Collector<? super Entity, A, R> collector)
	{
		return stream.collect(collector);
	}

	@Override
	public Optional<Entity> min(Comparator<? super Entity> comparator)
	{
		return stream.min(comparator);
	}

	@Override
	public Optional<Entity> max(Comparator<? super Entity> comparator)
	{
		return stream.max(comparator);
	}

	@Override
	public long count()
	{
		return stream.count();
	}

	@Override
	public boolean anyMatch(Predicate<? super Entity> predicate)
	{
		return stream.anyMatch(predicate);
	}

	@Override
	public boolean allMatch(Predicate<? super Entity> predicate)
	{
		return stream.allMatch(predicate);
	}

	@Override
	public boolean noneMatch(Predicate<? super Entity> predicate)
	{
		return stream.noneMatch(predicate);
	}

	@Override
	public Optional<Entity> findFirst()
	{
		return stream.findFirst();
	}

	@Override
	public Optional<Entity> findAny()
	{
		return stream.findAny();
	}
}
