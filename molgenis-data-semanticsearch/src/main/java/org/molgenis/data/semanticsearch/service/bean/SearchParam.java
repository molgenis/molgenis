package org.molgenis.data.semanticsearch.service.bean;

import com.google.auto.value.AutoValue;
import org.molgenis.gson.AutoGson;

import java.util.List;
import java.util.Set;

@AutoValue
@AutoGson(autoValueClass = AutoValue_SearchParam.class)
public abstract class SearchParam
{
	public final static float DEFAULT_HIGH_QUALITY_THRESHOLD = 0.8f;

	public abstract Set<String> getLexicalQueries();

	public abstract List<TagGroup> getTagGroups();

	public abstract float getHighQualityThreshold();

	public abstract boolean isStrictMatch();

	public static SearchParam create(Set<String> lexicalQueries, List<TagGroup> tagGroups)
	{
		return new AutoValue_SearchParam(lexicalQueries, tagGroups, DEFAULT_HIGH_QUALITY_THRESHOLD, false);
	}

	public static SearchParam create(Set<String> lexicalQueries, List<TagGroup> tagGroups, boolean strictMatch)
	{
		return new AutoValue_SearchParam(lexicalQueries, tagGroups, DEFAULT_HIGH_QUALITY_THRESHOLD, strictMatch);
	}

	public static SearchParam create(Set<String> lexicalQueries, List<TagGroup> tagGroups, float highQualityThreshold)
	{
		return new AutoValue_SearchParam(lexicalQueries, tagGroups, highQualityThreshold, false);
	}
}
