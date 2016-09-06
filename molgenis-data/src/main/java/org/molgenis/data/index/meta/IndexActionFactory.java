package org.molgenis.data.index.meta;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class IndexActionFactory extends AbstractSystemEntityFactory<IndexAction, IndexActionMetaData, String>
{
	@Autowired
	IndexActionFactory(IndexActionMetaData indexActionMetaData)
	{
		super(IndexAction.class, indexActionMetaData);
	}
}
