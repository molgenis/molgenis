package org.molgenis.data.populate;

import com.google.common.io.BaseEncoding;

import java.security.SecureRandom;

/**
 * Generates securely random IDs of arbitrary length.
 */
public class RandomIdGenerator
{
	private static final BaseEncoding encoding = BaseEncoding.base32().lowerCase().omitPadding();

	private static class Holder
	{
		static final SecureRandom numberGenerator = new SecureRandom();
	}

	/**
	 * Generates a long random identifier, 26 characters long.
	 */
	public String generateId()
	{
		return encodeRandomBytes(16);
	}

	/**
	 * Generates a short random identifier, 8 characters long.
	 * <p>
	 * The chance of collisions of k IDs taken from a population of N possibilities is
	 * <code>1 - Math.exp(-0.5 * k * (k - 1) / N)</code>
	 * <p>
	 * A couple collision chances for 5 bytes / 8 characters / <code>N = 256^5 = 32^8</code>:
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
	public String generateShortId()
	{
		return encodeRandomBytes(5);
	}

	/**
	 * Generates a specified number of bytes and encodes them using base32 encoding.
	 * 5 bytes get converted to 8 characters
	 *
	 * @param nBytes number of bytes to encode
	 * @return base32 encoded bytes
	 */
	public String encodeRandomBytes(int nBytes)
	{
		SecureRandom ng = Holder.numberGenerator;
		byte[] randomBytes = new byte[nBytes];
		ng.nextBytes(randomBytes);
		return encoding.encode(randomBytes);
	}
}