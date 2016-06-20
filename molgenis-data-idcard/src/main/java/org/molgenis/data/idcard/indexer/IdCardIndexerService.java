package org.molgenis.data.idcard.indexer;

import org.quartz.SchedulerException;
import org.quartz.Trigger.TriggerState;
import org.quartz.TriggerKey;

public interface IdCardIndexerService
{
	TriggerKey scheduleIndexRebuild() throws SchedulerException;

	TriggerState getIndexRebuildStatus(TriggerKey triggerKey) throws SchedulerException;

	void updateIndexerScheduler(boolean initScheduler) throws SchedulerException;
}
