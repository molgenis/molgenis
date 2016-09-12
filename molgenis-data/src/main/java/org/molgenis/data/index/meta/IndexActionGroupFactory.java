package org.molgenis.data.index.meta;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class IndexActionGroupFactory
		extends AbstractSystemEntityFactory<IndexActionGroup, IndexActionGroupMetaData, String>
{
	@Autowired
	IndexActionGroupFactory(IndexActionGroupMetaData indexActionGroupMetaData)
	{
		super(IndexActionGroup.class, indexActionGroupMetaData);
	}
}
