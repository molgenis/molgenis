package org.molgenis.data.vcf;

import org.molgenis.data.file.FileRepositoryCollectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
public class VcfDataConfig
{

	@Autowired
	private FileRepositoryCollectionFactory fileRepositorySourceFactory;

	/**
	 * Registers the VcfRepositorySource factory so it can be used by DataService.createFileRepositorySource(File file);
	 */
	@PostConstruct
	public void registerVcfRepositorySource()
	{
		fileRepositorySourceFactory.addFileRepositoryCollectionClass(VcfRepositoryCollection.class,
				VcfRepositoryCollection.EXTENSIONS);
	}

}
