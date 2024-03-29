package org.molgenis.dataexplorer;

import static java.time.LocalDateTime.parse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.molgenis.data.RepositoryCapability.WRITABLE;
import static org.molgenis.data.meta.AttributeType.STRING;
import static org.molgenis.data.meta.model.EntityTypeMetadata.IS_ABSTRACT;
import static org.molgenis.data.security.EntityTypePermission.READ_DATA;
import static org.molgenis.data.security.PackagePermission.ADD_ENTITY_TYPE;
import static org.molgenis.dataexplorer.controller.DataExplorerController.NAVIGATOR;
import static org.molgenis.dataexplorer.controller.DataRequest.DownloadType.DOWNLOAD_TYPE_CSV;
import static org.molgenis.dataexplorer.controller.DataRequest.DownloadType.DOWNLOAD_TYPE_XLSX;

import freemarker.template.Configuration;
import freemarker.template.Template;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.Repository;
import org.molgenis.data.UnknownEntityTypeException;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeMetadata;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.security.EntityTypeIdentity;
import org.molgenis.data.security.EntityTypePermission;
import org.molgenis.data.security.PackageIdentity;
import org.molgenis.dataexplorer.controller.DataExplorerController;
import org.molgenis.dataexplorer.controller.NavigatorLink;
import org.molgenis.dataexplorer.settings.DataExplorerSettings;
import org.molgenis.security.core.UserPermissionEvaluator;
import org.molgenis.settings.AppSettings;
import org.molgenis.test.AbstractMockitoSpringContextTests;
import org.molgenis.web.converter.GsonWebConfig;
import org.molgenis.web.menu.MenuReaderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.ui.Model;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

@MockitoSettings(strictness = Strictness.LENIENT)
@WebAppConfiguration
@ContextConfiguration(classes = {GsonWebConfig.class})
class DataExplorerControllerTest extends AbstractMockitoSpringContextTests {
  @InjectMocks private DataExplorerController controller = new DataExplorerController();

  @Mock private Repository<Entity> repository;

  @Mock private EntityType entityType;
  private String entityTypeId = "id";

  @Mock private Entity entity;
  private String entityId = "1";

  @Mock private Attribute idAttr;

  @Mock private Configuration configuration;

  @Mock private Model model;

  @Mock private AppSettings appSettings;
  @Mock private DataExplorerSettings dataExplorerSettings;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private DataService dataService;

  @Mock private FreeMarkerConfigurer freemarkerConfigurer;
  @Mock private UserPermissionEvaluator permissionService = mock(UserPermissionEvaluator.class);
  @Mock private MenuReaderService menuReaderService;
  @Mock private LocaleResolver localeResolver;

  @Mock private Package package_;
  @Mock private Package parentPackage;

  @Autowired private GsonHttpMessageConverter gsonHttpMessageConverter;

  @BeforeEach
  void beforeTest() {
    when(permissionService.hasPermission(
            new EntityTypeIdentity("yes"), EntityTypePermission.UPDATE_METADATA))
        .thenReturn(true);
    when(permissionService.hasPermission(
            new EntityTypeIdentity("no"), EntityTypePermission.UPDATE_METADATA))
        .thenReturn(false);

    when(idAttr.getDataType()).thenReturn(STRING);
    when(entityType.getIdAttribute()).thenReturn(idAttr);
    when(package_.getLabel()).thenReturn("pack");
    when(package_.getId()).thenReturn("packId");
    when(parentPackage.getLabel()).thenReturn("parent");
    when(parentPackage.getId()).thenReturn("parentId");
    when(package_.getParent()).thenReturn(parentPackage);
    when(entityType.getPackage()).thenReturn(package_);
    when(repository.findOneById(entityId)).thenReturn(entity);
    when(dataService.getEntityType(entityTypeId)).thenReturn(entityType);
    when(dataService.getRepository(entityTypeId)).thenReturn(repository);

    when(dataExplorerSettings.getEntityReport(entityTypeId)).thenReturn("template");
    when(dataExplorerSettings.getModStandaloneReports()).thenReturn(true);

    when(freemarkerConfigurer.getConfiguration()).thenReturn(configuration);

    when(menuReaderService.findMenuItemPath(NAVIGATOR)).thenReturn(null);

    when(localeResolver.resolveLocale(any())).thenReturn(Locale.ENGLISH);
    MockMvcBuilders.standaloneSetup(controller)
        .setMessageConverters(gsonHttpMessageConverter)
        .build();
  }

  @Test
  void initSetNavigatorMenuPathNoNavigator() {
    String selectedEntityname = "selectedEntityname";
    String selectedEntityId = "selectedEntityId";
    String navigatorPath = "path/to-navigator";

    MetaDataService metaDataService = mock(MetaDataService.class);
    when(dataService.getMeta()).thenReturn(metaDataService);
    when(metaDataService.getEntityTypes()).thenReturn(Stream.empty());

    controller.init(selectedEntityname, selectedEntityId, model);

    verify(model, never()).addAttribute("navigatorBaseUrl", navigatorPath);
  }

  @Test
  void initSortEntitiesByLabel() {
    Query<EntityType> query = mock(Query.class, RETURNS_DEEP_STUBS);
    when(query.eq(IS_ABSTRACT, false)).thenReturn(query);
    when(query.fetch(any())).thenReturn(query);
    when(dataService.query(EntityTypeMetadata.ENTITY_TYPE_META_DATA, EntityType.class))
        .thenReturn(query);

    EntityType entity1 = mock(EntityType.class);
    when(entity1.getId()).thenReturn("1");
    when(entity1.getLabel()).thenReturn("zzz");

    EntityType entity2 = mock(EntityType.class);
    when(entity2.getId()).thenReturn("2");
    when(entity2.getLabel()).thenReturn("aaa");

    Stream<EntityType> entityStream = Stream.of(entity1, entity2);
    when(query.findAll()).thenReturn(entityStream);

    controller.init(null, null, model);

    LinkedHashMap expected =
        new LinkedHashMap<>(
            Stream.of(entity1, entity2)
                .sorted(Comparator.comparing(EntityType::getLabel))
                .collect(Collectors.toMap(EntityType::getId, Function.identity())));

    verify(model).addAttribute("entitiesMeta", expected);
  }

  @Test
  void initTrackingIdPresent() {
    String selectedEntityname = "selectedEntityname";
    String selectedEntityId = "selectedEntityId";

    MetaDataService metaDataService = mock(MetaDataService.class);
    when(dataService.getMeta()).thenReturn(metaDataService);
    when(metaDataService.getEntityTypes()).thenReturn(Stream.empty());

    when(appSettings.getGoogleAnalyticsTrackingId()).thenReturn("id");
    when(appSettings.getGoogleAnalyticsTrackingIdMolgenis()).thenReturn("id");

    controller.init(selectedEntityname, selectedEntityId, model);

    verify(model).addAttribute("hasTrackingId", true);
    verify(model).addAttribute("hasMolgenisTrackingId", true);
  }

  @Test
  void initTrackingIdNotPresent() {
    String selectedEntityname = "selectedEntityname";
    String selectedEntityId = "selectedEntityId";

    MetaDataService metaDataService = mock(MetaDataService.class);
    when(dataService.getMeta()).thenReturn(metaDataService);
    when(metaDataService.getEntityTypes()).thenReturn(Stream.empty());

    controller.init(selectedEntityname, selectedEntityId, model);

    verify(model).addAttribute("hasTrackingId", false);
    verify(model).addAttribute("hasMolgenisTrackingId", false);
  }

  @Test
  void testViewEntityDetails() throws Exception {
    when(configuration.getTemplate("view-entityreport-specific-template.ftl"))
        .thenReturn(mock(Template.class));

    String actual = controller.viewEntityDetails(entityTypeId, entityId, model);
    String expected = "view-entityreport";

    assertEquals(expected, actual);
    verify(model).addAttribute("entity", entity);
    verify(model).addAttribute("entityType", entityType);
    verify(model).addAttribute("viewName", "view-entityreport-specific-template");
    verify(model).addAttribute("showStandaloneReportUrl", true);
    verify(model).addAttribute("entityTypeId", entityTypeId);
    verify(model).addAttribute("entityId", entityId);
  }

  @Test
  void testViewEntityDetailsById() throws Exception {
    when(dataService.hasEntityType(entityTypeId)).thenReturn(true);
    String entityTypeLabel = "MyEntityTypeLabel";
    when(entityType.getLabel()).thenReturn(entityTypeLabel);
    when(configuration.getTemplate("view-standalone-report-specific-" + entityTypeId + ".ftl"))
        .thenReturn(mock(Template.class));

    String actual = controller.viewEntityDetailsById(entityTypeId, entityId, model);
    String expected = "view-standalone-report";

    assertEquals(expected, actual);
    verify(model).addAttribute("entity", entity);
    verify(model).addAttribute("entityType", entityType);
    verify(model).addAttribute("entityTypeId", entityTypeId);
    verify(model).addAttribute("entityTypeLabel", entityTypeLabel);
    verify(model).addAttribute("viewName", "view-standalone-report-specific-id");
  }

  @Test
  void testViewEntityDetailsByIdEntityTypeNotExists() {
    when(dataService.getEntityType(entityTypeId)).thenReturn(null);
    assertThrows(
        UnknownEntityTypeException.class,
        () -> controller.viewEntityDetailsById(entityTypeId, entityId, model));
  }

  @Test
  void testGetDownloadFilenameCsv() {
    assertEquals(
        "it_emx_datatypes_TypeTest_2017-07-04_14_14_33.csv",
        controller.getDownloadFilename(
            "it_emx_datatypes_TypeTest", parse("2017-07-04T14:14:33"), DOWNLOAD_TYPE_CSV));
  }

  @Test
  void testGetDownloadFilenameXlsx() {
    assertEquals(
        "it_emx_datatypes_TypeTest_2017-07-04_14_14_33.xlsx",
        controller.getDownloadFilename(
            "it_emx_datatypes_TypeTest", parse("2017-07-04T14:14:33"), DOWNLOAD_TYPE_XLSX));
  }

  @Test
  void testPackageLink() {
    when(dataService.hasEntityType(entityTypeId)).thenReturn(true);

    when(menuReaderService.findMenuItemPath(NAVIGATOR))
        .thenReturn("menu/main/navigation/navigator");
    List<NavigatorLink> expected = new LinkedList<>();
    expected.add(NavigatorLink.create("menu/main/navigation/navigator/", "glyphicon-home"));
    expected.add(NavigatorLink.create("menu/main/navigation/navigator/parentId", "parent"));
    expected.add(NavigatorLink.create("menu/main/navigation/navigator/packId", "pack"));
    assertEquals(expected, controller.getNavigatorLinks(entityTypeId));
  }

  @Test
  void testShowCopyNoReadDataPermission() {
    when(dataService.getCapabilities(entityTypeId).contains(WRITABLE)).thenReturn(true);
    when(permissionService.hasPermission(new EntityTypeIdentity(entityTypeId), READ_DATA))
        .thenReturn(false);
    when(permissionService.hasPermission(new PackageIdentity(package_), ADD_ENTITY_TYPE))
        .thenReturn(true);

    assertFalse(controller.showCopy(entityTypeId));
  }

  @Test
  void testShowCopyNoParentPackage() {
    when(entityType.getPackage()).thenReturn(null);

    when(dataService.getEntityType(entityTypeId)).thenReturn(entityType);
    when(dataService.getCapabilities(entityTypeId).contains(WRITABLE)).thenReturn(true);
    when(permissionService.hasPermission(new EntityTypeIdentity(entityTypeId), READ_DATA))
        .thenReturn(true);

    assertFalse(controller.showCopy(entityTypeId));
  }

  @Test
  void testShowCopyNoAddEntityTypePermission() {
    when(dataService.getCapabilities(entityTypeId).contains(WRITABLE)).thenReturn(true);
    when(permissionService.hasPermission(new EntityTypeIdentity(entityTypeId), READ_DATA))
        .thenReturn(true);
    when(permissionService.hasPermission(new PackageIdentity(package_), ADD_ENTITY_TYPE))
        .thenReturn(false);

    assertFalse(controller.showCopy(entityTypeId));
  }

  @Test
  void testShowCopyRepositoryNotWritable() {
    when(dataService.getCapabilities(entityTypeId).contains(WRITABLE)).thenReturn(false);
    when(permissionService.hasPermission(new EntityTypeIdentity(entityTypeId), READ_DATA))
        .thenReturn(true);
    when(permissionService.hasPermission(new PackageIdentity(package_), ADD_ENTITY_TYPE))
        .thenReturn(true);

    assertFalse(controller.showCopy(entityTypeId));
  }

  @Test
  void testShowCopyAllowed() {
    when(dataService.hasEntityType(entityTypeId)).thenReturn(true);

    when(dataService.getCapabilities(entityTypeId).contains(WRITABLE)).thenReturn(true);
    when(permissionService.hasPermission(new EntityTypeIdentity(entityTypeId), READ_DATA))
        .thenReturn(true);
    when(permissionService.hasPermission(new PackageIdentity(package_), ADD_ENTITY_TYPE))
        .thenReturn(true);

    assertTrue(controller.showCopy(entityTypeId));
  }
}
