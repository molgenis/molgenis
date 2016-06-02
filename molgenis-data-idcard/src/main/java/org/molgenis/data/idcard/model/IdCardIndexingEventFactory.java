package org.molgenis.data.idcard.model;

import org.molgenis.data.AbstractEntityFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class IdCardIndexingEventFactory
		extends AbstractEntityFactory<IdCardIndexingEvent, IdCardIndexingEventMetaData, String>
{
	@Autowired
	IdCardIndexingEventFactory(IdCardIndexingEventMetaData idCardIndexingEventMetaData)
	{
		super(IdCardIndexingEvent.class, idCardIndexingEventMetaData, String.class);
	}
}
