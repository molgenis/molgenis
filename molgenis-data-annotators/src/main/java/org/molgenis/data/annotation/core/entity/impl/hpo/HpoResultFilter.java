package org.molgenis.data.annotation.core.entity.impl.hpo;

import com.google.common.base.Optional;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.annotation.core.entity.ResultFilter;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.support.DynamicEntity;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.molgenis.data.annotation.core.entity.impl.hpo.HPOAnnotator.HPO_IDS;
import static org.molgenis.data.annotation.core.entity.impl.hpo.HPOAnnotator.HPO_TERMS;
import static org.molgenis.data.annotation.core.entity.impl.hpo.HPORepository.HPO_ID_COL_NAME;
import static org.molgenis.data.annotation.core.entity.impl.hpo.HPORepository.HPO_TERM_COL_NAME;

public class HpoResultFilter implements ResultFilter
{
	private EntityTypeFactory entityTypeFactory;
	private HPOAnnotator hpoAnnotator;

	public HpoResultFilter(EntityTypeFactory entityTypeFactory, HPOAnnotator hpoAnnotator)
	{
		this.entityTypeFactory = entityTypeFactory;
		this.hpoAnnotator = hpoAnnotator;
	}

	@Override
	public Collection<Attribute> getRequiredAttributes()
	{
		return Collections.emptyList();
	}

	@Override
	public Optional<Entity> filterResults(Iterable<Entity> results, Entity annotatedEntity, boolean updateMode)
	{
		if (updateMode == true)
		{
			throw new MolgenisDataException("This annotator/filter does not support updating of values");
		}

		StringBuilder ids = new StringBuilder();
		StringBuilder terms = new StringBuilder();

		for (Entity hpoEntity : results)
		{
			if (ids.length() > 0)
			{
				ids.append('/');
				terms.append('/');
			}

			String hpoId = hpoEntity.getString(HPO_ID_COL_NAME);
			String hpoTerm = hpoEntity.getString(HPO_TERM_COL_NAME);
			ids.append(hpoId);
			terms.append(hpoTerm);
		}

		EntityType emd = entityTypeFactory.create(HPOAnnotator.NAME);
		emd.addAttributes(Arrays.asList(hpoAnnotator.getIdsAttr(), hpoAnnotator.getTermsAttr()));
		Entity aggregated = new DynamicEntity(emd);
		aggregated.set(HPO_IDS, ids.toString());
		aggregated.set(HPO_TERMS, terms.toString());

		return ids.length() == 0 ? Optional.absent() : Optional.of(aggregated);
	}
}