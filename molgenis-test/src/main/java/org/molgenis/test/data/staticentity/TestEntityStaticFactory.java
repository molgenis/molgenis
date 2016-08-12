package org.molgenis.test.data.staticentity;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TestEntityStaticFactory
		extends AbstractSystemEntityFactory<TestEntityStatic, TestEntityStaticMetaData, String>
{
	@Autowired
	TestEntityStaticFactory(TestEntityStaticMetaData testEntityStaticMetaData)
	{
		super(TestEntityStatic.class, testEntityStaticMetaData);
	}
}
