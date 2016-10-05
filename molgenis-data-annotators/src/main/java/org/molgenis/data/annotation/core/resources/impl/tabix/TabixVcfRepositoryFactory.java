package org.molgenis.data.annotation.core.resources.impl.tabix;

import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.annotation.core.resources.impl.RepositoryFactory;
import org.molgenis.data.meta.model.AttributeMetaDataFactory;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.vcf.model.VcfAttributes;

import java.io.File;
import java.io.IOException;

import static java.util.Objects.requireNonNull;

/**
 * Factory that can create a {@link TabixVcfRepository}. The name of the repository is configured in the factory.
 */
public class TabixVcfRepositoryFactory implements RepositoryFactory
{
	private final String name;

	private VcfAttributes vcfAttributes;
	private EntityTypeFactory entityTypeFactory;
	private AttributeMetaDataFactory attributeMetaDataFactory;

	public TabixVcfRepositoryFactory(String name, VcfAttributes vcfAttributes, EntityTypeFactory entityMetaFactory,
			AttributeMetaDataFactory attrMetaFactory)
	{
		this.name = requireNonNull(name);
		this.entityTypeFactory = requireNonNull(entityMetaFactory);
		this.attributeMetaDataFactory = requireNonNull(attrMetaFactory);
		this.vcfAttributes = requireNonNull(vcfAttributes);
	}

	@Override
	public Repository<Entity> createRepository(File file) throws IOException
	{
		return new TabixVcfRepository(file, name, vcfAttributes, entityTypeFactory, attributeMetaDataFactory);
	}

}
