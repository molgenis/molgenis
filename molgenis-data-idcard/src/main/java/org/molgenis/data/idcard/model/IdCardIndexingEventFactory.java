package org.molgenis.data.idcard.model;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class IdCardIndexingEventFactory
		extends AbstractSystemEntityFactory<IdCardIndexingEvent, IdCardIndexingEventMetaData, String>
{
	@Autowired
	IdCardIndexingEventFactory(IdCardIndexingEventMetaData idCardIndexingEventMetaData, EntityPopulator entityPopulator)
	{
		super(IdCardIndexingEvent.class, idCardIndexingEventMetaData, entityPopulator);
	}
}
