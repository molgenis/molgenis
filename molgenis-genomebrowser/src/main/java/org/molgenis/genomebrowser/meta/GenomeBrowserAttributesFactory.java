package org.molgenis.genomebrowser.meta;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.stereotype.Component;

@Component
public class GenomeBrowserAttributesFactory
		extends AbstractSystemEntityFactory<GenomeBrowserAttributes, GenomeBrowserAttributesMetadata, String>
{
	GenomeBrowserAttributesFactory(GenomeBrowserAttributesMetadata myEntityMeta, EntityPopulator entityPopulator)
	{
		super(GenomeBrowserAttributes.class, myEntityMeta, entityPopulator);
	}

	public GenomeBrowserAttributes create(String id, boolean isDefault, int order, String pos, String chr, String ref,
			String alt, String stop)
	{
		GenomeBrowserAttributes gba = super.create(id);
		gba.setPos(pos);
		gba.setChrom(chr);
		gba.setRef(ref);
		gba.setAlt(alt);
		gba.setDefault(isDefault);
		gba.setOrder(order);
		gba.setStop(stop);
		return gba;
	}
}