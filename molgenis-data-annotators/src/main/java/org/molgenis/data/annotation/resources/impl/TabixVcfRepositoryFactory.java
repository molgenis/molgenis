package org.molgenis.data.annotation.resources.impl;

import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.annotation.query.GeneNameQueryCreator;
import org.molgenis.data.annotator.tabix.TabixVcfRepository;
import org.molgenis.data.meta.model.AttributeMetaDataFactory;
import org.molgenis.data.meta.model.EntityMetaDataFactory;
import org.molgenis.data.vcf.model.VcfAttributes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

/**
 * Factory that can create a {@link TabixVcfRepository}. The name of the repository is configured in the factory.
 */
public class TabixVcfRepositoryFactory implements RepositoryFactory
{
	private final String name;

	private VcfAttributes vcfAttributes;
	private EntityMetaDataFactory entityMetaDataFactory;
	private AttributeMetaDataFactory attributeMetaDataFactory;

	public TabixVcfRepositoryFactory(String name, VcfAttributes vcfAttributes, EntityMetaDataFactory entityMetaFactory,
			AttributeMetaDataFactory attrMetaFactory)
	{
		this.name = name;
		this.entityMetaDataFactory = entityMetaFactory;
		this.attributeMetaDataFactory = attrMetaFactory;
		this.vcfAttributes = vcfAttributes;
	}

	@Override
	public Repository<Entity> createRepository(File file) throws IOException
	{
		return new TabixVcfRepository(file, name, vcfAttributes, entityMetaDataFactory, attributeMetaDataFactory);
	}

}
