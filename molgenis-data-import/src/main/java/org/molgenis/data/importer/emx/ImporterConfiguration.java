package org.molgenis.data.importer.emx;

import static java.util.Objects.requireNonNull;

import org.molgenis.data.DataService;
import org.molgenis.data.EntityManager;
import org.molgenis.data.i18n.model.L10nStringFactory;
import org.molgenis.data.i18n.model.LanguageFactory;
import org.molgenis.data.importer.DataPersister;
import org.molgenis.data.importer.ImportService;
import org.molgenis.data.importer.MetaDataParser;
import org.molgenis.data.meta.EntityTypeDependencyResolver;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.meta.model.PackageFactory;
import org.molgenis.data.meta.model.TagFactory;
import org.molgenis.data.security.permission.PermissionSystemService;
import org.molgenis.data.validation.meta.AttributeValidator;
import org.molgenis.data.validation.meta.EntityTypeValidator;
import org.molgenis.data.validation.meta.TagValidator;
import org.molgenis.security.core.UserPermissionEvaluator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ImporterConfiguration {
  private final DataService dataService;
  private final MetaDataService metaDataService;
  private final PermissionSystemService permissionSystemService;
  private final UserPermissionEvaluator permissionService;
  private final PackageFactory packageFactory;
  private final AttributeFactory attrMetaFactory;
  private final EntityTypeFactory entityTypeFactory;
  private final TagFactory tagFactory;
  private final LanguageFactory languageFactory;
  private final L10nStringFactory l10nStringFactory;
  private final EntityManager entityManager;
  private final EntityTypeValidator entityTypeValidator;
  private final AttributeValidator attributeValidator;
  private final TagValidator tagValidator;
  private final EntityTypeDependencyResolver entityTypeDependencyResolver;
  private final DataPersister dataPersister;

  public ImporterConfiguration(
      PackageFactory packageFactory,
      DataService dataService,
      MetaDataService metaDataService,
      PermissionSystemService permissionSystemService,
      EntityTypeDependencyResolver entityTypeDependencyResolver,
      UserPermissionEvaluator permissionService,
      AttributeValidator attributeValidator,
      AttributeFactory attrMetaFactory,
      EntityTypeFactory entityTypeFactory,
      DataPersister dataPersister,
      EntityTypeValidator entityTypeValidator,
      TagValidator tagValidator,
      TagFactory tagFactory,
      LanguageFactory languageFactory,
      L10nStringFactory l10nStringFactory,
      EntityManager entityManager) {
    this.packageFactory = requireNonNull(packageFactory);
    this.dataService = requireNonNull(dataService);
    this.metaDataService = requireNonNull(metaDataService);
    this.permissionSystemService = requireNonNull(permissionSystemService);
    this.entityTypeDependencyResolver = requireNonNull(entityTypeDependencyResolver);
    this.permissionService = requireNonNull(permissionService);
    this.attributeValidator = requireNonNull(attributeValidator);
    this.attrMetaFactory = requireNonNull(attrMetaFactory);
    this.entityTypeFactory = requireNonNull(entityTypeFactory);
    this.dataPersister = requireNonNull(dataPersister);
    this.entityTypeValidator = requireNonNull(entityTypeValidator);
    this.tagValidator = requireNonNull(tagValidator);
    this.tagFactory = requireNonNull(tagFactory);
    this.languageFactory = requireNonNull(languageFactory);
    this.l10nStringFactory = requireNonNull(l10nStringFactory);
    this.entityManager = requireNonNull(entityManager);
  }

  @Bean
  public ImportService emxImportService() {
    return new EmxImportService(emxMetaDataParser(), importWriter(), dataService);
  }

  @Bean
  public ImportWriter importWriter() {
    return new ImportWriter(
        metaDataService, permissionSystemService, permissionService, entityManager, dataPersister);
  }

  @Bean
  public MetaDataParser emxMetaDataParser() {
    return new EmxMetaDataParser(
        dataService,
        packageFactory,
        attrMetaFactory,
        entityTypeFactory,
        tagFactory,
        languageFactory,
        l10nStringFactory,
        entityTypeValidator,
        attributeValidator,
        tagValidator,
        entityTypeDependencyResolver);
  }
}
