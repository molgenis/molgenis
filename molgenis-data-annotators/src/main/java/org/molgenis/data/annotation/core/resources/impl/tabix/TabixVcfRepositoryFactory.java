package org.molgenis.data.annotation.core.resources.impl.tabix;

import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.annotation.core.resources.impl.RepositoryFactory;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityMetaDataFactory;
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
	private EntityMetaDataFactory entityMetaDataFactory;
	private AttributeFactory attributeFactory;

	public TabixVcfRepositoryFactory(String name, VcfAttributes vcfAttributes, EntityMetaDataFactory entityMetaFactory,
			AttributeFactory attrMetaFactory)
	{
		this.name = requireNonNull(name);
		this.entityMetaDataFactory = requireNonNull(entityMetaFactory);
		this.attributeFactory = requireNonNull(attrMetaFactory);
		this.vcfAttributes = requireNonNull(vcfAttributes);
	}

	@Override
	public Repository<Entity> createRepository(File file) throws IOException
	{
		return new TabixVcfRepository(file, name, vcfAttributes, entityMetaDataFactory, attributeFactory);
	}

}
