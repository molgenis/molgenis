package org.molgenis.data.annotation.core.resources.impl.tabix;

import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.annotation.core.resources.impl.RepositoryFactory;
import org.molgenis.data.meta.model.AttributeFactory;
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
	private AttributeFactory attributeFactory;

	public TabixVcfRepositoryFactory(String name, VcfAttributes vcfAttributes, EntityTypeFactory entityTypeFactory,
			AttributeFactory attrMetaFactory)
	{
		this.name = requireNonNull(name);
		this.entityTypeFactory = requireNonNull(entityTypeFactory);
		this.attributeFactory = requireNonNull(attrMetaFactory);
		this.vcfAttributes = requireNonNull(vcfAttributes);
	}

	@Override
	public Repository<Entity> createRepository(File file) throws IOException
	{
		return new TabixVcfRepository(file, name, vcfAttributes, entityTypeFactory, attributeFactory);
	}

}
