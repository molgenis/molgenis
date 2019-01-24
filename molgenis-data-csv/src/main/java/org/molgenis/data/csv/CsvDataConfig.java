package org.molgenis.data.csv;

import javax.annotation.PostConstruct;
import org.molgenis.data.file.FileRepositoryCollectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CsvDataConfig {

  @Autowired private FileRepositoryCollectionFactory fileRepositorySourceFactory;

  /**
   * Registers the CsvRepositorySource factory so it can be used by
   * DataService.createFileRepositorySource(File file);
   */
  @PostConstruct
  public void registerCsvRepositorySource() {
    fileRepositorySourceFactory.addFileRepositoryCollectionClass(
        CsvRepositoryCollection.class, CsvFileExtensions.getCSV());
  }
}
