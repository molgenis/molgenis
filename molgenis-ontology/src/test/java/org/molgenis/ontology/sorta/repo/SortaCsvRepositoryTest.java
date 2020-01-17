package org.molgenis.ontology.sorta.repo;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.molgenis.data.AbstractMolgenisSpringTest;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = {SortaCsvRepositoryTest.Config.class})
class SortaCsvRepositoryTest extends AbstractMolgenisSpringTest {
  private File file;
  @Autowired private EntityTypeFactory entityTypeFactory;
  @Autowired private AttributeFactory attributeFactory;
  private SortaCsvRepository sortaCsvRepository;

  @BeforeEach
  public void setUpBeforeEach() throws IOException {
    file = File.createTempFile("SortaCsvRepositoryTest_", ".csv");
    file.deleteOnExit(); // note: exception deleting file in AfterEach
    sortaCsvRepository = new SortaCsvRepository(file, entityTypeFactory, attributeFactory);
  }

  @Test
  void iterator() throws IOException {
    FileUtils.writeLines(file, asList("Name", "whisper", "SCREAM"));
    Iterator<Entity> it = sortaCsvRepository.iterator();
    assertAll(
        () -> assertTrue(it.hasNext()),
        () -> assertEquals("whisper", it.next().getString("Name")),
        () -> assertTrue(it.hasNext()),
        () -> assertEquals("scream", it.next().getString("Name")),
        () -> assertFalse(it.hasNext()));
  }
}
