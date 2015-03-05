package org.molgenis.ontology;

import org.molgenis.data.DataService;
import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.data.importer.EmxImportService;
import org.molgenis.ontology.matching.ProcessInputTermService;
import org.molgenis.ontology.matching.UploadProgress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync
@Import(
{ OntologyCoreConfiguration.class })
public class OntologyConfiguration
{
	@Autowired
	private SearchService searchService;

	@Autowired
	private EmxImportService emxImportService;

	@Autowired
	private DataService dataService;

	@Autowired
	private OntologyService ontologyService;

	@Bean
	public ProcessInputTermService processInputTermService()
	{
		return new ProcessInputTermService(emxImportService, dataService, uploadProgress(), ontologyService);
	}

	@Bean
	public UploadProgress uploadProgress()
	{
		return new UploadProgress();
	}
}
