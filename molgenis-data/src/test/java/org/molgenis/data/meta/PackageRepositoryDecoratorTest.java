package org.molgenis.data.meta;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class PackageRepositoryDecoratorTest {
  @Test
  void testPackageRepositoryDecorator() {
    assertThrows(NullPointerException.class, () -> new PackageRepositoryDecorator(null, null));
  }
}
