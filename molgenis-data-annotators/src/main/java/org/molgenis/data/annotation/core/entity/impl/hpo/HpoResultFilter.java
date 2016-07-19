package org.molgenis.data.annotation.core.entity.impl.hpo;

import com.google.common.base.Optional;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.annotation.core.entity.ResultFilter;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.AttributeMetaDataFactory;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.meta.model.EntityMetaDataFactory;
import org.molgenis.data.support.DynamicEntity;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import static org.molgenis.data.annotation.core.entity.impl.hpo.HPOAnnotator.HPO_IDS;
import static org.molgenis.data.annotation.core.entity.impl.hpo.HPOAnnotator.HPO_TERMS;
import static org.molgenis.data.annotation.core.entity.impl.hpo.HPORepository.HPO_ID_COL_NAME;
import static org.molgenis.data.annotation.core.entity.impl.hpo.HPORepository.HPO_TERM_COL_NAME;

public class HpoResultFilter implements ResultFilter
{
	private EntityMetaDataFactory entityMetaDataFactory;
	private AttributeMetaDataFactory attributeMetaDataFactory;
	private HPOAnnotator hpoAnnotator;

	public HpoResultFilter(EntityMetaDataFactory entityMetaDataFactory,
			AttributeMetaDataFactory attributeMetaDataFactory, HPOAnnotator hpoAnnotator)
	{
		this.entityMetaDataFactory = entityMetaDataFactory;
		this.attributeMetaDataFactory = attributeMetaDataFactory;
		this.hpoAnnotator = hpoAnnotator;
	}

	@Override
	public Collection<AttributeMetaData> getRequiredAttributes()
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

		Iterator<Entity> it = results.iterator();
		while (it.hasNext())
		{
			Entity hpoEntity = it.next();
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

		EntityMetaData emd = entityMetaDataFactory.create().setName(HPOAnnotator.NAME);
		emd.addAttributes(Arrays.asList(hpoAnnotator.getIdsAttr(), hpoAnnotator.getTermsAttr()));
		AttributeMetaData id = attributeMetaDataFactory.create().setName("ID").setAuto(true);
		emd.setIdAttribute(id);
		Entity aggregated = new DynamicEntity(emd);
		aggregated.set(HPO_IDS, ids.toString());
		aggregated.set(HPO_TERMS, terms.toString());

		return ids.length() == 0 ? Optional.absent() : Optional.of(aggregated);
	}
}