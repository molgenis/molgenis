package org.molgenis.data.meta.model;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.meta.AttributeType.XREF;
import static org.testng.Assert.assertEquals;

import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.data.config.MetadataTestConfig;
import org.molgenis.data.meta.AbstractSystemEntityTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(
    classes = {
      EntityBaseTestConfig.class,
      PackageMetadata.class,
      PackageFactory.class,
      MetadataTestConfig.class
    })
public class PackageTest extends AbstractSystemEntityTest {

  @Autowired PackageMetadata metadata;
  @Autowired PackageFactory factory;

  @Test
  public void testSystemEntity() {
    internalTestAttributes(
        metadata,
        java.lang.Package.class,
        factory,
        getOverriddenReturnTypes(),
        getExcludedAttrs(),
        true);
  }

  @Test
  public void getRootPackageNoParent() throws Exception {
    PackageMetadata packageMetadata = mock(PackageMetadata.class);
    Package package_ = new Package(packageMetadata);
    assertEquals(package_.getRootPackage(), package_);
  }

  @Test
  public void getRootPackageParent() throws Exception {
    PackageMetadata packageMetadata = mock(PackageMetadata.class);
    Attribute parentAttr = when(mock(Attribute.class).getDataType()).thenReturn(XREF).getMock();
    when(packageMetadata.getAttribute(PackageMetadata.PARENT)).thenReturn(parentAttr);
    Package grandParentPackage = new Package(packageMetadata);
    Package parentParent = new Package(packageMetadata);
    parentParent.setParent(grandParentPackage);
    Package package_ = new Package(packageMetadata);
    package_.setParent(parentParent);
    assertEquals(package_.getRootPackage(), grandParentPackage);
  }
}
