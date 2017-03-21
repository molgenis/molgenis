package org.molgenis.data.staticentity;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TestEntityStaticFactory
		extends AbstractSystemEntityFactory<TestEntityStatic, TestEntityStaticMetaData, String>
{
	@Autowired
	TestEntityStaticFactory(TestEntityStaticMetaData testEntityStaticMetaData, EntityPopulator entityPopulator)
	{
		super(TestEntityStatic.class, testEntityStaticMetaData, entityPopulator);
	}
}
