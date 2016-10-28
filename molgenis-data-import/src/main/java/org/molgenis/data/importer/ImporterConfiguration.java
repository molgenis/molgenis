package org.molgenis.data.importer;

import org.molgenis.data.DataService;
import org.molgenis.data.EntityManager;
import org.molgenis.data.i18n.model.I18nStringFactory;
import org.molgenis.data.i18n.model.I18nStringMetaData;
import org.molgenis.data.i18n.model.LanguageFactory;
import org.molgenis.data.meta.model.*;
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

	@Autowired
	private TagMetadata tagMetadata;

	@Autowired
	private I18nStringMetaData i18nStringMetaData;

	@Autowired
	private PackageFactory packageFactory;

	@Autowired
	private AttributeFactory attrMetaFactory;

	@Autowired
	private EntityTypeFactory entityTypeFactory;

	@Autowired
	private TagFactory tagFactory;

	@Autowired
	private LanguageFactory languageFactory;

	@Autowired
	private I18nStringFactory i18nStringFactory;

	@Autowired
	private EntityManager entityManager;

	@Bean
	public ImportService emxImportService()
	{
		return new EmxImportService(emxMetaDataParser(), importWriter(), dataService);
	}

	@Bean
	public ImportWriter importWriter()
	{
		return new ImportWriter(dataService, permissionSystemService, tagService, molgenisPermissionService, tagFactory,
				entityManager);
	}

	@Bean
	public MetaDataParser emxMetaDataParser()
	{
		return new EmxMetaDataParser(dataService, packageFactory, attrMetaFactory, entityTypeFactory, tagFactory,
				languageFactory, i18nStringFactory);
	}
}
