package org.molgenis.data.export.mapper;

import static java.util.stream.Collectors.joining;
import static org.molgenis.data.importer.emx.EmxMetadataParser.EMX_PACKAGE_DESCRIPTION;
import static org.molgenis.data.importer.emx.EmxMetadataParser.EMX_PACKAGE_LABEL;
import static org.molgenis.data.importer.emx.EmxMetadataParser.EMX_PACKAGE_NAME;
import static org.molgenis.data.importer.emx.EmxMetadataParser.EMX_PACKAGE_PARENT;
import static org.molgenis.data.importer.emx.EmxMetadataParser.EMX_PACKAGE_TAGS;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Streams;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.meta.model.PackageMetadata;
import org.molgenis.data.meta.model.Tag;

public class PackageMapper {

  public static final ImmutableMap<String, String> PACKAGE_ATTRS;

  static {
    PACKAGE_ATTRS =
        ImmutableMap.<String, String>builder()
            .put(EMX_PACKAGE_NAME, PackageMetadata.ID)
            .put(EMX_PACKAGE_LABEL, PackageMetadata.LABEL)
            .put(EMX_PACKAGE_DESCRIPTION, PackageMetadata.DESCRIPTION)
            .put(EMX_PACKAGE_PARENT, PackageMetadata.PARENT)
            .put(EMX_PACKAGE_TAGS, PackageMetadata.TAGS)
            .build();
  }

  private PackageMapper() {}

  public static List<Object> map(Package pack) {
    List<Object> row = new ArrayList<>(PACKAGE_ATTRS.size());
    for (Entry<String, String> entry : PACKAGE_ATTRS.entrySet()) {
      switch (entry.getKey()) {
        case EMX_PACKAGE_TAGS:
          row.add(Streams.stream(pack.getTags()).map(Tag::getId).collect(joining(",")));
          break;
        case EMX_PACKAGE_PARENT:
          Package parent = pack.getParent();
          row.add(parent != null ? parent.getId() : null);
          break;
        default:
          Object value = pack.get(entry.getValue());
          row.add(value != null ? value.toString() : null);
      }
    }
    return row;
  }
}
