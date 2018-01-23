package org.molgenis.data.populate;

import com.google.common.io.BaseEncoding;
import org.molgenis.data.util.UniqueId;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import static org.molgenis.data.populate.IdGenerator.Strategy.SEQUENTIAL_UUID;

/**
 * Generates identifiers.
 */
@Component
public class IdGeneratorImpl implements IdGenerator
{
	private static final BaseEncoding BASE_ENCODING = BaseEncoding.base32().omitPadding().lowerCase();

	private static class Holder
	{
		static final SecureRandom SECURE_RANDOM = new SecureRandom();
		static final UniqueId UNIQUE_ID = new UniqueId();
	}

	@Override
	public String generateId()
	{
		return generateId(SEQUENTIAL_UUID);
	}

	@Override
	public String generateId(Strategy strategy)
	{
		byte[] bytes = generateBytes(strategy);
		return BASE_ENCODING.encode(bytes);
	}

	private byte[] generateBytes(Strategy strategy)
	{
		switch (strategy)
		{
			case SECURE_RANDOM:
				return generateRandomBytes(16, Holder.SECURE_RANDOM);
			case LONG_SECURE_RANDOM:
				return generateRandomBytes(20, Holder.SECURE_RANDOM);
			case SHORT_SECURE_RANDOM:
				return generateRandomBytes(5, Holder.SECURE_RANDOM);
			case SHORT_RANDOM:
				return generateRandomBytes(5, ThreadLocalRandom.current());
			case SEQUENTIAL_UUID:
			default:
				return Holder.UNIQUE_ID.getId();
		}
	}

	/**
	 * Generates a number of random bytes.
	 * <p>
	 * The chance of collisions of k IDs taken from a population of N possibilities is
	 * <code>1 - Math.exp(-0.5 * k * (k - 1) / N)</code>
	 * <p>
	 * A couple collision chances for 5 bytes <code>N = 256^5</code>:
	 * <table>
	 * <tr><td> 1 </td><td> 0.0 </td></tr>
	 * <tr><td> 10 </td><td> 4.092726157978177e-11 </td></tr>
	 * <tr><td> 100 </td><td> 4.501998773775995e-09 </td></tr>
	 * <tr><td> 1000 </td><td> 4.5429250039585867e-07 </td></tr>
	 * <tr><td> 10000 </td><td> 4.546915386183237e-05 </td></tr>
	 * <tr><td> 100000 </td><td> 0.004537104138253034 </td></tr>
	 * <tr><td> 1000000 </td><td> 0.36539143049797307 </td></tr>
	 * </table>
	 *
	 * @see <a href="http://preshing.com/20110504/hash-collision-probabilities/">Hash collision probabilities</a>
	 */
	private byte[] generateRandomBytes(int nBytes, Random random)
	{
		byte[] randomBytes = new byte[nBytes];
		random.nextBytes(randomBytes);
		return randomBytes;
	}
}