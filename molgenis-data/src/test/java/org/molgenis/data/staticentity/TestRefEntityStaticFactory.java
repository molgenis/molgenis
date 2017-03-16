package org.molgenis.data.staticentity;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TestRefEntityStaticFactory
		extends AbstractSystemEntityFactory<TestRefEntityStatic, TestRefEntityStaticMetaData, String>
{
	@Autowired
	TestRefEntityStaticFactory(TestRefEntityStaticMetaData testRefEntityStaticMetaData, EntityPopulator entityPopulator)
	{
		super(TestRefEntityStatic.class, testRefEntityStaticMetaData, entityPopulator);
	}
}
