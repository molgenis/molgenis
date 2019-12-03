package org.molgenis.integrationtest.platform;

import static org.mockito.Mockito.mock;

import org.molgenis.data.DataService;
import org.molgenis.data.SystemRepositoryDecoratorFactoryRegistrar;
import org.molgenis.data.TestHarnessConfig;
import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.data.elasticsearch.client.ElasticsearchConfig;
import org.molgenis.data.file.FileRepositoryCollectionFactory;
import org.molgenis.data.importer.DataPersisterImpl;
import org.molgenis.data.platform.RepositoryCollectionDecoratorFactoryImpl;
import org.molgenis.data.platform.config.PlatformConfig;
import org.molgenis.data.populate.IdGeneratorImpl;
import org.molgenis.data.postgresql.DatabaseConfig;
import org.molgenis.data.postgresql.PostgreSqlConfiguration;
import org.molgenis.data.postgresql.identifier.EntityTypeRegistryPopulator;
import org.molgenis.data.security.DataserviceRoleHierarchy;
import org.molgenis.data.security.SystemEntityTypeRegistryImpl;
import org.molgenis.data.security.permission.DataPermissionConfig;
import org.molgenis.data.security.permission.EntityHelper;
import org.molgenis.data.security.permission.PermissionServiceImpl;
import org.molgenis.data.security.permission.UserRoleTools;
import org.molgenis.data.security.permission.inheritance.PermissionInheritanceResolver;
import org.molgenis.data.validation.ExpressionValidator;
import org.molgenis.integrationtest.config.JsonTestConfig;
import org.molgenis.integrationtest.config.ScriptTestConfig;
import org.molgenis.integrationtest.config.SecurityCoreITConfig;
import org.molgenis.integrationtest.data.TestAppSettings;
import org.molgenis.jobs.JobConfig;
import org.molgenis.jobs.JobExecutionConfig;
import org.molgenis.jobs.JobFactoryRegistrar;
import org.molgenis.ontology.core.config.OntologyConfig;
import org.molgenis.ontology.core.config.OntologyTestConfig;
import org.molgenis.security.acl.AclConfig;
import org.molgenis.security.acl.DataSourceAclTablesPopulator;
import org.molgenis.security.acl.MutableAclClassService;
import org.molgenis.security.acl.MutableAclClassServiceImpl;
import org.molgenis.security.acl.ObjectIdentityService;
import org.molgenis.security.core.GroupValueFactory;
import org.molgenis.security.core.MolgenisPasswordEncoder;
import org.molgenis.security.core.PermissionRegistry;
import org.molgenis.security.core.UserPermissionEvaluator;
import org.molgenis.security.core.runas.RunAsSystemAspect;
import org.molgenis.security.permission.AuthenticationAuthoritiesUpdaterImpl;
import org.molgenis.security.permission.PrincipalSecurityContextRegistryImpl;
import org.molgenis.security.permission.SecurityContextRegistryImpl;
import org.molgenis.security.permission.UserPermissionEvaluatorImpl;
import org.molgenis.semanticsearch.config.SemanticSearchConfig;
import org.molgenis.util.ApplicationContextProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.mail.MailSender;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement(proxyTargetClass = true)
@EnableAspectJAutoProxy
/*
FIXME Ideally, we'd like to scan all of org.molgenis.data or even org.molgenis, but there's some unwanted dependencies
in org.molgenis.data and subpackages from included modules
 */
@ComponentScan({
  "org.molgenis.data.security.aggregation",
  "org.molgenis.data.meta",
  "org.molgenis.data.index",
  "org.molgenis.js",
  "org.molgenis.data.elasticsearch",
  "org.molgenis.data.security.auth",
  "org.molgenis.data.security.permission",
  "org.molgenis.data.platform",
  "org.molgenis.data.meta.model",
  "org.molgenis.data.system.model",
  "org.molgenis.data.cache",
  "org.molgenis.data.i18n",
  "org.molgenis.i18n",
  "org.molgenis.web.i18n",
  "org.molgenis.data.postgresql",
  "org.molgenis.data.file.model",
  "org.molgenis.data.security.owned",
  "org.molgenis.data.security.user",
  "org.molgenis.data.validation",
  "org.molgenis.data.transaction",
  "org.molgenis.data.importer.emx",
  "org.molgenis.data.excel",
  "org.molgenis.util",
  "org.molgenis.settings",
  "org.molgenis.data.util",
  "org.molgenis.data.decorator",
  "org.molgenis.data.event",
  "org.molgenis.metrics"
})
@Import({
  PlatformITBaseConfig.class,
  SecurityCoreITConfig.class,
  PlatformBootstrapper.class,
  TestAppSettings.class,
  TestHarnessConfig.class,
  EntityBaseTestConfig.class,
  DatabaseConfig.class,
  ElasticsearchConfig.class,
  PostgreSqlConfiguration.class,
  RunAsSystemAspect.class,
  IdGeneratorImpl.class,
  ExpressionValidator.class,
  PlatformConfig.class,
  OntologyTestConfig.class,
  JobConfig.class,
  org.molgenis.data.RepositoryCollectionRegistry.class,
  RepositoryCollectionDecoratorFactoryImpl.class,
  DataSourceAclTablesPopulator.class,
  org.molgenis.data.RepositoryCollectionBootstrapper.class,
  org.molgenis.data.EntityFactoryRegistrar.class,
  org.molgenis.data.importer.emx.EmxImportService.class,
  DataPersisterImpl.class,
  org.molgenis.data.importer.ImportServiceFactory.class,
  FileRepositoryCollectionFactory.class,
  org.molgenis.data.excel.ExcelDataConfig.class,
  org.molgenis.security.permission.PermissionSystemServiceImpl.class,
  PrincipalSecurityContextRegistryImpl.class,
  AuthenticationAuthoritiesUpdaterImpl.class,
  SecurityContextRegistryImpl.class,
  org.molgenis.data.importer.ImportServiceRegistrar.class,
  EntityTypeRegistryPopulator.class,
  UserPermissionEvaluatorImpl.class,
  DataserviceRoleHierarchy.class,
  SystemRepositoryDecoratorFactoryRegistrar.class,
  SemanticSearchConfig.class,
  OntologyConfig.class,
  JobExecutionConfig.class,
  JobFactoryRegistrar.class,
  SystemEntityTypeRegistryImpl.class,
  ScriptTestConfig.class,
  AclConfig.class,
  MutableAclClassServiceImpl.class,
  PermissionRegistry.class,
  DataPermissionConfig.class,
  JsonTestConfig.class,
  GroupValueFactory.class
})
public class PlatformITConfig implements ApplicationListener<ContextRefreshedEvent> {
  @Autowired private PlatformBootstrapper platformBootstrapper;
  @Autowired private MutableAclService mutableAclService;
  @Autowired private PermissionInheritanceResolver inheritanceResolver;
  @Autowired private ObjectIdentityService objectIdentityService;
  @Autowired private DataService dataService;
  @Autowired private MutableAclClassService mutableAclClassService;
  @Autowired private UserRoleTools userRoleTools;
  @Autowired private EntityHelper entityHelper;
  @Autowired private UserPermissionEvaluator userPermissionEvaluator;

  @Override
  public void onApplicationEvent(ContextRefreshedEvent event) {
    platformBootstrapper.bootstrap(event);
  }

  @Bean
  public RunAsSystemPermissionService permissionService() {
    return new RunAsSystemPermissionService(
        new PermissionServiceImpl(
            mutableAclService,
            inheritanceResolver,
            objectIdentityService,
            dataService,
            mutableAclClassService,
            userRoleTools,
            entityHelper,
            userPermissionEvaluator));
  }

  @Bean
  public static PropertySourcesPlaceholderConfigurer properties() {
    PropertySourcesPlaceholderConfigurer pspc = new PropertySourcesPlaceholderConfigurer();
    Resource[] resources = new Resource[] {new ClassPathResource("/conf/molgenis.properties")};
    pspc.setLocations(resources);
    pspc.setFileEncoding("UTF-8");
    pspc.setIgnoreUnresolvablePlaceholders(true);
    pspc.setIgnoreResourceNotFound(true);
    pspc.setNullValue("@null");
    return pspc;
  }

  @Bean
  public MailSender mailSender() {
    return mock(MailSender.class);
  }

  @Bean
  public ApplicationContextProvider applicationContextProvider() {
    return new ApplicationContextProvider();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new MolgenisPasswordEncoder(new BCryptPasswordEncoder());
  }
}
