package org.molgenis.data.meta.model;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import org.molgenis.data.AbstractMolgenisSpringTest;
import org.molgenis.data.populate.EntityPopulator;
import org.molgenis.security.core.model.PackageValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(
    classes = {PackageFactory.class, PackageMetadata.class, EntityPopulator.class})
public class PackageFactoryTest extends AbstractMolgenisSpringTest {
  @Autowired PackageFactory packageFactory;

  @Test
  public void testCreatePackageValue() {
    PackageValue packageValue =
        PackageValue.builder()
            .setName("name")
            .setLabel("label")
            .setDescription("description")
            .build();

    Package actual = packageFactory.create(packageValue);

    assertEquals(actual.getId(), "name");
    assertEquals(actual.getLabel(), "label");
    assertEquals(actual.getDescription(), "description");
  }

  @Test
  public void testCreatePackageValueNulls() {
    PackageValue packageValue = PackageValue.builder().setName("name").setLabel("label").build();

    Package actual = packageFactory.create(packageValue);

    assertEquals(actual.getId(), "name");
    assertEquals(actual.getLabel(), "label");
    assertNull(actual.getDescription());
  }
}
