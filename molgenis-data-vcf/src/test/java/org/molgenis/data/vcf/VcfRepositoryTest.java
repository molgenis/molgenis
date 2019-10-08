package org.molgenis.data.vcf;

import static com.google.common.collect.ImmutableList.of;
import static java.nio.file.Files.createTempFile;
import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.molgenis.data.meta.AttributeType.COMPOUND;
import static org.molgenis.data.meta.AttributeType.INT;
import static org.molgenis.data.meta.AttributeType.STRING;
import static org.molgenis.data.meta.AttributeType.TEXT;
import static org.molgenis.data.vcf.model.VcfAttributes.CHROM;
import static org.molgenis.data.vcf.model.VcfAttributes.POS;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.molgenis.data.AbstractMolgenisSpringTest;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.vcf.config.VcfTestConfig;
import org.molgenis.data.vcf.model.VcfAttributes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = {VcfRepositoryTest.Config.class})
class VcfRepositoryTest extends AbstractMolgenisSpringTest {
  @Autowired private VcfAttributes vcfAttrs;

  @Autowired private EntityTypeFactory entityTypeFactory;

  @Autowired private AttributeFactory attrMetaFactory;

  @Mock private Consumer<List<Entity>> batchConsumer;

  @Captor private ArgumentCaptor<List<Entity>> entityListCaptor;

  private static File testData;
  private static File testNoData;
  private static File testEmptyFile;

  @BeforeAll
  static void beforeClass() throws IOException {
    testData = new ClassPathResource("testdata.vcf").getFile();
    testNoData = new ClassPathResource("testnodata.vcf").getFile();
    testEmptyFile = createTempFile("empty", "vcf").toFile();
  }

  @AfterAll
  static void afterClass() {
    testEmptyFile.delete();
  }

  // Regression test for https://github.com/molgenis/molgenis/issues/6528
  @SuppressWarnings("deprecation")
  @Test
  void testCreateRepositoryForEmptyFile() {
    Exception exception =
        assertThrows(
            MolgenisDataException.class,
            () ->
                new VcfRepository(
                    testEmptyFile, "test", vcfAttrs, entityTypeFactory, attrMetaFactory));
    assertThat(exception.getMessage())
        .containsPattern(
            "Failed to read VCF Metadata from file; nested exception is java.io.IOException: missing column headers");
  }

  @Test
  void metaData() {
    VcfRepository vcfRepository =
        new VcfRepository(testData, "testData", vcfAttrs, entityTypeFactory, attrMetaFactory);

    assertEquals("testData", vcfRepository.getName());
    Iterator<Attribute> it = vcfRepository.getEntityType().getAttributes().iterator();
    assertTrue(it.hasNext());
    testAttribute(it.next(), VcfAttributes.CHROM, STRING);
    assertTrue(it.hasNext());
    // TEXT to handle large insertions/deletions
    testAttribute(it.next(), VcfAttributes.ALT, TEXT);
    assertTrue(it.hasNext());
    testAttribute(it.next(), POS, INT);
    assertTrue(it.hasNext());
    // TEXT to handle large insertions/deletions
    testAttribute(it.next(), VcfAttributes.REF, TEXT);
    assertTrue(it.hasNext());
    testAttribute(it.next(), VcfAttributes.FILTER, STRING);
    assertTrue(it.hasNext());
    testAttribute(it.next(), VcfAttributes.QUAL, STRING);
    assertTrue(it.hasNext());
    testAttribute(it.next(), VcfAttributes.ID, STRING);
    assertTrue(it.hasNext());
    testAttribute(it.next(), VcfAttributes.INTERNAL_ID, STRING);
    assertTrue(it.hasNext());
    testAttribute(it.next(), VcfAttributes.INFO, COMPOUND);
    assertTrue(it.hasNext());
  }

  private static void testAttribute(Attribute metadata, String name, AttributeType type) {
    assertEquals(name, metadata.getName());
    assertEquals(type, metadata.getDataType());
  }

  @Test
  void testForEachBatched() {
    VcfRepository vcfRepository =
        new VcfRepository(testData, "testData", vcfAttrs, entityTypeFactory, attrMetaFactory);

    // stream the file in batches to the batchConsumer
    vcfRepository.forEachBatched(batchConsumer, 5);

    // verify that the batchConsumer was called with two batches and send the batches to the captor
    verify(batchConsumer, times(2)).accept(entityListCaptor.capture());

    // get the batch values from the captor
    List<List<Entity>> allValues = entityListCaptor.getAllValues();

    List<List<Integer>> positions =
        allValues.stream()
            .map(
                batch ->
                    batch.stream().map(entity -> entity.getInt(POS)).collect(Collectors.toList()))
            .collect(Collectors.toList());
    assertEquals(
        of(of(565286, 2243618, 3171929, 3172062, 3172273), of(6097450, 7569187)), positions);

    Set<String> chroms =
        allValues.stream()
            .flatMap(batch -> batch.stream().map(entity -> entity.getString(CHROM)))
            .collect(Collectors.toSet());
    assertEquals(singleton("1"), chroms);
  }

  @Test
  void iterator_noValues() {
    VcfRepository vcfRepository =
        new VcfRepository(testNoData, "testNoData", vcfAttrs, entityTypeFactory, attrMetaFactory);
    vcfRepository.forEachBatched(batchConsumer, 1000);

    verifyZeroInteractions(batchConsumer);
  }

  @Configuration
  @Import({VcfTestConfig.class})
  static class Config {}
}
