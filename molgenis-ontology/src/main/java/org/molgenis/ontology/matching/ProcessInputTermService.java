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
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.ontology.OntologyService;
import org.molgenis.ontology.OntologyServiceResult;
import org.molgenis.ontology.beans.ComparableEntity;
import org.molgenis.ontology.repository.OntologyTermQueryRepository;
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

	private final DataService dataService;

	private final UploadProgress uploadProgress;

	private final OntologyService ontologyService;

	@Autowired
	public ProcessInputTermService(EmxImportService emxImportService, DataService dataService,
			UploadProgress uploadProgress, OntologyService ontologyService)
	{
		this.emxImportService = emxImportService;
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
		dataService.getMeta().addEntityMeta(repositoryCollection.getRepository(entityName).getEntityMetaData());
		emxImportService.doImport(repositoryCollection, DatabaseAction.ADD);
		dataService.getRepository(entityName).flush();

		// Add a new entry in MatchingTask table for this new matching job
		int threshold = uploadProgress.getThreshold(userName);
		MapEntity mapEntity = new MapEntity();
		mapEntity.set(MatchingTaskEntityMetaData.IDENTIFIER, entityName);
		mapEntity.set(MatchingTaskEntityMetaData.DATA_CREATED, new Date());
		mapEntity.set(MatchingTaskEntityMetaData.CODE_SYSTEM, ontologyIri);
		mapEntity.set(MatchingTaskEntityMetaData.MOLGENIS_USER, userName);
		mapEntity.set(MatchingTaskEntityMetaData.THRESHOLD, threshold);
		dataService.add(MatchingTaskEntityMetaData.ENTITY_NAME, mapEntity);
		dataService.getRepository(MatchingTaskEntityMetaData.ENTITY_NAME).flush();
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
					Double score = Double.parseDouble(ontologyTerm.get(ComparableEntity.SCORE).toString());
					MapEntity matchingTaskContentEntity = new MapEntity();
					matchingTaskContentEntity.set(MatchingTaskContentEntityMetaData.IDENTIFIER,
							entityName + "_" + entity.getIdValue());
					matchingTaskContentEntity.set(MatchingTaskContentEntityMetaData.INPUT_TERM, entity.getIdValue());
					matchingTaskContentEntity.set(MatchingTaskContentEntityMetaData.REF_ENTITY, entityName);
					matchingTaskContentEntity.set(MatchingTaskContentEntityMetaData.MATCHED_TERM,
							ontologyTerm.get(OntologyTermQueryRepository.ONTOLOGY_TERM_IRI));
					matchingTaskContentEntity.set(MatchingTaskContentEntityMetaData.SCORE, score);
					matchingTaskContentEntity.set(MatchingTaskContentEntityMetaData.VALIDATED, false);
					entitiesToAdd.add(matchingTaskContentEntity);
					break;
				}
				// Add entity in batch
				if (entitiesToAdd.size() >= ADD_BATCH_SIZE)
				{
					dataService.add(MatchingTaskContentEntityMetaData.ENTITY_NAME, entitiesToAdd);
					entitiesToAdd.clear();
				}
				uploadProgress.incrementProgress(userName);
			}
			// Add the rest
			if (entitiesToAdd.size() != 0)
			{
				dataService.add(MatchingTaskContentEntityMetaData.ENTITY_NAME, entitiesToAdd);
				entitiesToAdd.clear();
			}
			dataService.getRepository(MatchingTaskContentEntityMetaData.ENTITY_NAME).flush();

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
				dataService.getRepository(UserAuthority.ENTITY_NAME).flush();
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