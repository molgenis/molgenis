package org.molgenis.integrationtest.download;

import static bad.robot.excel.matchers.WorkbookMatcher.sameWorkbook;
import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.testng.AssertJUnit.assertEquals;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.molgenis.data.DataService;
import org.molgenis.data.EntityTestHarness;
import org.molgenis.data.export.EmxExportService;
import org.molgenis.data.export.EmxExportServiceImpl;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.meta.model.PackageFactory;
import org.molgenis.integrationtest.config.FileTestConfig;
import org.molgenis.integrationtest.platform.PlatformITConfig;
import org.molgenis.jobs.Progress;
import org.molgenis.util.ResourceUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithSecurityContextTestExecutionListener;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.testng.AbstractTransactionalTestNGSpringContextTests;
import org.springframework.transaction.annotation.Transactional;
import org.testng.annotations.Test;

@ContextConfiguration(
    classes = {PlatformITConfig.class, FileTestConfig.class, EmxExportServiceImpl.class})
@TestExecutionListeners(listeners = {WithSecurityContextTestExecutionListener.class})
@Transactional
@Rollback
public class EmxExportServiceIT extends AbstractTransactionalTestNGSpringContextTests {

  private static final String USERNAME = "emx_user";
  static final String ROLE_SU = "SU";

  @Autowired private EntityTestHarness entityTestHarness;
  @Autowired private DataService dataService;
  @Autowired private PackageFactory packageFactory;

  @Autowired private EmxExportService emxDownloadService;

  @WithMockUser(
      username = USERNAME,
      roles = {ROLE_SU})
  @Test
  public void test() throws IOException {
    EntityType entityType1 = entityTestHarness.createDynamicRefEntityType("test1");
    dataService.getMeta().addEntityType(entityType1);
    dataService.add(
        entityType1.getId(), entityTestHarness.createTestRefEntities(entityType1, 2).stream());

    EntityType entityType2 = entityTestHarness.createDynamicRefEntityType("it_emx_test1");
    Package it = packageFactory.create("it");
    it.setLabel("it");
    Package emx = packageFactory.create("it_emx");
    emx.setLabel("it_emx");
    emx.setParent(it);
    dataService.getMeta().addPackage(it);
    dataService.getMeta().addPackage(emx);
    entityType2.setPackage(emx);
    dataService.getMeta().addEntityType(entityType2);
    dataService.add(
        entityType2.getId(), entityTestHarness.createTestRefEntities(entityType2, 2).stream());
    Path actual = Files.createTempFile("test", ".xlsx");
    // when using the "in memory" it package, created above, the "getChildern" returns null.
    Package actualIt = dataService.getMeta().getPackage("it").get();
    // We're testing the export service, not a job, use TestProgress to check if progress is updated
    Progress progress = new TestProgress();
    emxDownloadService.export(newArrayList(entityType1), newArrayList(actualIt), actual, progress);
    try (XSSFWorkbook actualWorkbook = new XSSFWorkbook(Files.newInputStream(actual))) {
      try (Workbook expected =
          new XSSFWorkbook(
              new FileInputStream(
                  ResourceUtils.getFile(
                      EmxExportServiceIT.class, "/xls/expectedDownloadResult.xlsx")))) {
        assertThat(actualWorkbook, sameWorkbook(expected));
      }
    }
    assertEquals(
        progress,
        new TestProgress(
            3,
            3,
            "Downloading: test1\nDownloading: it_emx_test1\nFinished downloading package metadata",
            ""));
  }
}
