package org.molgenis.data.vcf;

import javax.annotation.PostConstruct;
import org.molgenis.data.file.FileRepositoryCollectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VcfDataConfig {

  @Autowired private FileRepositoryCollectionFactory fileRepositorySourceFactory;

  /**
   * Registers the VcfRepositorySource factory so it can be used by
   * DataService.createFileRepositorySource(File file);
   */
  @PostConstruct
  public void registerVcfRepositorySource() {
    fileRepositorySourceFactory.addFileRepositoryCollectionClass(
        VcfRepositoryCollection.class, VcfRepositoryCollection.EXTENSIONS);
  }
}
