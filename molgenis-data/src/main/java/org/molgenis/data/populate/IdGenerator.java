package org.molgenis.data.populate;

/**
 * Generates identifiers.
 */
public interface IdGenerator
{
	/**
	 * The ID generation strategy. Strategies available are:
	 * <li>{@link #SEQUENTIAL_UUID}</li>
	 * <li>{@link #SECURE_RANDOM}</li>
	 * <li>{@link #SHORT_RANDOM}</li>
	 * <li>{@link #SHORT_SECURE_RANDOM}</li>
	 */
	enum Strategy
	{
		/**
		 * Globally unique sequential UUIDs. 26 characters long, base32 encoded.
		 * The IDs are based on the current epoch time, current machine identity, and a counter.
		 * The result are mostly ordered unique ids that require no synchronization between machines.
		 */
		SEQUENTIAL_UUID, /**
	 * Cryptographically safe random IDs, usable as a token. 32 characters long, base32 encoded
	 */
	SECURE_RANDOM, /**
	 * Short IDs. 8 characters long, base32 encoded
	 */
	SHORT_RANDOM, /**
	 * Cryptographically safe random IDs, usable as a password. 8 characters long, base32 encoded
	 */
	SHORT_SECURE_RANDOM, /**
	 * Cryptographically safe random IDs, usable as a password. 20 characters long, base32 encoded
	 */
	LONG_SECURE_RANDOM
	}

	/**
	 * Generate a unique id using the {@link Strategy#SEQUENTIAL_UUID} strategy.
	 *
	 * @return the generated String
	 */
	String generateId();

	/**
	 * Generates a unique id using the given strategy.
	 *
	 * @param strategy the {@link Strategy} to use
	 * @return the generated String
	 */
	String generateId(Strategy strategy);
}