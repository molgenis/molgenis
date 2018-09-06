package org.molgenis.core.ui.data.importer.wizard;

import static java.util.Collections.*;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import java.io.File;
import javax.servlet.http.HttpServletRequest;
import org.mockito.Mock;
import org.molgenis.data.DataService;
import org.molgenis.data.file.FileRepositoryCollectionFactory;
import org.molgenis.data.file.support.FileRepositoryCollection;
import org.molgenis.data.importer.EntitiesValidationReport;
import org.molgenis.data.importer.ImportService;
import org.molgenis.data.importer.ImportServiceFactory;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.Package;
import org.molgenis.security.core.UserPermissionEvaluator;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class OptionsWizardPageTest extends AbstractMockitoTest {
  @Mock private FileRepositoryCollectionFactory fileRepositoryCollectionFactory;
  @Mock private ImportServiceFactory importServiceFactory;
  @Mock private DataService dataService;
  @Mock private UserPermissionEvaluator userPermissionEvaluator;

  private OptionsWizardPage optionsWizardPage;

  @BeforeMethod
  public void setUpBeforeMethod() {
    optionsWizardPage =
        new OptionsWizardPage(
            fileRepositoryCollectionFactory,
            importServiceFactory,
            dataService,
            userPermissionEvaluator);
  }

  // test for https://github.com/molgenis/molgenis/issues/7448
  @Test
  public void testHandleRequestEntitiesInPackages() {
    HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
    BindingResult bindingResult = mock(BindingResult.class);
    ImportWizard wizard = mock(ImportWizard.class);
    File file = mock(File.class);
    when(wizard.getFile()).thenReturn(file);
    FileRepositoryCollection fileRepositoryCollection = mock(FileRepositoryCollection.class);
    when(fileRepositoryCollectionFactory.createFileRepositoryCollection(file))
        .thenReturn(fileRepositoryCollection);
    ImportService importService = mock(ImportService.class);
    EntitiesValidationReport entitiesValidationReport = mock(EntitiesValidationReport.class);
    when(importService.validateImport(file, fileRepositoryCollection))
        .thenReturn(entitiesValidationReport);
    when(importServiceFactory.getImportService(file, fileRepositoryCollection))
        .thenReturn(importService);
    MetaDataService metaDataService = mock(MetaDataService.class);
    when(metaDataService.getPackages()).thenReturn(emptyList());
    when(dataService.getMeta()).thenReturn(metaDataService);
    when(entitiesValidationReport.valid()).thenReturn(true);
    assertEquals(
        optionsWizardPage.handleRequest(httpServletRequest, bindingResult, wizard),
        "File is validated and can be imported.");
    verify(wizard).setEntitiesInDefaultPackage(emptyList());
    verify(wizard).setPackages(emptyMap());
  }

  // test for https://github.com/molgenis/molgenis/issues/7448
  @Test
  public void testHandleRequestNoWritablePackageException() {
    HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
    when(httpServletRequest.getParameter("data-option")).thenReturn("MyAction");
    BindingResult bindingResult = mock(BindingResult.class);
    ImportWizard wizard = mock(ImportWizard.class);
    File file = mock(File.class);
    when(file.getName()).thenReturn("fileName");
    when(wizard.getFile()).thenReturn(file);
    FileRepositoryCollection fileRepositoryCollection = mock(FileRepositoryCollection.class);
    when(fileRepositoryCollectionFactory.createFileRepositoryCollection(file))
        .thenReturn(fileRepositoryCollection);
    ImportService importService = mock(ImportService.class);
    EntitiesValidationReport entitiesValidationReport = mock(EntitiesValidationReport.class);
    when(entitiesValidationReport.getSheetsImportable())
        .thenReturn(singletonMap("MyEntityType", true));
    when(importService.validateImport(file, fileRepositoryCollection))
        .thenReturn(entitiesValidationReport);
    when(importServiceFactory.getImportService(file, fileRepositoryCollection))
        .thenReturn(importService);
    MetaDataService metaDataService = mock(MetaDataService.class);
    Package package0 = mock(Package.class);
    when(package0.getId()).thenReturn("packageId");
    when(metaDataService.getPackages()).thenReturn(singletonList(package0));
    when(dataService.getMeta()).thenReturn(metaDataService);
    assertNull(optionsWizardPage.handleRequest(httpServletRequest, bindingResult, wizard));
    verify(bindingResult)
        .addError(
            new ObjectError(
                "wizard",
                "<b>Your import failed:</b><br />FAILED TO FORMAT LOCALIZED MESSAGE FOR ERROR CODE DS07.%nFallback message: no writable package"));
  }
}
