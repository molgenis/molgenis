package org.molgenis.ontology.utils;

import static org.molgenis.ontology.beans.ComparableEntity.MAX_SCORE_FIELD;
import static org.molgenis.ontology.beans.ComparableEntity.SCORE;
import static org.molgenis.ontology.repository.AbstractOntologyRepository.ILLEGAL_CHARACTERS_PATTERN;
import static org.molgenis.ontology.repository.AbstractOntologyRepository.ONTOLOGY_TERM_IRI;
import static org.molgenis.ontology.repository.AbstractOntologyRepository.SINGLE_WHITESPACE;
import static org.molgenis.ontology.repository.AbstractOntologyRepository.SYNONYMS;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.molgenis.data.Entity;
import org.molgenis.ontology.beans.ComparableEntity;

/**
 * An algorithm container to deal with the case where matching term is mapped to multiple synonyms of the same ontology
 * term at the same time leading to a low final score. E.g. protruding eyeball (exophthalmos, proptosis) mapped to
 * 'protruding eye' by 30%, 'exophthalmos' by 30% and 'proptosis' by 26%. For such cases, the score should be
 * re-calculated by combining multiple synonyms together in order to give a fair score
 * 
 * @author chaopang
 *
 */
public class PostProcessOntologyTermCombineSynonymAlgorithm
{
	public static void process(List<ComparableEntity> comparableEntities, Map<String, Object> inputData)
	{
		List<ComparableEntity> sortedEntities = new ArrayList<ComparableEntity>(comparableEntities);
		Collections.sort(sortedEntities);
		for (int i = 0; i < sortedEntities.size(); i++)
		{
			Entity currentEntity = sortedEntities.get(i);
			String currentMaxScoreField = currentEntity.getString(MAX_SCORE_FIELD);
			String currentEntityIdentifier = currentEntity.getString(ONTOLOGY_TERM_IRI);
			String currentSynonym = currentEntity.getString(SYNONYMS);
			Double firstScore = Double.parseDouble(currentEntity.get(SCORE).toString());
			String inputDataQuery = inputData.get(currentMaxScoreField).toString();

			StringBuilder combinedSynonym = new StringBuilder();
			combinedSynonym.append(currentSynonym);

			for (int j = i + 1; j < sortedEntities.size(); j++)
			{
				ComparableEntity nextEntity = sortedEntities.get(j);
				String nextMaxScoreField = nextEntity.getString(MAX_SCORE_FIELD);
				String nextEntityIdentifier = nextEntity.getString(ONTOLOGY_TERM_IRI);

				if (currentEntityIdentifier.equals(nextEntityIdentifier)
						&& currentMaxScoreField.equals(nextMaxScoreField))
				{
					String nextSynonym = nextEntity.getString(SYNONYMS);
					if (!combinedSynonym.toString().contains(nextSynonym))
					{
						StringBuilder tempCombinedSynonym = new StringBuilder().append(combinedSynonym)
								.append(SINGLE_WHITESPACE).append(nextSynonym);
						Double newScore = NGramMatchingModel.stringMatching(inputDataQuery.replaceAll(
								ILLEGAL_CHARACTERS_PATTERN, SINGLE_WHITESPACE), tempCombinedSynonym.toString()
								.replaceAll(ILLEGAL_CHARACTERS_PATTERN, SINGLE_WHITESPACE));
						if (newScore.intValue() > firstScore.intValue())
						{
							firstScore = newScore;
							currentEntity.set(SCORE, newScore);
							combinedSynonym.delete(0, combinedSynonym.length()).append(tempCombinedSynonym);
							comparableEntities.remove(nextEntity);
						}
					}
				}
			}
		}
	}
}