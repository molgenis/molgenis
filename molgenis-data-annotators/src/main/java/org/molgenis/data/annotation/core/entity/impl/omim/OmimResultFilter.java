package org.molgenis.data.annotation.core.entity.impl.omim;

import com.google.common.base.Optional;
import com.google.common.collect.FluentIterable;
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

import static org.molgenis.data.annotation.core.entity.impl.omim.OmimAnnotator.*;

public class OmimResultFilter implements ResultFilter
{
	private EntityTypeFactory entityTypeFactory;
	private OmimAnnotator omimAnnotator;

	public OmimResultFilter(EntityTypeFactory entityTypeFactory, OmimAnnotator omimAnnotator)
	{
		this.entityTypeFactory = entityTypeFactory;
		this.omimAnnotator = omimAnnotator;
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
		Optional<Entity> firstResult = FluentIterable.from(results).first();

		// FIXME 4714 refactor to work with auto id, setPackage() and setName()
		EntityType emd = entityTypeFactory.create().setId(OmimAnnotator.NAME);
		emd.addAttributes(Arrays.asList(omimAnnotator.getPhenotypeAttr(), omimAnnotator.getMimNumberAttr(),
				omimAnnotator.getOmimLocationAttr(), omimAnnotator.getEntryAttr(), omimAnnotator.getTypeAttr()));

		return firstResult.transform(e ->
		{
			Entity result = new DynamicEntity(emd);
			result.set(OMIM_DISORDER, e.get(OmimRepository.OMIM_PHENOTYPE_COL_NAME));
			result.set(OMIM_CAUSAL_IDENTIFIER, e.get(OmimRepository.OMIM_MIM_NUMBER_COL_NAME));
			result.set(OMIM_CYTO_LOCATIONS, e.get(OmimRepository.OMIM_CYTO_LOCATION_COL_NAME));
			result.set(OMIM_TYPE, e.get(OmimRepository.OMIM_TYPE_COL_NAME).toString());
			result.set(OMIM_ENTRY, e.get(OmimRepository.OMIM_ENTRY_COL_NAME).toString());

			return result;
		});

	}
}
