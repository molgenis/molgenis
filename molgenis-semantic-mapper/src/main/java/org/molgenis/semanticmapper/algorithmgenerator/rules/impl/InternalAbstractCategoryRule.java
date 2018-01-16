package org.molgenis.semanticmapper.algorithmgenerator.rules.impl;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.molgenis.semanticmapper.algorithmgenerator.bean.Category;
import org.molgenis.semanticmapper.algorithmgenerator.rules.CategoryMatchQuality;
import org.molgenis.semanticmapper.algorithmgenerator.rules.CategoryRule;
import org.molgenis.semanticmapper.algorithmgenerator.rules.quality.Quality;
import org.molgenis.semanticmapper.algorithmgenerator.rules.quality.impl.NumericQuality;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

public abstract class InternalAbstractCategoryRule implements CategoryRule
{
	private static final Splitter TERM_SPLITTER = Splitter.onPattern("\\s+");
	private static final String ILLEGAL_CHARS_REGEX = "[^a-zA-Z0-9]";
	private final List<String> words;

	public InternalAbstractCategoryRule(Set<String> words)
	{
		this.words = Lists.newArrayList(requireNonNull(words));
		sortBasedOnLength(this.words);
	}

	@Override
	public CategoryMatchQuality<Double> createCategoryMatchQuality(Category targetCategory, Category sourceCategory)
	{
		String matchedTermForTargetLabel = getMatchedTermFromTheRulelabelContainsWords(targetCategory.getLabel());
		String matchedTermForSourceLabel = getMatchedTermFromTheRulelabelContainsWords(sourceCategory.getLabel());

		boolean ruleApplied =
				StringUtils.isNotBlank(matchedTermForTargetLabel) && StringUtils.isNotBlank(matchedTermForSourceLabel);

		Quality<Double> quality = NumericQuality.create(
				createNumericQualityIndicator(matchedTermForTargetLabel, matchedTermForSourceLabel));

		return CategoryMatchQuality.create(ruleApplied, quality, targetCategory, sourceCategory);
	}

	private double createNumericQualityIndicator(String matchedTermForTargetLabel, String matchedTermForSourceLabel)
	{
		return (double) matchedTermForTargetLabel.length() * matchedTermForSourceLabel.length();
	}

	protected String getMatchedTermFromTheRulelabelContainsWords(String label)
	{
		if (StringUtils.isNotBlank(label))
		{
			Set<String> tokens = split(label);
			return words.stream().filter(word -> tokens.containsAll(split(word))).findFirst().orElse(StringUtils.EMPTY);
		}
		return StringUtils.EMPTY;
	}

	protected Set<String> split(String label)
	{
		return Sets.newHashSet(TERM_SPLITTER.split(label.toLowerCase()))
				   .stream()
				   .map(this::removeIllegalChars)
				   .collect(Collectors.toSet());
	}

	protected String removeIllegalChars(String string)
	{
		return string.replaceAll(ILLEGAL_CHARS_REGEX, StringUtils.EMPTY);
	}

	private void sortBasedOnLength(List<String> words)
	{
		words.sort(Comparator.comparingInt(String::length));
	}
}
