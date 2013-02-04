package org.molgenis.util.plink.drivers;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Driver to query BED (binary Plink genotype) files. See:
 * http://pngu.mgh.harvard.edu/~purcell/plink/binary.shtml
 */
public class BedFileDriver
{
	private int mode;
	private long nrOfElements;
	private File bedFile;

	/**
	 * Get the mode: mode 1 = SNP-major, mode 0 = individual-major
	 * 
	 * @return
	 */
	public int getMode()
	{
		return mode;
	}

	/**
	 * Get the number of retrievable genotype elements of this BED file. Does
	 * not account for trailing null elements because they are indistinguishable
	 * from null genotypes in this format alone.
	 * 
	 * @return
	 */
	public long getNrOfElements()
	{
		return nrOfElements;
	}

	/**
	 * Construct new convertGenoCoding on this file
	 * 
	 * @param bedFile
	 * @throws Exception
	 */
	public BedFileDriver(File bedFile) throws Exception
	{
		RandomAccessFile raf = new RandomAccessFile(bedFile, "r");
		try
		{
			this.bedFile = bedFile;

			byte mn1 = raf.readByte();
			byte mn2 = raf.readByte();

			if (mn1 == 108 && mn2 == 27) // tested, bit code 01101100 00011011
			{
				// System.out.println("Plink magic number valid");
			}
			else
			{
				throw new Exception("Invalid Plink magic number");
			}

			byte bmode = raf.readByte();

			if (bmode == 1) // tested, bit code 00000001
			{
				// System.out.println("mode 1: SNP-major");
			}
			else if (bmode == 0) // assumed... bit code 00000000
			{
				// System.out.println("mode 0: individual-major");
			}
			else
			{
				throw new Exception("Mode not recognized: " + bmode);
			}

			this.mode = bmode;
			this.nrOfElements = (raf.length() - 3) * 4;
		}
		finally
		{
			raf.close();
		}
	}

	/**
	 * Convert bit coding in custom genotype coding.
	 * 
	 * @param in
	 * @param hom1
	 * @param hom2
	 * @param het
	 * @param _null
	 * @return
	 * @throws Exception
	 */
	public String convertGenoCoding(String in, String hom1, String hom2, String het, String _null) throws Exception
	{
		if (in.equals("00"))
		{
			return hom1;
		}
		if (in.equals("01"))
		{
			return het;
		}
		if (in.equals("11"))
		{
			return hom2;
		}
		if (in.equals("10"))
		{
			return _null;
		}
		throw new Exception("Input '" + in + "' not recognized");
	}

	/**
	 * Convert bit coding in common genotype signs: A & B for homozygotes, H for
	 * heterozygote, N for null.
	 * 
	 * @param in
	 * @return
	 * @throws Exception
	 */
	public String genoCodingCommon(String in) throws Exception
	{
		return convertGenoCoding(in, "A", "B", "H", "N");
	}

	/**
	 * Get a single element from the BED file
	 * 
	 * @param index
	 * @return
	 * @throws Exception
	 */
	public String getElement(long index) throws Exception
	{
		// throw new Exception("fixme!");
		RandomAccessFile raf = new RandomAccessFile(bedFile, "r");
		raf.seek((index / 4) + 3);
		String byteString = reverse(bits(raf.readByte()));
		raf.close();
		int bitpair = (int) (index % 4) * 2;
		return byteString.substring(bitpair, bitpair + 2);
	}

	/**
	 * Get a String[] of elements from the BED file.
	 * 
	 * from = inclusive to = exclusive
	 * 
	 * @param from
	 * @param to
	 * @return
	 * @throws IOException
	 */
	public String[] getElements(long from, long to, int paddingBitpairs, int pass) throws IOException
	{
		double paddingFraction = paddingBitpairs / 4.0;
		// Start byte = byte position of start individual, corrected for padding
		// 0's that get added at every SNP:
		long start = (long) ((from / 4.0) - (pass * paddingFraction) + pass + 3);
		// Stop byte = byte position after last individual, corrected for
		// padding 0's that get added at every SNP:
		long stop = (long) ((to / 4.0) - ((pass + 1) * paddingFraction) + (pass + 1) + 3);
		byte[] res = new byte[(int) (stop - start)];
		int res_index = 0;
		String[] result = new String[(int) (to - from)]; // to - from = nr. of
															// individuals

		RandomAccessFile raf = new RandomAccessFile(bedFile, "r");
		raf.seek(start);
		raf.read(res);
		raf.close();

		for (int i = 0; i < res.length; i++)
		{
			byte b = res[i];
			String byteString = reverse(bits(b));

			int toBit = 8; // normally we take the whole byte
			if (i == res.length - 1) // except at the end, when we correct for
										// padding 0's
			{
				// At the end, the string is padded with 0's -> check
				for (int j = paddingBitpairs * 2; j < 8; j++)
				{
					if (byteString.charAt(j) != '0')
					{
						throw new IOException("Fatal error: padding 0's not present where expected!");
					}
				}
				toBit -= (paddingBitpairs * 2);
			}

			for (int pair = 0; pair < toBit; pair += 2)
			{
				// System.out.print(res_index + " ");
				result[res_index++] = byteString.substring(pair, pair + 2);
			}
		}
		// System.out.println("");
		return result;
	}

	/**
	 * Helper function to get the bit values
	 * 
	 * @param b
	 * @return
	 */
	private String bits(byte b)
	{
		StringBuilder bitsBuilder = new StringBuilder();
		for (int bit = 7; bit >= 0; --bit)
		{
			bitsBuilder.append(((b >>> bit) & 1));
		}
		return bitsBuilder.toString();
	}

	/**
	 * Helper function to reverse a string
	 * 
	 * @param string
	 * @return
	 */
	private String reverse(String string)
	{
		return new StringBuffer(string).reverse().toString();
	}

}
