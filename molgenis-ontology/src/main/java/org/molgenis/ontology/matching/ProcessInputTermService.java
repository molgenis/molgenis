package org.molgenis.ontology.matching;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.molgenis.auth.MolgenisUser;
import org.molgenis.auth.UserAuthority;
import org.molgenis.data.DataService;
import org.molgenis.data.DatabaseAction;
import org.molgenis.data.Entity;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.importer.EmxImportService;
import org.molgenis.data.mysql.MysqlRepositoryCollection;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.ontology.OntologyService;
import org.molgenis.ontology.OntologyServiceResult;
import org.molgenis.ontology.repository.OntologyTermQueryRepository;
import org.molgenis.ontology.service.OntologyServiceImpl;
import org.molgenis.security.core.Permission;
import org.molgenis.security.core.utils.SecurityUtils;
import org.molgenis.security.runas.RunAsSystem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;

public class ProcessInputTermService
{
	private static final int ADD_BATCH_SIZE = 1000;

	private final EmxImportService emxImportService;

	private final MysqlRepositoryCollection mysqlRepositoryCollection;

	private final DataService dataService;

	private final UploadProgress uploadProgress;

	private final OntologyService ontologyService;

	@Autowired
	public ProcessInputTermService(EmxImportService emxImportService,
			MysqlRepositoryCollection mysqlRepositoryCollection, DataService dataService,
			UploadProgress uploadProgress, OntologyService ontologyService)
	{
		this.emxImportService = emxImportService;
		this.mysqlRepositoryCollection = mysqlRepositoryCollection;
		this.dataService = dataService;
		this.uploadProgress = uploadProgress;
		this.ontologyService = ontologyService;
	}

	@Async
	@RunAsSystem
	@Transactional
	public void process(SecurityContext securityContext, MolgenisUser molgenisUser, String entityName,
			String ontologyIri, File uploadFile, RepositoryCollection repositoryCollection)
	{
		String userName = molgenisUser.getUsername();
		uploadProgress.registerUser(userName, entityName);
		// Add the original input dataset to database
		mysqlRepositoryCollection.add(repositoryCollection.getRepositoryByEntityName(entityName).getEntityMetaData());
		emxImportService.doImport(repositoryCollection, DatabaseAction.ADD);
		dataService.getCrudRepository(entityName).flush();

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
		uploadProgress.registerUser(userName, entityName, (int) dataService.count(entityName, new QueryImpl()));
		// Match input terms with code
		Iterable<Entity> findAll = dataService.findAll(entityName);
		try
		{
			List<Entity> entitiesToAdd = new ArrayList<Entity>();
			for (Entity entity : findAll)
			{
				OntologyServiceResult searchEntity = ontologyService.searchEntity(ontologyIri, entity);
				for (Map<String, Object> ontologyTerm : searchEntity.getOntologyTerms())
				{
					Double score = Double.parseDouble(ontologyTerm.get(OntologyServiceImpl.SCORE).toString());
					MapEntity matchingTaskContentEntity = new MapEntity();
					matchingTaskContentEntity.set(MatchingTaskContentEntity.IDENTIFIER,
							entityName + "_" + entity.getIdValue());
					matchingTaskContentEntity.set(MatchingTaskContentEntity.INPUT_TERM, entity.getIdValue());
					matchingTaskContentEntity.set(MatchingTaskContentEntity.REF_ENTITY, entityName);
					matchingTaskContentEntity.set(MatchingTaskContentEntity.MATCHED_TERM,
							ontologyTerm.get(OntologyTermQueryRepository.ONTOLOGY_TERM_IRI));
					matchingTaskContentEntity.set(MatchingTaskContentEntity.SCORE, score);
					matchingTaskContentEntity.set(MatchingTaskContentEntity.VALIDATED, false);
					entitiesToAdd.add(matchingTaskContentEntity);
					break;
				}
				// Add entity in batch
				if (entitiesToAdd.size() >= ADD_BATCH_SIZE)
				{
					dataService.add(MatchingTaskContentEntity.ENTITY_NAME, entitiesToAdd);
					entitiesToAdd.clear();
				}
				uploadProgress.incrementProgress(userName);
			}
			// Add the rest
			if (entitiesToAdd.size() != 0)
			{
				dataService.add(MatchingTaskContentEntity.ENTITY_NAME, entitiesToAdd);
				entitiesToAdd.clear();
			}
			dataService.getCrudRepository(MatchingTaskContentEntity.ENTITY_NAME).flush();

			// FIXME : temporary work around to assign write permissions to the
			// users who create the entities.
			Authentication auth = securityContext.getAuthentication();
			List<GrantedAuthority> roles = Lists.newArrayList(auth.getAuthorities());
			for (Permission permiossion : Permission.values())
			{
				UserAuthority userAuthority = new UserAuthority();
				userAuthority.setMolgenisUser(molgenisUser);
				String role = SecurityUtils.AUTHORITY_ENTITY_PREFIX + permiossion.toString() + "_"
						+ entityName.toUpperCase();
				userAuthority.setRole(role);
				roles.add(new SimpleGrantedAuthority(role));
				dataService.add(UserAuthority.ENTITY_NAME, userAuthority);
				dataService.getCrudRepository(UserAuthority.ENTITY_NAME).flush();
			}
			auth = new UsernamePasswordAuthenticationToken(auth.getPrincipal(), null, roles);
			securityContext.setAuthentication(auth);

		}
		catch (Exception e)
		{
			throw new RuntimeException(e.getMessage());
		}
		finally
		{
			uploadProgress.removeUser(userName);
		}
	}
}