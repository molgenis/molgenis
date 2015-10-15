package org.molgenis.rdconnect;

import org.quartz.SchedulerException;
import org.quartz.Trigger.TriggerState;
import org.quartz.TriggerKey;

public interface IdCardBiobankService
{
	TriggerKey scheduleIndexRebuild() throws SchedulerException;

	TriggerState getIndexRebuildStatus(TriggerKey triggerKey) throws SchedulerException;
}
