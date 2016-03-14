package org.molgenis.ontology.sorta.job;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.common.collect.Iterables;
import org.molgenis.auth.MolgenisUser;
import org.molgenis.auth.UserAuthority;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.jobs.Progress;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.ontology.core.meta.OntologyTermMetaData;
import org.molgenis.ontology.sorta.meta.MatchingTaskContentEntityMetaData;
import org.molgenis.ontology.sorta.service.SortaService;
import org.molgenis.ontology.sorta.service.impl.SortaServiceImpl;
import org.molgenis.security.core.Permission;
import org.molgenis.security.core.runas.RunAsSystem;
import org.molgenis.security.core.utils.SecurityUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;

import com.google.common.collect.Lists;

import static java.util.Objects.requireNonNull;

public class SortaJobProcessor
{
	private static final int ADD_BATCH_SIZE = 1000;
	private static final int PROGRESS_UPDATE_BATCH_SIZE = 50;

	private final String ontologyIri;
	private final String entityName;
	private final MolgenisUser molgenisUser;
	private final Progress progress;
	private final DataService dataService;
	private final SortaService sortaService;
	private final SecurityContext securityContext;
	private final AtomicInteger counter;

	public SortaJobProcessor(String ontologyIri, String entityName, MolgenisUser molgenisUser, Progress progress,
			DataService dataService, SortaService sortaService, SecurityContext securityContext)
	{
		this.ontologyIri = requireNonNull(ontologyIri);
		this.entityName = requireNonNull(entityName);
		this.molgenisUser = requireNonNull(molgenisUser);
		this.progress = requireNonNull(progress);
		this.dataService = requireNonNull(dataService);
		this.sortaService = requireNonNull(sortaService);
		this.securityContext = requireNonNull(securityContext);
		this.counter = new AtomicInteger(0);
	}

	@RunAsSystem
	public void process()
	{
		long maxCount = dataService.count(entityName, new QueryImpl());

		progress.setProgressMax((int) maxCount);

		// Match input terms with code
		List<Entity> entitiesToAdd = new ArrayList<Entity>();
		dataService.findAll(entityName).forEach(entity -> {
			MapEntity matchingTaskContentEntity = new MapEntity();
			matchingTaskContentEntity.set(MatchingTaskContentEntityMetaData.INPUT_TERM, entity.getIdValue());
			matchingTaskContentEntity.set(MatchingTaskContentEntityMetaData.IDENTIFIER,
					entityName + "_" + entity.getIdValue());
			matchingTaskContentEntity.set(MatchingTaskContentEntityMetaData.REF_ENTITY, entityName);
			matchingTaskContentEntity.set(MatchingTaskContentEntityMetaData.VALIDATED, false);
			entitiesToAdd.add(matchingTaskContentEntity);

			Iterable<Entity> ontologyTermEntities = sortaService.findOntologyTermEntities(ontologyIri, entity);
			if (Iterables.size(ontologyTermEntities) > 0)
			{
				Entity firstMatchedOntologyTerm = Iterables.getFirst(ontologyTermEntities, new MapEntity());
				matchingTaskContentEntity.set(MatchingTaskContentEntityMetaData.MATCHED_TERM,
						firstMatchedOntologyTerm.get(OntologyTermMetaData.ONTOLOGY_TERM_IRI));
				matchingTaskContentEntity.set(MatchingTaskContentEntityMetaData.SCORE,
						firstMatchedOntologyTerm.get(SortaServiceImpl.SCORE));
			}
			else
			{
				matchingTaskContentEntity.set(MatchingTaskContentEntityMetaData.SCORE, 0);
			}

			// Add entity in batch
			if (entitiesToAdd.size() >= ADD_BATCH_SIZE)
			{
				dataService.add(MatchingTaskContentEntityMetaData.ENTITY_NAME, entitiesToAdd.stream());
				entitiesToAdd.clear();
			}

			// Increase the number of the progress
			counter.incrementAndGet();

			// Update the progress only when the progress proceeds the threshold
			if (counter.get() % PROGRESS_UPDATE_BATCH_SIZE == 0)
			{
				progress.progress(counter.get(), StringUtils.EMPTY);
			}
		});
		// Add the rest
		if (entitiesToAdd.size() != 0)
		{
			dataService.add(MatchingTaskContentEntityMetaData.ENTITY_NAME, entitiesToAdd.stream());
			entitiesToAdd.clear();
		}
		progress.progress(counter.get(), StringUtils.EMPTY);
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
}