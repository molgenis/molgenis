package org.molgenis.data.meta.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.data.config.MetadataTestConfig;
import org.molgenis.data.meta.AbstractEntityFactoryTest;
import org.molgenis.security.core.model.PackageValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(
    classes = {
      EntityBaseTestConfig.class,
      PackageMetadata.class,
      PackageFactory.class,
      MetadataTestConfig.class
    })
class PackageFactoryTest extends AbstractEntityFactoryTest {

  @Autowired PackageFactory factory;

  @Override
  @Test
  public void testCreate() {
    super.testCreate(factory, Package.class);
  }

  @Override
  @Test
  public void testCreateWithId() {
    super.testCreateWithId(factory, Package.class);
  }

  @Override
  @Test
  public void testCreateWithEntity() {
    super.testCreateWithEntity(factory, Package.class);
  }

  @Test
  void testCreatePackageValue() {
    PackageValue packageValue =
        PackageValue.builder()
            .setName("name")
            .setLabel("label")
            .setDescription("description")
            .build();

    Package actual = factory.create(packageValue);

    assertEquals("name", actual.getId());
    assertEquals("label", actual.getLabel());
    assertEquals("description", actual.getDescription());
  }

  @Test
  void testCreatePackageValueNulls() {
    PackageValue packageValue = PackageValue.builder().setName("name").setLabel("label").build();

    Package actual = factory.create(packageValue);

    assertEquals("name", actual.getId());
    assertEquals("label", actual.getLabel());
    assertNull(actual.getDescription());
  }
}
