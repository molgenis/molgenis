package org.molgenis.data.semanticsearch.explain.bean;

import com.google.auto.value.AutoValue;
import org.molgenis.gson.AutoGson;

import java.util.List;

import static java.util.Collections.emptyList;

@AutoValue
@AutoGson(autoValueClass = AutoValue_ExplainedMatchCandidate.class)
public abstract class ExplainedMatchCandidate<CandidateType>
{
	public static <CandidateType> ExplainedMatchCandidate<CandidateType> create(CandidateType match)
	{
		return new AutoValue_ExplainedMatchCandidate<>(match, emptyList(), false);
	}

	public static <CandidateType> ExplainedMatchCandidate<CandidateType> create(CandidateType match,
			List<ExplainedQueryString> explainedQueryStrings, boolean highQuality)
	{
		return new AutoValue_ExplainedMatchCandidate<CandidateType>(match, explainedQueryStrings, highQuality);
	}

	public abstract CandidateType getMatch();

	public abstract List<ExplainedQueryString> getExplainedQueryStrings();

	public abstract boolean isHighQuality();
}