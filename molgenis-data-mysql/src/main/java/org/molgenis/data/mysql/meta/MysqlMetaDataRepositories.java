package org.molgenis.data.mysql.meta;

import org.molgenis.data.meta.AttributeMetaDataRepositoryDecoratorFactory;
import org.molgenis.data.meta.EntityMetaDataRepositoryDecoratorFactory;
import org.molgenis.data.meta.MetaDataRepositories;
import org.molgenis.data.mysql.MysqlRepositoryCollection;

public class MysqlMetaDataRepositories implements MetaDataRepositories
{
	public MysqlPackageRepository packageRepository;
	public MysqlEntityMetaDataRepository entityMetaDataRepository;
	public MysqlAttributeMetaDataRepository attributeMetaDataRepository;
	public EntityMetaDataRepositoryDecoratorFactory entityMetaDataRepositoryDecoratorFactory;
	public AttributeMetaDataRepositoryDecoratorFactory attributeMetaDataRepositoryDecoratorFactory;

	public MysqlMetaDataRepositories(MysqlPackageRepository packageRepository,
			MysqlEntityMetaDataRepository entityMetaDataRepository,
			MysqlAttributeMetaDataRepository attributeMetaDataRepository)
	{
		this(packageRepository, entityMetaDataRepository, attributeMetaDataRepository, null, null);
	}

	public MysqlMetaDataRepositories(MysqlPackageRepository packageRepository,
			MysqlEntityMetaDataRepository entityMetaDataRepository,
			MysqlAttributeMetaDataRepository attributeMetaDataRepository,
			EntityMetaDataRepositoryDecoratorFactory entityMetaDataRepositoryDecoratorFactory,
			AttributeMetaDataRepositoryDecoratorFactory attributeMetaDataRepositoryDecoratorFactory)
	{
		this.packageRepository = packageRepository;
		this.entityMetaDataRepository = entityMetaDataRepository;
		this.attributeMetaDataRepository = attributeMetaDataRepository;
		this.entityMetaDataRepositoryDecoratorFactory = entityMetaDataRepositoryDecoratorFactory;
		this.attributeMetaDataRepositoryDecoratorFactory = attributeMetaDataRepositoryDecoratorFactory;
	}

	public void setRepositoryCollection(MysqlRepositoryCollection mysqlRepositoryCollection)
	{
		packageRepository.setRepositoryCollection(mysqlRepositoryCollection);
		entityMetaDataRepository.setRepositoryCollection(mysqlRepositoryCollection);
		attributeMetaDataRepository.setRepositoryCollection(mysqlRepositoryCollection);
	}
}