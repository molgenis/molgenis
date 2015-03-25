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

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.elasticsearch.common.collect.Iterables;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.excel.ExcelRepositoryCollection;
import org.molgenis.data.excel.ExcelSheetWriter;
import org.molgenis.data.excel.ExcelWriter;
import org.molgenis.data.excel.ExcelWriter.FileFormat;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.ontology.model.OntologyTermMetaData;
import org.molgenis.ontology.sorta.MatchingTaskContentEntityMetaData;
import org.molgenis.ontology.sorta.MatchingTaskEntityMetaData;
import org.molgenis.ontology.sorta.SortaService;
import org.molgenis.ontology.sorta.SortaServiceImpl;
import org.molgenis.ontology.utils.SortaServiceUtil;
import org.molgenis.security.user.UserAccountService;
import org.molgenis.util.FileStore;
import org.springframework.beans.factory.annotation.Autowired;

public class MatchQualityRocService
{
	private static final int MAX_NUM = 100;

	@Autowired
	private FileStore fileStore;
	@Autowired
	private UserAccountService userAccountService;

	private final DataService dataService;
	private final SortaService ontologyService;

	@Autowired
	public MatchQualityRocService(DataService dataService, SortaService ontologyService)
	{
		if (dataService == null) throw new IllegalArgumentException("DataService cannot be null!");
		if (ontologyService == null) throw new IllegalArgumentException("OntologyMatchingService cannot be null!");
		this.dataService = dataService;
		this.ontologyService = ontologyService;
	}

	public Map<String, Object> calculateROC(String matchingTaskIdentifier) throws IOException, InvalidFormatException
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
				Iterable<Entity> validatedMatchEntities = dataService.findAll(
						MatchingTaskContentEntityMetaData.ENTITY_NAME,
						new QueryImpl().eq(MatchingTaskContentEntityMetaData.REF_ENTITY, entityName).and().nest()
								.eq(MatchingTaskContentEntityMetaData.VALIDATED, true).or()
								.ge(MatchingTaskContentEntityMetaData.SCORE, threshold).unnest());

				List<Entity> resultEntities = new ArrayList<Entity>();
				for (Entity validatedMatchEntity : validatedMatchEntities)
				{
					String matchedCodeIdentifier = validatedMatchEntity
							.getString(MatchingTaskContentEntityMetaData.MATCHED_TERM);
					boolean manualMatchExists = matchedCodeIdentifier != null && !matchedCodeIdentifier.equals("NULL");

					Iterable<Entity> ontologyTermEntities = ontologyService.findOntologyTermEntities(codeSystem,
							getInputTerm(validatedMatchEntity, entityName));

					long totalNumber = Iterables.size(ontologyTermEntities);
					int rank = 0;

					if (manualMatchExists)
					{
						for (Entity candidateMatch : ontologyTermEntities)
						{
							rank++;
							String candidateMatchIdentifier = candidateMatch
									.getString(OntologyTermMetaData.ONTOLOGY_TERM_IRI);
							if (candidateMatchIdentifier.equals(matchedCodeIdentifier)) break;
						}
					}

					MapEntity entity = new MapEntity();
					entity.set("Total", totalNumber);
					entity.set("Rank", rank);
					entity.set("Match", manualMatchExists);
					resultEntities.add(entity);
				}

				ExcelWriter excelWriter = new ExcelWriter(file, FileFormat.XLS);
				createRocExcelSheet(resultEntities, entityName, excelWriter);
				excelWriter.close();

				ExcelRepositoryCollection excelRepositoryCollection = new ExcelRepositoryCollection(file);

				data.put("entityName", matchingTaskIdentifier);
				data.put("rocfilePath", file.getAbsolutePath());
				data.put("totalNumber", totalNumberOfTerms);
				data.put("validatedNumber", Iterables.size(validatedMatchEntities));
				data.put("rocEntities", SortaServiceUtil.getEntityAsMap(excelRepositoryCollection.getSheet(0)));
			}
		}
		return data;
	}

	private void createRocExcelSheet(Iterable<Entity> resultEntities, String entityName, ExcelWriter excelWriter)
			throws IOException
	{
		ExcelSheetWriter createWritable = excelWriter.createWritable(entityName, Arrays.asList("Cutoff", "TPR", "FPR"));

		DecimalFormat df = new DecimalFormat("##.###", new DecimalFormatSymbols(Locale.ENGLISH));
		for (int cutOff = 1; cutOff <= MAX_NUM; cutOff++)
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

	private Entity getInputTerm(Entity validatedMatchEntity, String entityName)
	{
		String termIdentifier = validatedMatchEntity.getString(MatchingTaskContentEntityMetaData.INPUT_TERM);
		Entity termEntity = dataService.findOne(entityName,
				new QueryImpl().eq(SortaServiceImpl.DEFAULT_MATCHING_IDENTIFIER, termIdentifier));
		return termEntity;
	}

	private String createFileName()
	{
		Date date = new Date();
		return userAccountService.getCurrentUser().getUsername() + "_" + date.getTime() + ".xls";
	}
}
