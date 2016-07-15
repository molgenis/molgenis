package org.molgenis.test.data.staticentity;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TestRefEntityStaticFactory extends AbstractSystemEntityFactory<TestRefEntityStatic, TestRefEntityStaticMetaData, String>
{
	@Autowired
	TestRefEntityStaticFactory(TestRefEntityStaticMetaData testRefEntityStaticMetaData)
	{
		super(TestRefEntityStatic.class, testRefEntityStaticMetaData);
	}
}
