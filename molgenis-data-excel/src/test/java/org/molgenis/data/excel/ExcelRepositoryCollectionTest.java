package org.molgenis.data.excel;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.InputStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.test.AbstractMockitoTest;

class ExcelRepositoryCollectionTest extends AbstractMockitoTest {
  @Mock private EntityTypeFactory entityTypeFactory;
  @Mock private AttributeFactory attributeFactory;
  private ExcelRepositoryCollection excelRepositoryCollection;
  private InputStream inputStream;

  @BeforeEach
  void setUpBeforeEach() throws IOException {
    String fileName = "/test.xlsx";
    inputStream = getClass().getResourceAsStream(fileName);
    excelRepositoryCollection = new ExcelRepositoryCollection(inputStream);
    excelRepositoryCollection.setEntityTypeFactory(entityTypeFactory);
    excelRepositoryCollection.setAttributeFactory(attributeFactory);
  }

  @Test
  void testGetRepository() {
    assertNotNull(excelRepositoryCollection.getRepository("attributes"));
  }

  @Test
  void testGetRepositoryNotExists() {
    assertNull(excelRepositoryCollection.getRepository("blaat"));
  }

  @Test
  void testGetSheet() {
    assertNotNull(excelRepositoryCollection.getSheet(0));
  }

  @Test
  void testGetSheetNotExists() {
    assertThrows(IllegalArgumentException.class, () -> excelRepositoryCollection.getSheet(123));
  }

  @AfterEach
  void tearDownAfterEach() throws IOException {
    inputStream.close();
  }
}
