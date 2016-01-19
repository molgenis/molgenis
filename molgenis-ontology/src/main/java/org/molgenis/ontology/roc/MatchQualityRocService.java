package org.molgenis.ontology.roc;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisInvalidFormatException;
import org.molgenis.data.Query;
import org.molgenis.data.excel.ExcelRepositoryCollection;
import org.molgenis.data.excel.ExcelSheetWriter;
import org.molgenis.data.excel.ExcelWriter;
import org.molgenis.data.excel.ExcelWriter.FileFormat;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.file.FileStore;
import org.molgenis.ontology.beans.OntologyServiceResult;
import org.molgenis.ontology.core.meta.OntologyTermMetaData;
import org.molgenis.ontology.matching.MatchingTaskContentEntityMetaData;
import org.molgenis.ontology.matching.MatchingTaskEntityMetaData;
import org.molgenis.ontology.matching.OntologyService;
import org.molgenis.ontology.matching.OntologyServiceImpl;
import org.molgenis.ontology.utils.OntologyServiceUtil;
import org.molgenis.security.user.UserAccountService;
import org.springframework.beans.factory.annotation.Autowired;

public class MatchQualityRocService
{
	@Autowired
	private FileStore fileStore;
	@Autowired
	private UserAccountService userAccountService;

	private final DataService dataService;
	private final OntologyService ontologyService;

	@Autowired
	public MatchQualityRocService(DataService dataService, OntologyService ontologyService)
	{
		if (dataService == null) throw new IllegalArgumentException("DataService cannot be null!");
		if (ontologyService == null) throw new IllegalArgumentException("OntologyMatchingService cannot be null!");
		this.dataService = dataService;
		this.ontologyService = ontologyService;
	}

	public Map<String, Object> calculateROC(String matchingTaskIdentifier)
			throws IOException, MolgenisInvalidFormatException
	{
		Map<String, Object> data = new HashMap<String, Object>();
		if (StringUtils.isNotEmpty(matchingTaskIdentifier))
		{
			File file = fileStore.getFile(createFileName());
			Entity matchingTask = dataService.findOne(MatchingTaskEntityMetaData.ENTITY_NAME,
					new QueryImpl().eq(MatchingTaskEntityMetaData.IDENTIFIER, matchingTaskIdentifier));

			if (matchingTask != null)
			{
				String entityName = matchingTask.getString(MatchingTaskEntityMetaData.IDENTIFIER);
				String codeSystem = matchingTask.getString(MatchingTaskEntityMetaData.CODE_SYSTEM);
				double threshold = matchingTask.getDouble(MatchingTaskEntityMetaData.THRESHOLD);

				long totalNumberOfTerms = dataService.count(MatchingTaskContentEntityMetaData.ENTITY_NAME,
						new QueryImpl().eq(MatchingTaskContentEntityMetaData.REF_ENTITY, matchingTaskIdentifier));

				// Get all validated matches
				Query q = new QueryImpl().eq(MatchingTaskContentEntityMetaData.REF_ENTITY, entityName).and().nest()
						.eq(MatchingTaskContentEntityMetaData.VALIDATED, true).or()
						.ge(MatchingTaskContentEntityMetaData.SCORE, threshold).unnest();
				Stream<Entity> validatedMatchEntities = dataService
						.findAll(MatchingTaskContentEntityMetaData.ENTITY_NAME, q);

				List<Entity> resultEntities = new ArrayList<Entity>();
				validatedMatchEntities.forEach(validatedMatchEntity -> {
					String matchedCodeIdentifier = validatedMatchEntity
							.getString(MatchingTaskContentEntityMetaData.MATCHED_TERM);
					boolean manualMatchExists = matchedCodeIdentifier != null && !matchedCodeIdentifier.equals("NULL");

					OntologyServiceResult searchResult = ontologyService.search(codeSystem,
							getInputTerm(validatedMatchEntity, entityName));

					long totalNumber = searchResult.getTotalHitCount();
					int rank = 0;

					if (manualMatchExists)
					{
						for (Map<String, Object> candidateMatch : searchResult.getOntologyTerms())
						{
							rank++;
							String candidateMatchIdentifier = candidateMatch.get(OntologyTermMetaData.ONTOLOGY_TERM_IRI)
									.toString();

							if (candidateMatchIdentifier.equals(matchedCodeIdentifier))
							{
								break;
							}
						}
					}

					MapEntity entity = new MapEntity();
					entity.set("Total", totalNumber);
					entity.set("Rank", rank);
					entity.set("Match", manualMatchExists);
					resultEntities.add(entity);
				});

				ExcelWriter excelWriter = new ExcelWriter(file, FileFormat.XLS);
				createRocExcelSheet(resultEntities, entityName, excelWriter);
				excelWriter.close();

				ExcelRepositoryCollection excelRepositoryCollection = new ExcelRepositoryCollection(file);

				data.put("entityName", matchingTaskIdentifier);
				data.put("rocfilePath", file.getAbsolutePath());
				data.put("totalNumber", totalNumberOfTerms);
				data.put("validatedNumber", dataService.count(MatchingTaskContentEntityMetaData.ENTITY_NAME, q));
				data.put("rocEntities", OntologyServiceUtil.getEntityAsMap(excelRepositoryCollection.getSheet(0)));
			}
		}
		return data;
	}

	private void createRocExcelSheet(Iterable<Entity> resultEntities, String entityName, ExcelWriter excelWriter)
			throws IOException
	{
		ExcelSheetWriter createWritable = excelWriter.createWritable(entityName, Arrays.asList("Cutoff", "TPR", "FPR"));

		DecimalFormat df = new DecimalFormat("##.###", new DecimalFormatSymbols(Locale.ENGLISH));
		for (int cutOff = 1; cutOff <= 500; cutOff++)
		{
			int totalPositives = 0;
			int totalNegatives = 0;
			int retrievedPositives = 0;
			int falsePositives = 0;
			int totalRetrieved = 0;

			for (Entity entity : resultEntities)
			{
				Integer rank = entity.getInt("Rank");
				Integer total = entity.getInt("Total");
				boolean manualMatchExists = entity.getBoolean("Match");

				// If manual match exists, increment postive by one
				if (manualMatchExists)
				{
					totalPositives++;
					totalNegatives += total - 1;
				}
				else
				{
					totalNegatives += total;
				}

				totalRetrieved = cutOff <= total ? cutOff : total;

				if (!manualMatchExists)
				{
					falsePositives += totalRetrieved;
				}
				else
				{
					if (rank <= cutOff)
					{
						retrievedPositives++;
						falsePositives += totalRetrieved - 1;
					}
					else
					{
						falsePositives += totalRetrieved;
					}
				}
			}

			if (totalPositives != 0 && totalNegatives != 0 && totalRetrieved != 0)
			{
				String truePositiveRate = df.format((double) retrievedPositives / totalPositives);
				String falsePositiveRate = df.format((double) falsePositives / totalNegatives);
				String precision = df.format((double) retrievedPositives / totalRetrieved);
				MapEntity entity = new MapEntity();
				entity.set("Cutoff", cutOff);
				entity.set("TPR", truePositiveRate);
				entity.set("FPR", falsePositiveRate);
				entity.set("Precision", precision);
				createWritable.add(entity);
			}
		}
		createWritable.close();
	}

	private String getInputTerm(Entity validatedMatchEntity, String entityName)
	{
		String termIdentifier = validatedMatchEntity.getString(MatchingTaskContentEntityMetaData.INPUT_TERM);
		Entity termEntity = dataService.findOne(entityName,
				new QueryImpl().eq(OntologyServiceImpl.DEFAULT_MATCHING_IDENTIFIER, termIdentifier));
		return termEntity.getString(OntologyServiceImpl.DEFAULT_MATCHING_NAME_FIELD);
	}

	private String createFileName()
	{
		Date date = new Date();
		return userAccountService.getCurrentUser().getUsername() + "_" + date.getTime() + ".xls";
	}
}
