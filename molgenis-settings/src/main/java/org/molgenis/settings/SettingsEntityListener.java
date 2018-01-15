package org.molgenis.settings;

import org.molgenis.data.Entity;

/**
 * Entity listeners can be added to settings entities to listen to setting updates.
 */
public interface SettingsEntityListener
{
	/**
	 * Callback that is fired when the settings entity is updated.
	 *
	 * @param entity settings entity
	 */
	void postUpdate(Entity entity);
}
