package org.molgenis.ontology.matching;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.molgenis.data.DataService;
import org.molgenis.data.DatabaseAction;
import org.molgenis.data.Entity;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.importer.EmxImportService;
import org.molgenis.data.mysql.MysqlRepositoryCollection;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.omx.auth.MolgenisUser;
import org.molgenis.omx.auth.UserAuthority;
import org.molgenis.ontology.OntologyServiceResult;
import org.molgenis.ontology.repository.OntologyTermQueryRepository;
import org.molgenis.ontology.service.OntologyServiceImpl;
import org.molgenis.security.core.Permission;
import org.molgenis.security.core.utils.SecurityUtils;
import org.molgenis.security.permission.PermissionManagerService;
import org.molgenis.security.permission.PermissionSystemService;
import org.molgenis.security.runas.RunAsSystem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;

import com.google.common.collect.Lists;

public class ProcessInputTermService
{
	@Autowired
	private EmxImportService emxImportService;

	@Autowired
	private MysqlRepositoryCollection mysqlRepositoryCollection;

	@Autowired
	private PermissionManagerService permissionManagerService;

	@Autowired
	private PermissionSystemService permissionSystemService;

	@Autowired
	private DataService dataService;

	@Autowired
	private UploadProgress uploadProgress;

	@Autowired
	private OntologyServiceImpl ontologyService;

	@Async
	@RunAsSystem
	public void process(SecurityContext securityContext, MolgenisUser molgenisUser, String entityName,
			String ontologyIri, File uploadFile, RepositoryCollection repositoryCollection) throws Exception
	{
		String userName = molgenisUser.getUsername();
		uploadProgress.registerUser(userName, entityName);
		// Add the original input dataset to database
		mysqlRepositoryCollection.add(repositoryCollection.getRepositoryByEntityName(entityName).getEntityMetaData());

		emxImportService.doImport(repositoryCollection, DatabaseAction.ADD);

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
		}
		dataService.getCrudRepository(UserAuthority.ENTITY_NAME).flush();
		auth = new UsernamePasswordAuthenticationToken(auth.getPrincipal(), null, roles);
		securityContext.setAuthentication(auth);

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

					dataService.add(MatchingTaskContentEntity.ENTITY_NAME, matchingTaskContentEntity);
					break;
				}
				uploadProgress.incrementProgress(userName);
			}
		}
		catch (Exception e)
		{
			throw new RuntimeException(e.getMessage());
		}

		dataService.getCrudRepository(MatchingTaskContentEntity.ENTITY_NAME).flush();

		uploadProgress.removeUser(userName);
	}
}
