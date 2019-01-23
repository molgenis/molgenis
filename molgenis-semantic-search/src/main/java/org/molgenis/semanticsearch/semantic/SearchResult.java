package org.molgenis.semanticsearch.semantic;

/**
 * @param <I> item type
 */
public interface SearchResult<I> {
  I getItem();

  int getRelevance();
}
