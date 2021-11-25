package org.molgenis.util;

import static java.lang.Integer.remainderUnsigned;
import static java.lang.Math.round;
import static java.lang.Math.sqrt;

import java.math.BigInteger;
import java.text.DecimalFormat;
import java.util.regex.Pattern;

/**
 * Scrambles the integers in the space <code>0,...,M-1</code> using <a
 * href="https://en.wikipedia.org/wiki/Hash_function#Fibonacci_hashing" target="_top">Fibonacci
 * hashing</a>.
 *
 * <p>We want to scramble, not hash, so we set <code>W = M = 2^m</code>. This removes the range
 * reducing bit shift at the end of the operation. The scramble function then is simply <code>
 * k -> ak mod M</code> This scramble version can be inverted, which means that a scrambled sequence
 * of unique numbers is still unique.
 */
public class IntScrambler {

  // the golden ratio
  private static final double PHI = (1 + sqrt(5)) / 2;
  private final int a;
  private final int M;

  /**
   * Create a new scrambler.
   *
   * @param m the maximum number of bits the scrambled ints will have
   */
  IntScrambler(int m) {
    M = 2 << m;
    // Choose a close to W / phi
    // but make sure a and M are relatively prime
    int aCandidate = (int) round(M / PHI);
    while (!bigIntegerRelativelyPrime(aCandidate, M)) {
      aCandidate++;
    }
    a = aCandidate;
  }

  @SuppressWarnings("ResultOfMethodCallIgnored")
  public static IntScrambler forDecimalFormat(DecimalFormat decimalFormat) {
    var matcher = Pattern.compile("0+").matcher(decimalFormat.toPattern());
    matcher.find();
    var zeroes = matcher.group(0).length();
    var maxValue = (int) Math.pow(10, zeroes);
    var m = 30 - Integer.numberOfLeadingZeros(maxValue - 1);
    return new IntScrambler(m);
  }

  private boolean bigIntegerRelativelyPrime(int a, int b) {
    return BigInteger.valueOf(a).gcd(BigInteger.valueOf(b)).equals(BigInteger.ONE);
  }

  public int scramble(int k) {
    return remainderUnsigned(k * a, M);
  }

  public int getMaxValue() {
    return M - 1;
  }
}
