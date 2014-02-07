package org.molgenis.omx.protocol;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Countable;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.support.AbstractRepository;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.omx.observ.Category;
import org.molgenis.omx.observ.Protocol;
import org.molgenis.omx.utils.I18nTools;

import com.google.common.collect.Iterables;

public class CategoryRepository extends AbstractRepository implements Countable
{
	private static final String FIELD_TYPE = "type";
	private static final String FIELD_ID = "id";
	private static final String FIELD_NAME = "name";
	private static final String FIELD_DESCRIPTION = "description";
	private static final String FIELD_DESCRIPTION_STOPWORDS = "descriptionStopwords";

	private final Protocol protocol;
	private final DataService dataService;
	private final Integer id;
	public static final Set<String> STOPWORDSLIST;
	static
	{
		STOPWORDSLIST = new HashSet<String>(Arrays.asList("a", "you", "about", "above", "after", "again", "against",
				"all", "am", "an", "and", "any", "are", "aren't", "as", "at", "be", "because", "been", "before",
				"being", "below", "between", "both", "but", "by", "can't", "cannot", "could", "couldn't", "did",
				"didn't", "do", "does", "doesn't", "doing", "don't", "down", "during", "each", "few", "for", "from",
				"further", "had", "hadn't", "has", "hasn't", "have", "haven't", "having", "he", "he'd", "he'll",
				"he's", "her", "here", "here's", "hers", "herself", "him", "himself", "his", "how", "how's", "i",
				"i'd", "i'll", "i'm", "i've", "if", "in", "into", "is", "isn't", "it", "it's", "its", "itself",
				"let's", "me", "more", "most", "mustn't", "my", "myself", "no", "nor", "not", "of", "off", "on",
				"once", "only", "or", "other", "ought", "our", "ours ", " ourselves", "out", "over", "own", "same",
				"shan't", "she", "she'd", "she'll", "she's", "should", "shouldn't", "so", "some", "such", "than",
				"that", "that's", "the", "their", "theirs", "them", "themselves", "then", "there", "there's", "these",
				"they", "they'd", "they'll", "they're", "they've", "this", "those", "through", "to", "too", "under",
				"until", "up", "very", "was", "wasn't", "we", "we'd", "we'll", "we're", "we've", "were", "weren't",
				"what", "what's", "when", "when's", "where", "where's", "which", "while", "who", "who's", "whom",
				"why", "why's", "with", "won't", "would", "wouldn't", "you", "you'd", "you'll", "you're", "you've",
				"your", "yours", "yourself", "yourselves", "many", ")", "("));
	}

	public CategoryRepository(Protocol protocol, Integer id, DataService dataService)
	{
		if (protocol == null) throw new IllegalArgumentException("protocol cannot be null");
		this.protocol = protocol;
		if (dataService == null) throw new IllegalArgumentException("dataService cannot be null");
		this.dataService = dataService;
		if (id == null) throw new IllegalArgumentException("id cannot be null");
		this.id = id;
	}

	@Override
	public long count()
	{
		AtomicInteger count = new AtomicInteger(0);
		countEntities(protocol, count);

		return count.get();
	}

	@Override
	public Class<? extends Entity> getEntityClass()
	{
		return MapEntity.class;
	}

	@Override
	public Iterator<Entity> iterator()
	{
		List<Entity> entities = new ArrayList<Entity>();
		createEntities(protocol, entities);

		return entities.iterator();
	}

	@Override
	public void close() throws IOException
	{
		// Nothing
	}

	@Override
	public EntityMetaData getEntityMetaData()
	{
		DefaultEntityMetaData entityMetaData = new DefaultEntityMetaData("featureCategory-" + id);

		entityMetaData.addAttributeMetaData(new DefaultAttributeMetaData(FIELD_TYPE, FieldTypeEnum.STRING));
		entityMetaData.addAttributeMetaData(new DefaultAttributeMetaData(FIELD_ID, FieldTypeEnum.STRING));
		entityMetaData.addAttributeMetaData(new DefaultAttributeMetaData(FIELD_NAME, FieldTypeEnum.STRING));
		entityMetaData.addAttributeMetaData(new DefaultAttributeMetaData(FIELD_DESCRIPTION, FieldTypeEnum.STRING));
		entityMetaData.addAttributeMetaData(new DefaultAttributeMetaData(FIELD_DESCRIPTION_STOPWORDS,
				FieldTypeEnum.STRING));

		return entityMetaData;
	}

	private void countEntities(Protocol protocol, AtomicInteger count)
	{
		List<Protocol> subProtocols = protocol.getSubprotocols();
		if (!subProtocols.isEmpty())
		{
			for (Protocol p : subProtocols)
			{
				countEntities(p, count);
			}
		}
		else
		{
			Iterable<Category> categories = dataService.findAll(Category.ENTITY_NAME,
					new QueryImpl().in(Category.OBSERVABLEFEATURE, protocol.getFeatures()), Category.class);

			count.addAndGet(Iterables.size(categories));
		}
	}

	private void createEntities(Protocol protocol, List<Entity> entities)
	{
		List<Protocol> subProtocols = protocol.getSubprotocols();
		if (!subProtocols.isEmpty())
		{
			for (Protocol p : subProtocols)
			{
				createEntities(p, entities);
			}
		}
		else
		{
			Iterable<Category> categories = dataService.findAll(Category.ENTITY_NAME,
					new QueryImpl().in(Category.OBSERVABLEFEATURE, protocol.getFeatures()), Category.class);

			for (Category c : categories)
			{
				String name = c.getIdentifier();
				String description = c.getName() == null ? StringUtils.EMPTY : I18nTools.get(c.getName())
						.replaceAll("[^a-zA-Z0-9 ]", " ").toLowerCase();

				Set<String> descriptionStopWords = new HashSet<String>(Arrays.asList(description.split(" +")));
				descriptionStopWords.removeAll(STOPWORDSLIST);

				Entity entity = new MapEntity();
				entity.set(FIELD_TYPE, Category.class.getSimpleName().toLowerCase());
				entity.set(FIELD_ID, c.getObservableFeature().getId());
				entity.set(FIELD_NAME, name);
				entity.set(FIELD_DESCRIPTION, name);
				entity.set(FIELD_DESCRIPTION_STOPWORDS, StringUtils.join(descriptionStopWords.toArray(), ' '));
				entities.add(entity);
			}
		}

	}

	@Override
	public Iterable<AttributeMetaData> getLevelOneAttributes()
	{
		// TODO Auto-generated method stub
		return null;
	}

}
