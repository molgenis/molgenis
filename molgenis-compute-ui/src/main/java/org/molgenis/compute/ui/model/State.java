package org.molgenis.compute.ui.model;

import java.util.EnumSet;

/**
 * State machine state
 * 
 * @param <E>
 */
public interface State<E extends Enum<E>>
{
	/**
	 * Initial state in a state machine
	 * 
	 * @return
	 */
	boolean isInitialState();

	/**
	 * Final state in a state machine
	 * 
	 * @return
	 */
	boolean isFinalState();

	/**
	 * Returns allowed state transitions from this state
	 * 
	 * @return
	 */
	EnumSet<E> getStateTransitions();
}
