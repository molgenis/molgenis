package org.molgenis.data.importer.config;

import org.molgenis.data.DataService;
import org.molgenis.data.EntityManager;
import org.molgenis.data.i18n.model.I18nStringFactory;
import org.molgenis.data.i18n.model.I18nStringMetaData;
import org.molgenis.data.i18n.model.LanguageFactory;
import org.molgenis.data.importer.ImportService;
import org.molgenis.data.importer.ImportServiceFactory;
import org.molgenis.data.importer.MetaDataParser;
import org.molgenis.data.importer.emx.EmxImportService;
import org.molgenis.data.importer.emx.EmxMetaDataParser;
import org.molgenis.data.importer.emx.ImportWriter;
import org.molgenis.data.meta.EntityTypeDependencyResolver;
import org.molgenis.data.meta.model.*;
import org.molgenis.data.validation.meta.AttributeValidator;
import org.molgenis.data.validation.meta.EntityTypeValidator;
import org.molgenis.data.validation.meta.TagValidator;
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

	@Autowired
	private EntityTypeValidator entityTypeValidator;

	@Autowired
	private AttributeValidator attributeValidator;

	@Autowired
	private TagValidator tagValidator;

	@Autowired
	private EntityTypeDependencyResolver entityTypeDependencyResolver;

	@Bean
	public ImportService emxImportService()
	{
		return new EmxImportService(emxMetaDataParser(), importWriter(), dataService);
	}

	@Bean
	public ImportWriter importWriter()
	{
		return new ImportWriter(dataService, permissionSystemService, molgenisPermissionService, entityManager,
				entityTypeDependencyResolver);
	}

	@Bean
	public MetaDataParser emxMetaDataParser()
	{
		return new EmxMetaDataParser(dataService, packageFactory, attrMetaFactory, entityTypeFactory, tagFactory,
				languageFactory, i18nStringFactory, entityTypeValidator, attributeValidator, tagValidator,
				entityTypeDependencyResolver);
	}
}
