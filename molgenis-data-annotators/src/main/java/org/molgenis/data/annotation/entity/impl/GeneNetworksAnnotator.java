package org.molgenis.data.annotation.entity.impl;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.annotation.AbstractRepositoryAnnotator;
import org.molgenis.data.annotation.CmdLineAnnotatorSettingsConfigurer;
import org.molgenis.data.annotation.RepositoryAnnotator;
import org.molgenis.data.annotation.entity.AnnotatorInfo;
import org.molgenis.data.annotation.entity.AnnotatorInfo.Status;
import org.molgenis.data.annotation.entity.AnnotatorInfo.Type;
import org.molgenis.data.annotation.utils.JsonReader;
import org.molgenis.data.annotator.websettings.GeneNetworksAnnotatorSettings;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.QueryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.DECIMAL;
import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.HYPERLINK;
import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.STRING;

// flow:
// 1) upload VCF
// 2) run snpEff to join the genename to the VCF
// 3) create new instance of the Project entity through data explorer UI
// 4) run this annotator

@Configuration
public class GeneNetworksAnnotator
{
	private static final Logger LOG = LoggerFactory.getLogger(GeneNetworksAnnotator.class);
	public static final String NAME = "GeneNetwork";

	public static final String RESULTS = "results";
	public static final String GENE = "gene";
	public static String GENE_NETWORK_PATH = "/api/v1/prioritization/";
	public static final String VERBOSE = "verbose";
	public static final String GENE_NAME = "name";
	public static final String Z_SCORE = "weightedZScore";
	public static final String ENSEMBLE_ID = "EnsembleID";
	public static final String GENE_DESCRIPTION = "GeneDescription";
	public static final String DESCRIPTION = "description";
	public static final String ID = "id";
	public static final String ONTOLOGY_PREFIX = "http://purl.obolibrary.org/obo/HP_";
	public static final String HPO_PREFIX = "HP:";
	public static final String ONTOLOGY_TERM_IRI = "ontologyTermIRI";
	public static final String GENE_NETWORK_LINK = "Gene network link";
	public static final String ANNOTATION_LOG = "Annotation_log";

	@Autowired
	private DataService dataService;

	@Autowired
	private Entity geneNetworksAnnotatorSettings;

	@Bean
	public RepositoryAnnotator geneNetwork()
	{
		return new GeneNetworksRepositoryAnnotator(geneNetworksAnnotatorSettings, dataService);
	}

	// FIXME: this whole thing is a POC and should not be used for any "real" work!!!!
	public static class GeneNetworksRepositoryAnnotator extends AbstractRepositoryAnnotator
	{
		private static final String TERMS_USED = "termsFound";
		private static final String TERMS_UNUSED = "termsNotFound";
		private static final String LINKOUT = "geneNetworkLink";
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

		private DataService dataService;
		private final Entity pluginSettings;
		private final AnnotatorInfo info = AnnotatorInfo.create(Status.READY, Type.EFFECT_PREDICTION, NAME,
				"Prototype for the gene networks", getOutputMetaData());
		private Entity superEntity;

		public GeneNetworksRepositoryAnnotator(Entity pluginSettings, DataService dataService)
		{
			this.pluginSettings = pluginSettings;
			this.dataService = dataService;
		}

		@Override
		public AnnotatorInfo getInfo()
		{
			return info;
		}

		@Override
		public Iterator<Entity> annotate(Iterable<Entity> source)
		{
			superEntity = dataService
					.findOne(pluginSettings.getString(GeneNetworksAnnotatorSettings.Meta.PROJECT_ENTITY),
							new QueryImpl().eq(
									pluginSettings
											.getString(GeneNetworksAnnotatorSettings.Meta.VARIANT_ENTITY_ATTRIBUTE),
									source.iterator().next().getEntityMetaData().getName()));
			Iterable<Entity> phenotypeEntities = superEntity.getEntities("Phenotypes");
			List<String> phenotypes = parsePhenotypes(phenotypeEntities);
			Map<String, Map<String, Object>> geneNetworkResults = getGeneNetworkResults(phenotypes);
			// query genenetworks for genes + info -> write to Map<GeneName,Map<key, value>>
			// iterate over repo and add info from map based on gene name
			Iterator<Entity> iterator = source.iterator();
			return new Iterator<Entity>()
			{
				@Override
				public boolean hasNext()
				{
					boolean next = iterator.hasNext();
					return next;
				}

				@Override
				public Entity next()
				{
					Entity entity = iterator.next();
					DefaultEntityMetaData meta = new DefaultEntityMetaData(entity.getEntityMetaData());
					info.getOutputAttributes().forEach(meta::addAttributeMetaData);
					Entity copy = new MapEntity(entity, meta);
					Map<String, Object> resultsForGene = geneNetworkResults
							.get(entity.getString(CGDAnnotator.CGDAttributeName.GENE.getAttributeName()));
					if (resultsForGene != null)
					{
						copy.set(Z_SCORE, resultsForGene.get(Z_SCORE));
						copy.set(ENSEMBLE_ID, ((JSONObject) resultsForGene.get(GENE)).getString(ID));
						copy.set(GENE_DESCRIPTION, ((JSONObject) resultsForGene.get(GENE)).getString(DESCRIPTION));
						copy.set(GENE_NETWORK_LINK,
								pluginSettings.getString(GeneNetworksAnnotatorSettings.Meta.GENE_NETWORK_URL) + "/gene/"
										+ entity.getString(CGDAnnotator.CGDAttributeName.GENE.getAttributeName()));
					}
					return copy;
				}

			};
		}

		private Map<String, Map<String, Object>> getGeneNetworkResults(List<String> phenotypes)
		{
			Map<String, Map<String, Object>> results = new HashMap<>();
			try
			{

				JSONObject jsonObject = JsonReader
						.readJsonFromUrl(pluginSettings.getString(GeneNetworksAnnotatorSettings.Meta.GENE_NETWORK_URL)
								+ GENE_NETWORK_PATH + StringUtils.join(phenotypes, ',') + "?" + VERBOSE);
				JSONArray array = jsonObject.getJSONArray(RESULTS);
				for (int i = 0; i < array.length(); i++)
				{
					String geneName = ((JSONObject) array.get(i)).getJSONObject(GENE).getString(GENE_NAME);
					Map<String, Object> resultsForGene = new HashMap<>();
					for (String key : ((JSONObject) array.get(i)).keySet())
					{
						resultsForGene.put(key, ((JSONObject) array.get(i)).get(key));
					}
					results.put(geneName, resultsForGene);
				}

				if (superEntity.getEntityMetaData().getAttribute(TERMS_USED) != null)
				{
					StringBuilder sb = new StringBuilder();
					JSONArray termsArray = jsonObject.getJSONArray("terms");
					for (int i = 0; i < termsArray.length(); i++)
					{
						if(i != 0) sb.append(",");
						sb.append(((JSONObject) termsArray.get(i)).getJSONObject("term").getString("id"));
					}
					superEntity.set(TERMS_USED, sb.toString());
					dataService.update(pluginSettings.getString(GeneNetworksAnnotatorSettings.Meta.PROJECT_ENTITY),
							superEntity);
				}
				if (superEntity.getEntityMetaData().getAttribute(LINKOUT) != null)
				{
					StringBuilder sb = new StringBuilder();
					JSONArray termsArray = jsonObject.getJSONArray("terms");
					for (int i = 0; i < termsArray.length(); i++)
					{
						if(i != 0) sb.append(",");
						sb.append(((JSONObject) termsArray.get(i)).getJSONObject("term").getString("id"));
					}
					superEntity.set(LINKOUT, pluginSettings.getString(GeneNetworksAnnotatorSettings.Meta.GENE_NETWORK_URL) + "/diagnosis/" +sb.toString());
					dataService.update(pluginSettings.getString(GeneNetworksAnnotatorSettings.Meta.PROJECT_ENTITY),
							superEntity);
				}
				if (superEntity.getEntityMetaData().getAttribute(TERMS_UNUSED) != null)
				{
					StringBuilder sb = new StringBuilder();
					JSONArray termsArray = jsonObject.getJSONArray("termsNotFound");
					for (int i = 0; i < termsArray.length(); i++)
					{
						if(i != 0) sb.append(",");
						sb.append((termsArray.get(i)));
					}
					superEntity.set(TERMS_UNUSED, sb.toString());
					dataService.update(pluginSettings.getString(GeneNetworksAnnotatorSettings.Meta.PROJECT_ENTITY),
							superEntity);
				}
			}
			catch (MalformedURLException e)
			{

				e.printStackTrace();

			}
			catch (IOException e)
			{

				e.printStackTrace();

			}

			return results;
		}

		private List<String> parsePhenotypes(Iterable<Entity> phenotypeEntities)
		{
			List<String> results = new ArrayList<>();
			for (Entity entity : phenotypeEntities)
			{
				String iri = entity.getString(ONTOLOGY_TERM_IRI);
				String hpoTerm = iri.replace(ONTOLOGY_PREFIX, HPO_PREFIX);
				results.add(hpoTerm);
			}
			return results;
		}

		@Override
		public String canAnnotate(EntityMetaData repoMetaData)
		{
			long resultCount = dataService
					.count(pluginSettings.getString(GeneNetworksAnnotatorSettings.Meta.PROJECT_ENTITY), new QueryImpl()
							.eq(GeneNetworksAnnotatorSettings.Meta.VARIANT_ENTITY_ATTRIBUTE, repoMetaData.getName()));
			if (resultCount <= 0)
			{
				return "No matching entity found in: "
						+ pluginSettings.getString(GeneNetworksAnnotatorSettings.Meta.PROJECT_ENTITY) + " for: "
						+ repoMetaData.getName();
			}
			else if (resultCount > 1)
			{
				return "more than one matching entity found in: "
						+ pluginSettings.getString(GeneNetworksAnnotatorSettings.Meta.PROJECT_ENTITY) + " for: "
						+ repoMetaData.getName();
			}
			return super.canAnnotate(repoMetaData);
		}

		@Override
		public List<AttributeMetaData> getOutputMetaData()
		{
			List<AttributeMetaData> attributes = new ArrayList<>();

			DefaultAttributeMetaData annotation = new DefaultAttributeMetaData(Z_SCORE, DECIMAL);
			annotation.setDescription("The weighted Z-Score determined by 'Gene Network'");
			attributes.add(annotation);

			DefaultAttributeMetaData ensembleID = new DefaultAttributeMetaData(ENSEMBLE_ID, STRING);
			ensembleID.setDescription("Ensemble ID for the gene");
			attributes.add(ensembleID);

			DefaultAttributeMetaData description = new DefaultAttributeMetaData(GENE_DESCRIPTION, STRING);
			description.setDescription("description of the gene");
			attributes.add(description);

			DefaultAttributeMetaData hyperlink = new DefaultAttributeMetaData(GENE_NETWORK_LINK, HYPERLINK);
			hyperlink.setDescription("Link to the gene on the gene network page");
			attributes.add(hyperlink);

			DefaultAttributeMetaData compoundAttributeMetaData = new DefaultAttributeMetaData(this.getFullName(),
					MolgenisFieldTypes.FieldTypeEnum.COMPOUND);
			compoundAttributeMetaData.setLabel(this.getSimpleName());

			for (AttributeMetaData attributeMetaData : attributes)
			{
				compoundAttributeMetaData.addAttributePart(attributeMetaData);
			}

			return Collections.singletonList(compoundAttributeMetaData);
		}

		@Override
		public List<AttributeMetaData> getInputMetaData()
		{
			List<AttributeMetaData> attributes = new ArrayList<>();
			attributes.add(new DefaultAttributeMetaData(CGDAnnotator.CGDAttributeName.GENE.getAttributeName()));

			return attributes;
		}

		@Override
		public String getSimpleName()
		{
			return NAME;
		}

		@Override
		protected boolean annotationDataExists()
		{
			return dataService
					.hasRepository(pluginSettings.getString(GeneNetworksAnnotatorSettings.Meta.PROJECT_ENTITY));
		}

		@Override
		public CmdLineAnnotatorSettingsConfigurer getCmdLineAnnotatorSettingsConfigurer()
		{
			throw new UnsupportedOperationException("Commandline version not yet implemented");
		}
	}

}
