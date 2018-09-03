package org.molgenis.core.ui.data.importer.wizard;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.molgenis.data.DataAction.ADD;
import static org.molgenis.data.importer.MetadataAction.UPSERT;
import static org.testng.Assert.assertTrue;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.molgenis.data.file.FileRepositoryCollectionFactory;
import org.molgenis.data.importer.ImportService;
import org.molgenis.data.importer.ImportServiceFactory;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.validation.BindingResult;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class PackageWizardPageTest extends AbstractMockitoTest {
  @Mock private FileRepositoryCollectionFactory fileRepositoryCollectionFactory;
  @Mock private ImportServiceFactory importServiceFactory;
  @Mock private MetaDataService metaDataService;
  @Mock private ImportService importService;

  private PackageWizardPage packageWizardPage;

  @SuppressWarnings("unchecked")
  @BeforeMethod
  public void beforeTest() {
    when(importServiceFactory.getImportService(any(), any())).thenReturn(importService);
    LinkedHashMap importableEntities = new LinkedHashMap<>();
    importableEntities.put("pack1_test", true);
    when(importService.determineImportableEntities(any(), any(), any()))
        .thenReturn(importableEntities);
    packageWizardPage =
        new PackageWizardPage(
            fileRepositoryCollectionFactory, importServiceFactory, metaDataService);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testHandleRequest() {
    HttpServletRequest request = mock(HttpServletRequest.class);
    BindingResult bindingResult = mock(BindingResult.class);
    ImportWizard wizard = mock(ImportWizard.class);
    when(wizard.getMetadataImportOption()).thenReturn(UPSERT.toString().toLowerCase());
    when(wizard.getDataImportOption()).thenReturn(ADD.toString().toLowerCase());
    Map<String, Collection<String>> result = new HashMap<>();
    result.put("test", null);

    when(wizard.getFieldsAvailable()).thenReturn(result);
    when(wizard.getFieldsDetected()).thenReturn(result);
    when(wizard.getFieldsRequired()).thenReturn(result);
    when(wizard.getFieldsUnknown()).thenReturn(result);

    when(request.getParameter("selectedPackage")).thenReturn("pack1");
    packageWizardPage.handleRequest(request, bindingResult, wizard);

    ArgumentCaptor<HashMap> argumentCaptor1 = ArgumentCaptor.forClass(HashMap.class);
    verify(wizard).setFieldsAvailable(argumentCaptor1.capture());
    assertTrue(argumentCaptor1.getValue().keySet().contains("pack1_test"));

    ArgumentCaptor<HashMap> argumentCaptor2 = ArgumentCaptor.forClass(HashMap.class);
    verify(wizard).setFieldsAvailable(argumentCaptor2.capture());
    assertTrue(argumentCaptor2.getValue().keySet().contains("pack1_test"));

    ArgumentCaptor<HashMap> argumentCaptor3 = ArgumentCaptor.forClass(HashMap.class);
    verify(wizard).setFieldsAvailable(argumentCaptor3.capture());
    assertTrue(argumentCaptor3.getValue().keySet().contains("pack1_test"));

    ArgumentCaptor<HashMap> argumentCaptor4 = ArgumentCaptor.forClass(HashMap.class);
    verify(wizard).setFieldsAvailable(argumentCaptor4.capture());
    assertTrue(argumentCaptor4.getValue().keySet().contains("pack1_test"));
  }
}
