package org.molgenis.semanticmapper.algorithmgenerator.categorymapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.molgenis.semanticmapper.algorithmgenerator.bean.Category.create;

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
        maleCategory,
        lexicalCategoryMapper.findBestCategoryMatch(create("1", "male"), targetCategories1));
    assertEquals(
        femaleCategory,
        lexicalCategoryMapper.findBestCategoryMatch(create("2", "female"), targetCategories1));

    Category neverStrokeCategory = Category.create("0", "Never had stroke");
    Category hasStrokeCategory = Category.create("1", "Has has stroke");
    List<Category> targetCategories2 = Lists.newArrayList(neverStrokeCategory, hasStrokeCategory);
    assertEquals(
        neverStrokeCategory,
        lexicalCategoryMapper.findBestCategoryMatch(create("1", "no"), targetCategories2));
    assertEquals(
        hasStrokeCategory,
        lexicalCategoryMapper.findBestCategoryMatch(create("1", "yes"), targetCategories2));
  }
}
