package org.molgenis.metadata.manager.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.metadata.manager.model.EditorPackageIdentifier.create;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.molgenis.data.DataService;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.meta.model.PackageFactory;
import org.molgenis.data.meta.model.PackageMetadata;
import org.molgenis.metadata.manager.model.EditorPackageIdentifier;

class PackageMapperTest {
  @Mock private PackageFactory packageFactory;

  @Mock private DataService dataService;

  private PackageMapper packageMapper;

  @BeforeEach
  void setUpBeforeMethod() {
    MockitoAnnotations.initMocks(this);
    PackageMetadata packageMetadata = mock(PackageMetadata.class);
    when(packageFactory.getEntityType()).thenReturn(packageMetadata);
    packageMapper = new PackageMapper(packageFactory, dataService);
  }

  @Test
  void testPackageMapper() {
    assertThrows(NullPointerException.class, () -> new PackageMapper(null, null));
  }

  @Test
  void testToPackageReference() {
    String id = "id0";
    EditorPackageIdentifier packageIdentifier = EditorPackageIdentifier.create(id, "label");
    Package package_ = packageMapper.toPackageReference(packageIdentifier);
    assertEquals(id, package_.getIdValue());
  }

  @Test
  void testToPackageReferenceNull() {
    assertNull(packageMapper.toPackageReference(null));
  }

  @Test
  void testToEditorPackage() {
    String id = "id0";
    String label = "label0";
    Package package_ = mock(Package.class);
    when(package_.getId()).thenReturn(id);
    when(package_.getLabel()).thenReturn(label);
    EditorPackageIdentifier editorPackage = packageMapper.toEditorPackage(package_);
    assertEquals(create(id, label), editorPackage);
  }

  @Test
  void testToEditorPackageNull() {
    assertNull(packageMapper.toEditorPackage(null));
  }
}
