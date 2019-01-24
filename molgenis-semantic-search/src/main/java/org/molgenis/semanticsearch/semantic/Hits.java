package org.molgenis.semanticsearch.semantic;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nonnull;

@AutoValue
@SuppressWarnings(
    "squid:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class Hits<T> implements Iterable<Hit<T>> {
  public abstract List<Hit<T>> getHits();

  @Override
  public @Nonnull Iterator<Hit<T>> iterator() {
    return getHits().iterator();
  }

  public boolean hasHits() {
    return iterator().hasNext();
  }

  public static <T> Hits<T> create(List<Hit<T>> hits) {
    return new AutoValue_Hits<>(ImmutableList.copyOf(hits));
  }

  @SafeVarargs
  public static <T> Hits<T> create(Hit<T>... hits) {
    return new AutoValue_Hits<>(ImmutableList.copyOf(hits));
  }
}
