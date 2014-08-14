package org.molgenis.data.mysql;

import static org.molgenis.util.SecurityDecoratorUtils.validatePermission;

import org.molgenis.data.AggregateResult;
import org.molgenis.data.Aggregateable;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.CrudRepositorySecurityDecorator;
import org.molgenis.data.Query;
import org.molgenis.security.core.Permission;

public class MysqlSecurityDecorator extends CrudRepositorySecurityDecorator implements Aggregateable
{
	private final MysqlRepository decoratedRepository;

	public MysqlSecurityDecorator(MysqlRepository decoratedRepository)
	{
		super(decoratedRepository);
		this.decoratedRepository = decoratedRepository;
	}

	@Override
	public AggregateResult aggregate(AttributeMetaData xAttr, AttributeMetaData yAttr, Query q)
	{
		validatePermission(decoratedRepository.getName(), Permission.COUNT);
		return decoratedRepository.aggregate(xAttr, yAttr, q);
	}

}
