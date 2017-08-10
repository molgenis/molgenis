package org.molgenis.genomebrowser.meta;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.StaticEntity;

import javax.annotation.Nullable;

public class GenomeBrowserAttributes extends StaticEntity implements Comparable<GenomeBrowserAttributes>
{
	public GenomeBrowserAttributes(Entity entity)
	{
		super(entity);
	}

	public GenomeBrowserAttributes(EntityType entityType)
	{
		super(entityType);
	}

	public GenomeBrowserAttributes(String identifier, EntityType entityType)
	{
		super(identifier, entityType);
	}

	public String getIdentifier()
	{
		return getString(GenomeBrowserAttributesMetadata.IDENTIFIER);
	}

	public void setIdentifier(String identifier)
	{
		set(GenomeBrowserAttributesMetadata.IDENTIFIER, identifier);
	}

	public Boolean iDefault()
	{
		return getBoolean(GenomeBrowserAttributesMetadata.DEFAULT);
	}

	public void setDefault(Boolean isDefault)
	{
		set(GenomeBrowserAttributesMetadata.DEFAULT, isDefault);
	}

	@Nullable
	public Integer getOrder()
	{
		return getInt(GenomeBrowserAttributesMetadata.ORDER);
	}

	public void setOrder(Integer order)
	{
		set(GenomeBrowserAttributesMetadata.ORDER, order);
	}

	public String getPos()
	{
		return getString(GenomeBrowserAttributesMetadata.POS);
	}

	public void setPos(String pos)
	{
		set(GenomeBrowserAttributesMetadata.POS, pos);
	}

	public String getChrom()
	{
		return getString(GenomeBrowserAttributesMetadata.CHROM);
	}

	public void setChrom(String chrom)
	{
		set(GenomeBrowserAttributesMetadata.CHROM, chrom);
	}

	@Nullable
	public String getRef()
	{
		return getString(GenomeBrowserAttributesMetadata.REF);
	}

	public void setRef(String ref)
	{
		set(GenomeBrowserAttributesMetadata.REF, ref);
	}

	@Nullable
	public String getAlt()
	{
		return getString(GenomeBrowserAttributesMetadata.ALT);
	}

	public void setAlt(String alt)
	{
		set(GenomeBrowserAttributesMetadata.ALT, alt);
	}

	@Nullable
	public String getStop()
	{
		return getString(GenomeBrowserAttributesMetadata.STOP);
	}

	public void setStop(String stop)
	{
		set(GenomeBrowserAttributesMetadata.STOP, stop);
	}

	@Override
	public int compareTo(GenomeBrowserAttributes gba)
	{
		if (gba.getOrder() == null) return -1;
		if (gba.getOrder() > this.getOrder()) return -1;
		else if (gba.getOrder() < this.getOrder()) return 1;
		return 0;
	}
}
