package org.molgenis.data.excel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.molgenis.data.AbstractMolgenisSpringTest;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisInvalidFormatException;
import org.molgenis.data.Repository;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.springframework.beans.factory.annotation.Autowired;

class ExcelRepositorySourceTest extends AbstractMolgenisSpringTest {
  @Autowired private EntityTypeFactory entityTypeFactory;

  @Autowired private AttributeFactory attrMetaFactory;

  private InputStream is;
  private ExcelRepositoryCollection excelRepositoryCollection;

  @BeforeEach
  void beforeMethod() throws MolgenisInvalidFormatException, IOException {
    is = getClass().getResourceAsStream("/test.xls");
    excelRepositoryCollection = new ExcelRepositoryCollection(is);
    excelRepositoryCollection.setEntityTypeFactory(entityTypeFactory);
    excelRepositoryCollection.setAttributeFactory(attrMetaFactory);
  }

  @AfterEach
  void afterMethod() throws IOException {
    is.close();
  }

  @Test
  void getNumberOfSheets() {
    assertEquals(3, excelRepositoryCollection.getNumberOfSheets());
  }

  @Test
  void getRepositories() {
    List<String> repositories = Lists.newArrayList(excelRepositoryCollection.getEntityTypeIds());
    assertNotNull(repositories);
    assertEquals(3, repositories.size());
  }

  @Test
  void getRepository() {
    Repository<Entity> test = excelRepositoryCollection.getRepository("test");
    assertNotNull(test);
    assertEquals("test", test.getName());

    Repository<Entity> blad2 = excelRepositoryCollection.getRepository("Blad2");
    assertNotNull(blad2);
    assertEquals("Blad2", blad2.getName());
  }
}
