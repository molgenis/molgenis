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
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.Protocol;
import org.molgenis.omx.utils.I18nTools;

public class ProtocolTreeRepository extends AbstractRepository implements Countable
{
	public static final String BASE_URL = "protocolTree://";
	private static final String FIELD_TYPE = "type";
	private static final String FIELD_ID = "id";
	private static final String FIELD_IDENTIFIER = "identifier";
	private static final String FIELD_NAME = "name";
	private static final String FIELD_DESCRIPTION = "description";
	private static final String FIELD_DESCRIPTION_STOPWORDS = "descriptionStopwords";
	private static final String FIELD_PATH = "path";
	private static final String FIELD_BOOST_ONTOLOGYTERM = "boostOntologyTerms";
	private static final String DATA_TYPE = "dataType";
	private static final String FIELD_CATEGORY = "category";

	private final Protocol protocol;
	private final DataService dataService;
	private final String name;
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

	public ProtocolTreeRepository(Protocol protocol, DataService dataService, String name)
	{
		super(BASE_URL + name);
		if (protocol == null) throw new IllegalArgumentException("protocol cannot be null");
		this.protocol = protocol;
		if (dataService == null) throw new IllegalArgumentException("dataService cannot be null");
		this.dataService = dataService;
		if (name == null) throw new IllegalArgumentException("name cannot be null");
		this.name = name;
	}

	@Override
	public long count()
	{
		AtomicInteger count = new AtomicInteger(1);// add one for root protocol
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

		// index root protocol
		String description = protocol.getDescription() == null ? StringUtils.EMPTY : I18nTools
				.get(protocol.getDescription()).replaceAll("[^a-zA-Z0-9 ]", " ").toLowerCase();

		Entity entity = new MapEntity();
		entity.set(FIELD_TYPE, Protocol.class.getSimpleName().toLowerCase());
		entity.set(FIELD_ID, protocol.getId());
		entity.set(FIELD_IDENTIFIER, protocol.getIdentifier());
		entity.set(FIELD_NAME, protocol.getName());
		entity.set(FIELD_DESCRIPTION, description);
		entity.set(FIELD_PATH, protocol.getId().toString());
		entities.add(entity);

		createEntities(protocol.getId().toString(), protocol, entities);

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
		DefaultEntityMetaData entityMetaData = new DefaultEntityMetaData(name);

		entityMetaData.addAttributeMetaData(new DefaultAttributeMetaData(FIELD_TYPE, FieldTypeEnum.STRING));
		entityMetaData.addAttributeMetaData(new DefaultAttributeMetaData(FIELD_ID, FieldTypeEnum.STRING));
		entityMetaData.addAttributeMetaData(new DefaultAttributeMetaData(FIELD_IDENTIFIER, FieldTypeEnum.STRING));
		entityMetaData.addAttributeMetaData(new DefaultAttributeMetaData(FIELD_NAME, FieldTypeEnum.STRING));
		entityMetaData.addAttributeMetaData(new DefaultAttributeMetaData(FIELD_DESCRIPTION, FieldTypeEnum.STRING));
		entityMetaData.addAttributeMetaData(new DefaultAttributeMetaData(FIELD_DESCRIPTION_STOPWORDS,
				FieldTypeEnum.STRING));
		entityMetaData
				.addAttributeMetaData(new DefaultAttributeMetaData(FIELD_BOOST_ONTOLOGYTERM, FieldTypeEnum.STRING));
		entityMetaData.addAttributeMetaData(new DefaultAttributeMetaData(FIELD_PATH, FieldTypeEnum.STRING));
		entityMetaData.addAttributeMetaData(new DefaultAttributeMetaData(FIELD_CATEGORY, FieldTypeEnum.STRING));

		return entityMetaData;
	}

	private void countEntities(Protocol protocol, AtomicInteger count)
	{
		List<Protocol> subProtocols = protocol.getSubprotocols();
		if (subProtocols != null && !subProtocols.isEmpty())
		{
			for (Protocol p : subProtocols)
			{
				count.incrementAndGet();
				countEntities(p, count);
			}
		}
		List<ObservableFeature> features = protocol.getFeatures();
		if (features != null && !features.isEmpty())
		{
			count.addAndGet(features.size());
		}
	}

	private void createEntities(String protocolPath, Protocol protocol, List<Entity> entities)
	{
		List<Protocol> subProtocols = protocol.getSubprotocols();
		if (subProtocols != null && !subProtocols.isEmpty())
		{
			for (Protocol p : subProtocols)
			{
				StringBuilder pathBuilder = new StringBuilder();
				if (!protocolPath.isEmpty()) pathBuilder.append(protocolPath).append('.');
				String name = p.getName();
				final String path = pathBuilder.append(p.getId()).toString();
				String description = p.getDescription() == null ? StringUtils.EMPTY : I18nTools.get(p.getDescription())
						.replaceAll("[^a-zA-Z0-9 ]", " ").toLowerCase();

				Entity entity = new MapEntity();
				entity.set(FIELD_TYPE, Protocol.class.getSimpleName().toLowerCase());
				entity.set(FIELD_ID, p.getId());
				entity.set(FIELD_IDENTIFIER, p.getIdentifier());
				entity.set(FIELD_NAME, name);
				entity.set(FIELD_DESCRIPTION, description);
				entity.set(FIELD_PATH, path);
				entities.add(entity);

				// recurse
				createEntities(pathBuilder.toString(), p, entities);
			}
		}
		List<ObservableFeature> features = protocol.getFeatures();
		if (features != null && !features.isEmpty())
		{
			for (ObservableFeature feature : features)
			{
				StringBuilder pathBuilder = new StringBuilder();
				String name = feature.getName();
				String description = feature.getDescription() == null ? StringUtils.EMPTY : I18nTools
						.get(feature.getDescription()).replaceAll("[^a-zA-Z0-9 ]", " ").toLowerCase();
				String path = pathBuilder.append(protocolPath).append(".F").append(feature.getId()).toString();
				StringBuilder categoryValue = new StringBuilder();

				Iterable<Category> categories = dataService.findAll(Category.ENTITY_NAME,
						new QueryImpl().eq(Category.OBSERVABLEFEATURE, feature), Category.class);

				for (Category c : categories)
				{
					String categoryName = c.getName() == null ? StringUtils.EMPTY : c.getName().replaceAll(
							"[^a-zA-Z0-9 ]", " ");
					categoryValue.append(categoryName).append(' ');
				}
				Set<String> descriptionStopWords = new HashSet<String>(Arrays.asList(description.split(" +")));
				descriptionStopWords.removeAll(STOPWORDSLIST);

				Entity entity = new MapEntity();
				entity.set(FIELD_TYPE, ObservableFeature.class.getSimpleName().toLowerCase());
				entity.set(FIELD_ID, feature.getId());
				entity.set(FIELD_IDENTIFIER, feature.getIdentifier());
				entity.set(FIELD_NAME, name);
				entity.set(FIELD_DESCRIPTION, description);
				entity.set(FIELD_DESCRIPTION_STOPWORDS, StringUtils.join(descriptionStopWords.toArray(), ' '));
				entity.set(FIELD_BOOST_ONTOLOGYTERM, StringUtils.EMPTY);
				entity.set(FIELD_PATH, path);
				entity.set(DATA_TYPE, feature.getDataType());
				entity.set(FIELD_CATEGORY, categoryValue.toString().toLowerCase());
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
