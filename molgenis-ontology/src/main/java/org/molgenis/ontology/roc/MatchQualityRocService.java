package org.molgenis.ontology.roc;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.molgenis.ontology.sorta.meta.MatchingTaskContentEntityMetaData.SCORE;
import static org.molgenis.ontology.sorta.meta.MatchingTaskContentEntityMetaData.VALIDATED;

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

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisInvalidFormatException;
import org.molgenis.data.excel.ExcelRepositoryCollection;
import org.molgenis.data.excel.ExcelSheetWriter;
import org.molgenis.data.excel.ExcelWriter;
import org.molgenis.data.excel.ExcelWriter.FileFormat;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.file.FileStore;
import org.molgenis.ontology.core.meta.OntologyTermMetaData;
import org.molgenis.ontology.sorta.job.SortaJobExecution;
import org.molgenis.ontology.sorta.meta.MatchingTaskContentEntityMetaData;
import org.molgenis.ontology.sorta.service.SortaService;
import org.molgenis.ontology.sorta.service.impl.SortaServiceImpl;
import org.molgenis.ontology.utils.SortaServiceUtil;
import org.molgenis.security.user.UserAccountService;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Iterables;

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

	public Map<String, Object> calculateROC(String sortaJobExecutionId)
			throws IOException, MolgenisInvalidFormatException
	{
		Map<String, Object> data = new HashMap<String, Object>();
		if (isNotEmpty(sortaJobExecutionId))
		{
			File file = fileStore.getFile(createFileName());
			SortaJobExecution sortaJobExecution = dataService.findOne(SortaJobExecution.ENTITY_NAME,
					sortaJobExecutionId, SortaJobExecution.class);

			if (sortaJobExecution != null)
			{
				String sourceEntityName = sortaJobExecution.getSourceEntityName();
				String resultEntityName = sortaJobExecution.getResultEntityName();
				String codeSystem = sortaJobExecution.getOntologyIri();
				double threshold = sortaJobExecution.getThreshold();

				long totalNumberOfTerms = dataService.count(sortaJobExecution.getResultEntityName(), new QueryImpl());

				// Get all validated matches
				Stream<Entity> validatedMatchEntities = dataService.findAll(resultEntityName,
						new QueryImpl().eq(VALIDATED, true).or().ge(SCORE, threshold));

				List<Entity> resultEntities = new ArrayList<Entity>();
				validatedMatchEntities.forEach(validatedMatchEntity -> {
					String matchedCodeIdentifier = validatedMatchEntity
							.getString(MatchingTaskContentEntityMetaData.MATCHED_TERM);
					boolean manualMatchExists = matchedCodeIdentifier != null && !matchedCodeIdentifier.equals("NULL");

					Iterable<Entity> ontologyTermEntities = ontologyService.findOntologyTermEntities(codeSystem,
							getInputTerm(validatedMatchEntity, sourceEntityName));

					long totalNumber = Iterables.size(ontologyTermEntities);
					int rank = 0;

					if (manualMatchExists)
					{
						for (Entity candidateMatch : ontologyTermEntities)
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
				createRocExcelSheet(resultEntities, sourceEntityName, excelWriter);
				excelWriter.close();

				ExcelRepositoryCollection excelRepositoryCollection = new ExcelRepositoryCollection(file);

				data.put("sortaJobExecutionId", resultEntityName);
				data.put("rocfilePath", file.getAbsolutePath());
				data.put("totalNumber", totalNumberOfTerms);
				data.put("validatedNumber", resultEntities.size());
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
