package org.molgenis.data.mapper.algorithmgenerator.rules;

import com.google.auto.value.AutoValue;
import org.molgenis.data.mapper.algorithmgenerator.bean.Category;
import org.molgenis.data.mapper.algorithmgenerator.rules.quality.Quality;
import org.molgenis.gson.AutoGson;

import javax.annotation.Nullable;

@AutoValue
@AutoGson(autoValueClass = AutoValue_CategoryMatchQuality.class)
public abstract class CategoryMatchQuality<T> implements Comparable<CategoryMatchQuality<T>>
{
	public abstract boolean isRuleApplied();

	@Nullable
	public abstract Quality<T> getQuality();

	public abstract Category getTargetCategory();

	public abstract Category getSourceCategory();

	public static <T> CategoryMatchQuality<T> create(boolean ruleApplied, Quality<T> quality, Category targetCategory,
			Category sourceCategory)
	{
		return new AutoValue_CategoryMatchQuality<T>(ruleApplied, quality, targetCategory, sourceCategory);
	}

	@Override
	public int compareTo(CategoryMatchQuality<T> categoryMatchQuality)
	{
		return getQuality().compareTo(categoryMatchQuality.getQuality());
	}
}