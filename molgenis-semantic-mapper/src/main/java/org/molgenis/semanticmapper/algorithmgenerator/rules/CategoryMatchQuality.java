package org.molgenis.semanticmapper.algorithmgenerator.rules;

import com.google.auto.value.AutoValue;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.molgenis.gson.AutoGson;
import org.molgenis.semanticmapper.algorithmgenerator.bean.Category;
import org.molgenis.semanticmapper.algorithmgenerator.rules.quality.Quality;

@AutoValue
@AutoGson(autoValueClass = AutoValue_CategoryMatchQuality.class)
@SuppressWarnings("java:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class CategoryMatchQuality<T> implements Comparable<CategoryMatchQuality<T>> {
  public abstract boolean isRuleApplied();

  @Nullable
  @CheckForNull
  public abstract Quality<T> getQuality();

  public abstract Category getTargetCategory();

  public abstract Category getSourceCategory();

  public static <T> CategoryMatchQuality<T> create(
      boolean ruleApplied, Quality<T> quality, Category targetCategory, Category sourceCategory) {
    return new AutoValue_CategoryMatchQuality<>(
        ruleApplied, quality, targetCategory, sourceCategory);
  }

  @Override
  public int compareTo(CategoryMatchQuality<T> categoryMatchQuality) {
    Quality<T> quality = getQuality();
    Quality<T> otherQuality = categoryMatchQuality.getQuality();
    if (quality == null || otherQuality == null) {
      return 0;
    }
    return quality.compareTo(otherQuality);
  }
}
