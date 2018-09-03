package org.molgenis.data.meta.model;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.molgenis.security.core.model.PackageValue;
import org.springframework.stereotype.Component;

@Component
public class PackageFactory extends AbstractSystemEntityFactory<Package, PackageMetadata, String> {
  PackageFactory(PackageMetadata packageMetadata, EntityPopulator entityPopulator) {
    super(Package.class, packageMetadata, entityPopulator);
  }

  public Package create(String id, String description) {
    Package pack = create(id);
    pack.setLabel(id);
    pack.setDescription(description);
    return pack;
  }

  @Override
  public Package create(String id) {
    Package pack = create();
    pack.setId(id);
    pack.setLabel(id);
    return pack;
  }

  public Package create(String id, String description, Package parentPackage) {
    Package pack = create(id);
    pack.setLabel(id);
    pack.setDescription(description);
    pack.setParent(parentPackage);
    return pack;
  }

  public Package create(PackageValue packageValue) {
    Package result = create(packageValue.getName());
    result.setLabel(packageValue.getLabel());
    result.setDescription(packageValue.getDescription());
    return result;
  }
}
