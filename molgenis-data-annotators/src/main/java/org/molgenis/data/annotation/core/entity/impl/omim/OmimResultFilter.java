package org.molgenis.data.annotation.core.entity.impl.omim;

import com.google.common.base.Optional;
import com.google.common.collect.FluentIterable;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.annotation.core.entity.ResultFilter;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeMetaDataFactory;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.meta.model.EntityMetaDataFactory;
import org.molgenis.data.support.DynamicEntity;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.molgenis.data.annotation.core.entity.impl.omim.OmimAnnotator.*;

public class OmimResultFilter implements ResultFilter
{
	private EntityMetaDataFactory entityMetaDataFactory;
	private AttributeMetaDataFactory attributeMetaDataFactory;
	private OmimAnnotator omimAnnotator;

	public OmimResultFilter(EntityMetaDataFactory entityMetaDataFactory,
			AttributeMetaDataFactory attributeMetaDataFactory, OmimAnnotator omimAnnotator)
	{
		this.entityMetaDataFactory = entityMetaDataFactory;
		this.attributeMetaDataFactory = attributeMetaDataFactory;
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

		EntityMetaData emd = entityMetaDataFactory.create().setName(OmimAnnotator.NAME);
		emd.addAttributes(Arrays.asList(omimAnnotator.getPhenotypeAttr(), omimAnnotator.getMimNumberAttr(),
				omimAnnotator.getOmimLocationAttr(), omimAnnotator.getEntryAttr(), omimAnnotator.getTypeAttr()));
		Attribute id = attributeMetaDataFactory.create().setName("ID").setAuto(true);
		emd.setIdAttribute(id);

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
