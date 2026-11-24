package org.molgenis.data.index;

import static java.util.Objects.requireNonNull;

import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.springframework.stereotype.Component;

@Component
public class IndexedRepositoryDecoratorFactory {
  private final SearchService searchService;
  private final IndexActionScheduler indexActionScheduler;

  IndexedRepositoryDecoratorFactory(
      SearchService searchService, IndexActionScheduler indexActionScheduler) {
    this.searchService = requireNonNull(searchService);
    this.indexActionScheduler = requireNonNull(indexActionScheduler);
  }

  public IndexedRepositoryDecorator create(Repository<Entity> delegateRepository) {
    return new IndexedRepositoryDecorator(delegateRepository, searchService, indexActionScheduler);
  }
}
