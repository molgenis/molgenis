package org.molgenis.data.vcf;

import static com.google.common.collect.Iterators.partition;
import static com.google.common.collect.Iterators.transform;
import static com.google.common.collect.Streams.stream;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.RepositoryCapability;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.support.AbstractRepository;
import org.molgenis.data.vcf.format.VcfToEntity;
import org.molgenis.data.vcf.model.VcfAttributes;
import org.molgenis.vcf.VcfReader;
import org.molgenis.vcf.meta.VcfMeta;

/**
 * Repository implementation for vcf files.
 *
 * <p>The filename without the extension is considered to be the entityname
 */
public class VcfRepository extends AbstractRepository {
  public static final String DEFAULT_ATTRIBUTE_DESCRIPTION = "Description not provided";

  public static final String NAME = "NAME";
  public static final String ORIGINAL_NAME = "ORIGINAL_NAME";
  public static final String PREFIX = "##";

  public static final int BATCH_SIZE = 1000;
  private final String entityTypeId;
  private final VcfAttributes vcfAttributes;
  private final EntityTypeFactory entityTypeFactory;
  private final AttributeFactory attrMetaFactory;
  private VcfToEntity vcfToEntity;
  private final File file;

  VcfRepository(
      File file,
      String entityTypeId,
      VcfAttributes vcfAttributes,
      EntityTypeFactory entityTypeFactory,
      AttributeFactory attrMetaFactory) {
    this.file = requireNonNull(file);
    this.entityTypeId = requireNonNull(entityTypeId);
    this.vcfAttributes = requireNonNull(vcfAttributes);
    this.entityTypeFactory = requireNonNull(entityTypeFactory);
    this.attrMetaFactory = requireNonNull(attrMetaFactory);
    parseVcfMeta();
  }

  private void parseVcfMeta() {
    withReader(
        reader -> {
          try {
            VcfMeta vcfMeta = reader.getVcfMeta();
            vcfToEntity =
                new VcfToEntity(
                    entityTypeId, vcfMeta, vcfAttributes, entityTypeFactory, attrMetaFactory);
          } catch (IOException | RuntimeException e) {
            throw new MolgenisDataException("Failed to read VCF Metadata from file", e);
          }
        });
  }

  @Override
  public Iterator<Entity> iterator() {
    throw new UnsupportedOperationException("Use forEachBatched instead of iterator");
  }

  public EntityType getEntityType() {
    return vcfToEntity.getEntityType();
  }

  @Override
  public Set<RepositoryCapability> getCapabilities() {
    return Collections.emptySet();
  }

  @Override
  public long count() {
    AtomicInteger counter = new AtomicInteger(0);
    forEachBatched(batch -> counter.addAndGet(batch.size()), BATCH_SIZE);
    return counter.get();
  }

  @Override
  public void forEachBatched(Consumer<List<Entity>> consumer, int batchSize) {
    withReader(
        reader ->
            stream(partition(transform(reader.iterator(), vcfToEntity::toEntity), batchSize))
                .forEach(consumer));
  }

  private void withReader(Consumer<VcfReader> consumer) {
    withInputStream(
        inputStream -> {
          try (VcfReader reader = new VcfReader(new InputStreamReader(inputStream, UTF_8))) {
            consumer.accept(reader);
          } catch (IOException e) {
            throw new MolgenisDataException(
                "Failed to create VCF Reader for file" + file.getAbsolutePath(), e);
          }
        });
  }

  private void withInputStream(Consumer<InputStream> consumer) {
    try {
      if (file.getName().endsWith(".gz")) {
        consumer.accept(new GZIPInputStream(new FileInputStream(file)));
      } else if (file.getName().endsWith(".zip")) {
        try (ZipFile zipFile = new ZipFile(file.getPath())) {
          Enumeration<? extends ZipEntry> e = zipFile.entries();
          ZipEntry entry = e.nextElement(); // your only file
          consumer.accept(zipFile.getInputStream(entry));
        }
      } else {
        consumer.accept(new FileInputStream(file));
      }
    } catch (IOException e) {
      throw new MolgenisDataException(
          "Failed to create InputStream for file" + file.getAbsolutePath(), e);
    }
  }
}
