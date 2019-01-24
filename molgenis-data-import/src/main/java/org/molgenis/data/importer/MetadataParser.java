package org.molgenis.data.importer;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.molgenis.data.RepositoryCollection;

public interface MetadataParser {
  /**
   * Parses the metadata of the entities to import.
   *
   * @param source {@link RepositoryCollection} containing the data to parse
   * @param packageId , the package where the entities should go. Default if none was supplied
   * @return {@link ParsedMetaData}
   */
  ParsedMetaData parse(RepositoryCollection source, @Nullable @CheckForNull String packageId);

  /** Generates a {@link EntitiesValidationReport} by parsing all data from a supplied source */
  EntitiesValidationReport validate(RepositoryCollection source);
}
