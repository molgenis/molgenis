package org.molgenis.semanticmapper.algorithmgenerator.categorymapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.molgenis.semanticmapper.algorithmgenerator.bean.Category;
import org.molgenis.semanticmapper.algorithmgenerator.rules.impl.NegativeCategoryRule;
import org.molgenis.semanticmapper.algorithmgenerator.rules.impl.PositiveCategoryRule;

public class LexicalCategoryMapperTest {
  LexicalCategoryMapper lexicalCategoryMapper =
      new LexicalCategoryMapper(
          Arrays.asList(new PositiveCategoryRule(), new NegativeCategoryRule()));

  @Test
  public void findBestCategoryMatch() {
    Category maleCategory = Category.create("0", "MALE");
    Category femaleCategory = Category.create("1", "FEMALE");
    List<Category> targetCategories1 = Lists.newArrayList(maleCategory, femaleCategory);
    assertEquals(
        lexicalCategoryMapper.findBestCategoryMatch(
            Category.create("1", "male"), targetCategories1),
        maleCategory);
    assertEquals(
        lexicalCategoryMapper.findBestCategoryMatch(
            Category.create("2", "female"), targetCategories1),
        femaleCategory);

    Category neverStrokeCategory = Category.create("0", "Never had stroke");
    Category hasStrokeCategory = Category.create("1", "Has has stroke");
    List<Category> targetCategories2 = Lists.newArrayList(neverStrokeCategory, hasStrokeCategory);
    assertEquals(
        lexicalCategoryMapper.findBestCategoryMatch(Category.create("1", "no"), targetCategories2),
        neverStrokeCategory);
    assertEquals(
        lexicalCategoryMapper.findBestCategoryMatch(Category.create("1", "yes"), targetCategories2),
        hasStrokeCategory);
  }
}
