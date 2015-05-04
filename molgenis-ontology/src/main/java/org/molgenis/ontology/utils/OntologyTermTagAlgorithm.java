package org.molgenis.ontology.utils;


public class OntologyTermTagAlgorithm
{
	private static final String ILLEGAL_CHARACTERS_PATTERN = "[^a-zA-Z0-9 ]";
	private static final String SINGLE_WHITESPACE = " ";

	// /**
	// * A helper function to calculate the best NGram score from a list ontologyTerm synonyms
	// *
	// * @param queryString
	// * @param ontologyTermEntity
	// * @return
	// */
	// private Iterable<Entity> calculateNGramOTSynonyms(String queryString, Iterable<Entity> ontologyterms)
	// {
	// if (Iterables.size(ontologyterms) > 0)
	// {
	// String cleanedQueryString = removeIllegalCharWithSingleWhiteSpace(queryString);
	//
	// // Calculate the Ngram silmiarity score for all the synonyms and sort them in descending order
	// List<MapEntity> synonymEntities = FluentIterable.from(ontologyterms)
	// .transform(new Function<Entity, MapEntity>()
	// {
	// public MapEntity apply(Entity ontologyTerm)
	// {
	//
	// List<Entity> ontologyTermSynonyms = new ArrayList<Entity>();
	// for (Entity ontologyTermSynonymEntity : ontologyTerm
	// .getEntities(OntologyTermSynonymMetaData.ONTOLOGY_TERM_SYNONYM))
	// {
	// String synonym = ontologyTermSynonymEntity
	// .getString(OntologyTermSynonymMetaData.ONTOLOGY_TERM_SYNONYM);
	// double score = NGramMatchingModel.stringMatching(cleanedQueryString, synonym);
	// MapEntity entity = new MapEntity(ontologyTermSynonymEntity);
	// entity.set("score", score);
	// ontologyTermSynonyms.add(entity);
	// }
	//
	// ontologyTermSynonyms.stream().max(new Comparator<Entity>()
	// {
	// public int compare(Entity o1, Entity o2)
	// {
	// // o1.get
	// }
	// });
	//
	// // Lists.newArrayList(ontologyTermSynonyms).stream().max(new Comparator<Entity>()
	// // {
	// // public int compare(Entity o1, Entity o2)
	// // {
	// // return 0;
	// // }
	// // })
	//
	// // MapEntity mapEntity = new MapEntity(ontologyTermSynonymEntity);
	// // String ontologyTermSynonym =
	// // removeIllegalCharWithSingleWhiteSpace(ontologyTermSynonymEntity
	// // .getString(OntologyTermSynonymMetaData.ONTOLOGY_TERM_SYNONYM));
	// // mapEntity.set(SCORE, NGramMatchingModel.stringMatching(cleanedQueryString,
	// // ontologyTermSynonym));
	// return mapEntity;
	// }
	//
	// }).toSortedList(new Comparator<MapEntity>()
	// {
	// public int compare(MapEntity entity_1, MapEntity entity_2)
	// {
	// return entity_2.getDouble(SCORE).compareTo(entity_1.getDouble(SCORE));
	// }
	// });
	//
	// MapEntity firstMatchedSynonymEntity = Iterables.getFirst(synonymEntities, new MapEntity());
	// double topNgramScore = firstMatchedSynonymEntity.getDouble(SCORE);
	// String topMatchedSynonym = firstMatchedSynonymEntity
	// .getString(OntologyTermSynonymMetaData.ONTOLOGY_TERM_SYNONYM);
	//
	// // the algorithm to combine synonyms to re-calculate the similarity scores to deal with the case where the
	// // input query string contains multiple words from different synonyms of the same ontology term. E.g.
	// // query string "propotosis, protruding eyeball, Exophthalmos" contains three synonyms of OT (propotosis),
	// // if it was matched to each of the synonyms, all the similarity score would be fairly low (25%), therefore
	// // need to combine those synonyms to recalculate the similarity score.
	// //
	// // The idea of the algorithm is quite simple, we add up the current synonym (the most) and next synonym (the
	// // second most), if the combined string yields a higher score, the synonyms will be combined together. The
	// // same process is repeated until all the synonyms have been checked
	// // A --> 30%
	// // B --> 25%
	// // C --> 20%
	// //
	// // if(score(a+b, query) > score(a)) combine
	// // else move to next synonym
	// for (Entity nextMatchedSynonymEntity : Iterables.skip(synonymEntities, 1))
	// {
	// String nextMatchedSynonym = nextMatchedSynonymEntity
	// .getString(OntologyTermSynonymMetaData.ONTOLOGY_TERM_SYNONYM);
	//
	// StringBuilder tempCombinedSynonym = new StringBuilder();
	// tempCombinedSynonym.append(topMatchedSynonym).append(SINGLE_WHITESPACE).append(nextMatchedSynonym);
	//
	// double newScore = NGramMatchingModel.stringMatching(cleanedQueryString,
	// removeIllegalCharWithSingleWhiteSpace(tempCombinedSynonym.toString()));
	//
	// if (newScore > topNgramScore)
	// {
	// topNgramScore = newScore;
	// topMatchedSynonym = tempCombinedSynonym.toString();
	// }
	// }
	//
	// firstMatchedSynonymEntity.set(OntologyTermSynonymMetaData.ONTOLOGY_TERM_SYNONYM, topMatchedSynonym);
	// firstMatchedSynonymEntity.set(SCORE, topNgramScore);
	// firstMatchedSynonymEntity.set(COMBINED_SCORE, topNgramScore);
	//
	// // The similarity scores are adjusted based on the inverse document frequency of the words.
	// // The idea is that all the words from query string are weighted (important words occur fewer times across
	// // all ontology terms than common words), the final score should be compensated for according to the word
	// // weight.
	// Map<String, Double> weightedWordSimilarity = informationContentService.redistributedNGramScore(
	// cleanedQueryString, ontologyIri);
	//
	// Set<String> synonymStemmedWords = informationContentService.createStemmedWordSet(topMatchedSynonym);
	//
	// Set<String> createStemmedWordSet = informationContentService.createStemmedWordSet(cleanedQueryString);
	//
	// createStemmedWordSet
	// .stream()
	// .filter(originalWord -> Iterables.contains(synonymStemmedWords, originalWord)
	// && weightedWordSimilarity.containsKey(originalWord))
	// .forEach(
	// word -> firstMatchedSynonymEntity.set(COMBINED_SCORE, (firstMatchedSynonymEntity
	// .getDouble(COMBINED_SCORE) + weightedWordSimilarity.get(word))));
	// return firstMatchedSynonymEntity;
	// }
	//
	// return null;
	// }

	public String removeIllegalCharWithSingleWhiteSpace(String string)
	{
		return string.replaceAll(ILLEGAL_CHARACTERS_PATTERN, SINGLE_WHITESPACE);
	}
}
