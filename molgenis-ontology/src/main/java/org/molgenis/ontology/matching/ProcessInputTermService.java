package org.molgenis.ontology.matching;

import java.io.File;
import java.util.Date;
import java.util.Map;

import org.molgenis.data.DataService;
import org.molgenis.data.DatabaseAction;
import org.molgenis.data.Entity;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.importer.EmxImportService;
import org.molgenis.data.mysql.MysqlRepositoryCollection;
import org.molgenis.data.support.MapEntity;
import org.molgenis.ontology.OntologyServiceResult;
import org.molgenis.ontology.repository.OntologyTermQueryRepository;
import org.molgenis.ontology.service.OntologyServiceImpl;
import org.molgenis.security.runas.RunAsSystem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;

public class ProcessInputTermService
{
	@Autowired
	private EmxImportService emxImportService;

	@Autowired
	private MysqlRepositoryCollection mysqlRepositoryCollection;

	@Autowired
	private DataService dataService;

	@Autowired
	private UploadProgress uploadProgress;

	@Autowired
	private OntologyServiceImpl ontologyService;

	@Async
	@RunAsSystem
	public void process(String userName, String entityName, String ontologyIri, File uploadFile,
			RepositoryCollection repositoryCollection) throws Exception
	{
		uploadProgress.registerUser(userName, 0);

		// Add the original input dataset to database
		mysqlRepositoryCollection.add(repositoryCollection.getRepositoryByEntityName(entityName).getEntityMetaData());
		emxImportService.doImport(repositoryCollection, DatabaseAction.ADD);

		// Add a new entry in MatchingTask table for this new matching job
		int threshold = uploadProgress.getThreshold(userName);
		MapEntity mapEntity = new MapEntity();
		mapEntity.set(MatchingTaskEntity.IDENTIFIER, entityName);
		mapEntity.set(MatchingTaskEntity.DATA_CREATED, new Date());
		mapEntity.set(MatchingTaskEntity.CODE_SYSTEM, ontologyIri);
		mapEntity.set(MatchingTaskEntity.MOLGENIS_USER, userName);
		mapEntity.set(MatchingTaskEntity.THRESHOLD, threshold);
		dataService.add(MatchingTaskEntity.ENTITY_NAME, mapEntity);
		dataService.getCrudRepository(MatchingTaskEntity.ENTITY_NAME).flush();

		// Match input terms with code
		Iterable<Entity> findAll = dataService.findAll(entityName);
		for (Entity entity : findAll)
		{
			OntologyServiceResult searchEntity = ontologyService.searchEntity(ontologyIri, entity);
			for (Map<String, Object> ontologyTerm : searchEntity.getOntologyTerms())
			{
				Double score = Double.parseDouble(ontologyTerm.get(OntologyServiceImpl.COMBINED_SCORE).toString());
				MapEntity matchingTaskContentEntity = new MapEntity();
				matchingTaskContentEntity.set(MathcingTaskContentEntity.IDENTIFIER,
						entityName + ":" + entity.getIdValue());
				matchingTaskContentEntity.set(MathcingTaskContentEntity.INPUT_TERM, entity.getIdValue());
				matchingTaskContentEntity.set(MathcingTaskContentEntity.MATCHED_TERM,
						ontologyTerm.get(OntologyTermQueryRepository.ONTOLOGY_TERM_IRI));
				matchingTaskContentEntity.set(MathcingTaskContentEntity.SCORE, score);
				matchingTaskContentEntity.set(MathcingTaskContentEntity.VALIDATED, score.intValue() >= threshold);
				try
				{
					dataService.add(MathcingTaskContentEntity.ENTITY_NAME, matchingTaskContentEntity);
				}
				catch (Exception e)
				{
					// throw new RuntimeException(e.getMessage());
					e.printStackTrace();
				}
				break;
			}
		}
		dataService.getCrudRepository(MathcingTaskContentEntity.ENTITY_NAME).flush();

		uploadProgress.removeUser(userName);
	}
}
