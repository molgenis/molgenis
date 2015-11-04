package org.molgenis.data.importer;

import org.molgenis.data.DataService;
import org.molgenis.data.mysql.EmxImportServiceRegistrator;
import org.molgenis.data.semantic.LabeledResource;
import org.molgenis.data.semanticsearch.service.TagService;
import org.molgenis.security.core.MolgenisPermissionService;
import org.molgenis.security.permission.PermissionSystemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ImporterConfiguration
{
	@Autowired
	private DataService dataService;
	@Autowired
	private PermissionSystemService permissionSystemService;
	@Autowired
	private TagService<LabeledResource, LabeledResource> tagService;
	@Autowired
	private ImportServiceFactory importServiceFactory;
	@Autowired
	private MolgenisPermissionService molgenisPermissionService;

	@Bean
	public ImportService emxImportService()
	{
		return new EmxImportService(emxMetaDataParser(), importWriter(), dataService);
	}

	@Bean
	public ImportWriter importWriter()
	{
		return new ImportWriter(dataService, permissionSystemService, tagService, molgenisPermissionService);
	}

	@Bean
	public MetaDataParser emxMetaDataParser()
	{
		return new EmxMetaDataParser(dataService);
	}

	@Bean
	public EmxImportServiceRegistrator mysqlRepositoryRegistrator()
	{
		return new EmxImportServiceRegistrator(importServiceFactory, emxImportService());
	}
}
