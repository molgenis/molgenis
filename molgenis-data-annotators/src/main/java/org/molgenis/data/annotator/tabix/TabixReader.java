package org.molgenis.data.annotator.tabix;

/* The MIT License

 Copyright (c) 2010 Broad Institute.

 Permission is hereby granted, free of charge, to any person obtaining
 a copy of this software and associated documentation files (the
 "Software"), to deal in the Software without restriction, including
 without limitation the rights to use, copy, modify, merge, publish,
 distribute, sublicense, and/or sell copies of the Software, and to
 permit persons to whom the Software is furnished to do so, subject to
 the following conditions:

 The above copyright notice and this permission notice shall be
 included in all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 SOFTWARE.
 */

/* Contact: Heng Li <hengli@broadinstitute.org> */

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;

import net.sf.samtools.util.BlockCompressedInputStream;

public class TabixReader
{
	public String filename;
	public BlockCompressedInputStream blockCompressedInputStream;

	private int mPreset;
	private int mSc;
	private int mBc;
	private int mEc;
	private int mMeta;
	private int mSkip;
	private String[] mSeq;
	private TIndex[] mIndex;

	public HashMap<String, Integer> mChr2tid;

	private static int MAX_BIN = 37450;
	private static int TAD_MIN_CHUNK_GAP = 32768;
	private static int TAD_LIDX_SHIFT = 14;

	private static boolean less64(final long u, final long v)
	{ // unsigned 64-bit comparison
		return (u < v) ^ (u < 0) ^ (v < 0);
	}

	/**
	 * The constructor
	 *
	 * @param filename
	 *            File name of the data file
	 */
	public TabixReader(String filename) throws IOException
	{
		this.filename = filename;
		this.blockCompressedInputStream = new BlockCompressedInputStream(new File(filename));
		readIndex(filename);
	}

	private class TPair64 implements Comparable<TPair64>
	{
		long u, v;

		public TPair64(final long _u, final long _v)
		{
			u = _u;
			v = _v;
		}

		public TPair64(final TPair64 p)
		{
			u = p.u;
			v = p.v;
		}

		@Override
		public int compareTo(final TPair64 p)
		{
			return u == p.u ? 0 : ((u < p.u) ^ (u < 0) ^ (p.u < 0)) ? -1 : 1; // unsigned 64-bit comparison
		}
	};

	private class TIndex
	{
		HashMap<Integer, TPair64[]> b; // binning index
		long[] l; // linear index
	};

	private class TIntv
	{
		int internalChromosomeID, beginPosition, end;
	};

	private static int reg2bins(final int beginposition, final int endPosition, final int[] list)
	{
		int i = 0, k, end = endPosition;
		if (beginposition >= end) return 0;
		if (end >= 1 << 29) end = 1 << 29;
		--end;
		list[i++] = 0;
		for (k = 1 + (beginposition >> 26); k <= 1 + (end >> 26); ++k)
			list[i++] = k;
		for (k = 9 + (beginposition >> 23); k <= 9 + (end >> 23); ++k)
			list[i++] = k;
		for (k = 73 + (beginposition >> 20); k <= 73 + (end >> 20); ++k)
			list[i++] = k;
		for (k = 585 + (beginposition >> 17); k <= 585 + (end >> 17); ++k)
			list[i++] = k;
		for (k = 4681 + (beginposition >> 14); k <= 4681 + (end >> 14); ++k)
			list[i++] = k;
		return i;
	}

	public static int readInt(final InputStream is) throws IOException
	{
		byte[] buf = new byte[4];
		is.read(buf);
		return ByteBuffer.wrap(buf).order(ByteOrder.LITTLE_ENDIAN).getInt();
	}

	public static long readLong(final InputStream is) throws IOException
	{
		byte[] buf = new byte[8];
		is.read(buf);
		return ByteBuffer.wrap(buf).order(ByteOrder.LITTLE_ENDIAN).getLong();
	}

	public static String readLine(final InputStream is) throws IOException
	{
		StringBuffer buf = new StringBuffer();
		int c;
		while ((c = is.read()) >= 0 && c != '\n')
			buf.append((char) c);
		if (c < 0) return null;
		return buf.toString();
	}

	/**
	 * Read the Tabix index from a file
	 *
	 * @param file
	 *            File pointer
	 */
	public void readIndex(final File file) throws IOException
	{
		if (file == null) return;
		BlockCompressedInputStream is = new BlockCompressedInputStream(file);
		byte[] buf = new byte[4];

		is.read(buf, 0, 4); // read "TBI\1"
		mSeq = new String[readInt(is)]; // # sequences
		mChr2tid = new HashMap<String, Integer>();
		mPreset = readInt(is);
		mSc = readInt(is);
		mBc = readInt(is);
		mEc = readInt(is);
		mMeta = readInt(is);
		mSkip = readInt(is);
		// read sequence dictionary
		int i, j, k, l = readInt(is);
		buf = new byte[l];
		is.read(buf);
		for (i = j = k = 0; i < buf.length; ++i)
		{
			if (buf[i] == 0)
			{
				byte[] b = new byte[i - j];
				System.arraycopy(buf, j, b, 0, b.length);
				String s = new String(b, Charset.forName("UTF-8"));
				mChr2tid.put(s, k);
				mSeq[k++] = s;
				j = i + 1;
			}
		}
		// read the index
		mIndex = new TIndex[mSeq.length];
		for (i = 0; i < mSeq.length; ++i)
		{
			// the binning index
			int n_bin = readInt(is);
			mIndex[i] = new TIndex();
			mIndex[i].b = new HashMap<Integer, TPair64[]>();
			for (j = 0; j < n_bin; ++j)
			{
				int bin = readInt(is);
				TPair64[] chunks = new TPair64[readInt(is)];
				for (k = 0; k < chunks.length; ++k)
				{
					long u = readLong(is);
					long v = readLong(is);
					chunks[k] = new TPair64(u, v); // in C, this is inefficient
				}
				mIndex[i].b.put(bin, chunks);
			}
			// the linear index
			mIndex[i].l = new long[readInt(is)];
			for (k = 0; k < mIndex[i].l.length; ++k)
				mIndex[i].l[k] = readLong(is);
		}
		// close
		is.close();
	}

	/**
	 * Read the Tabix index from the default file.
	 */
	public void readIndex(String fileName) throws IOException
	{
		readIndex(new File(filename + ".tbi"));
	}

	/**
	 * Read one line from the data file.
	 */
	public String readLine() throws IOException
	{
		return readLine(blockCompressedInputStream);
	}

	private int chr2tid(final String chr)
	{
		if (mChr2tid.containsKey(chr)) return mChr2tid.get(chr);
		else return -1;
	}

	/**
	 * Parse a region in the format of "chr1", "chr1:100" or "chr1:100-1000"
	 *
	 * @param queryString
	 *            Region string
	 * @return An array where the three elements are sequence_id, region_begin and region_end. On failure,
	 *         sequence_id==-1.
	 */
	private int[] parseReg(final String queryString)
	{ // FIXME: NOT working when the sequence name contains : or -.
		String chr;
		int colon, hyphen;
		int[] ret = new int[3];
		colon = queryString.indexOf(':');
		hyphen = queryString.indexOf('-');
		chr = colon >= 0 ? queryString.substring(0, colon) : queryString;
		ret[1] = colon >= 0 ? Integer.parseInt(queryString.substring(colon + 1,
				hyphen >= 0 ? hyphen : queryString.length())) - 1 : 0;
		ret[2] = hyphen >= 0 ? Integer.parseInt(queryString.substring(hyphen + 1)) : 0x7fffffff;
		ret[0] = chr2tid(chr);
		return ret;
	}

	private TIntv getIntv(final String s)
	{
		TIntv intv = new TIntv();
		int col = 0, end = 0, beg = 0;
		while ((end = s.indexOf('\t', beg)) >= 0 || end == -1)
		{
			++col;
			if (col == mSc)
			{
				intv.internalChromosomeID = chr2tid(s.substring(beg, end));
			}
			else if (col == mBc)
			{
				intv.beginPosition = intv.end = Integer.parseInt(s.substring(beg, end == -1 ? s.length() : end));
				if ((mPreset & 0x10000) != 0) ++intv.end;
				else --intv.beginPosition;
				if (intv.beginPosition < 0) intv.beginPosition = 0;
				if (intv.end < 1) intv.end = 1;
			}
			else
			{ // FIXME: SAM supports are not tested yet
				if ((mPreset & 0xffff) == 0)
				{ // generic
					if (col == mEc) intv.end = Integer.parseInt(s.substring(beg, end));
				}
				else if ((mPreset & 0xffff) == 1)
				{ // SAM
					if (col == 6)
					{ // CIGAR
						int l = 0, i, j;
						String cigar = s.substring(beg, end);
						for (i = j = 0; i < cigar.length(); ++i)
						{
							if (cigar.charAt(i) > '9')
							{
								int op = cigar.charAt(i);
								if (op == 'M' || op == 'D' || op == 'N') l += Integer.parseInt(cigar.substring(j, i));
							}
						}
						intv.end = intv.beginPosition + l;
					}
				}
				else if ((mPreset & 0xffff) == 2)
				{ // VCF
					String alt;
					alt = end >= 0 ? s.substring(beg, end) : s.substring(beg);
					if (col == 4)
					{ // REF
						if (alt.length() > 0) intv.end = intv.beginPosition + alt.length();
					}
					else if (col == 8)
					{ // INFO
						int e_off = -1, i = alt.indexOf("END=");
						if (i == 0) e_off = 4;
						else if (i > 0)
						{
							i = alt.indexOf(";END=");
							if (i >= 0) e_off = i + 5;
						}
						if (e_off > 0)
						{
							i = alt.indexOf(";", e_off);
							intv.end = Integer.parseInt(i > e_off ? alt.substring(e_off, i) : alt.substring(e_off));
						}
					}
				}
			}
			if (end == -1) break;
			beg = end + 1;
		}
		return intv;
	}

	public class Iterator
	{
		private int i;
		private final int internalChromosomeID, beginPosition, endPosition;
		private final TPair64[] off;
		private long curr_off;
		private boolean isEndOfFile;

		public Iterator(final int _tid, final int _beg, final int _end, final TPair64[] _off)
		{
			i = -1;
			curr_off = 0;
			isEndOfFile = false;
			off = _off;
			internalChromosomeID = _tid;
			beginPosition = _beg;
			endPosition = _end;
		}

		public String next() throws IOException
		{
			if (isEndOfFile) return null;
			for (;;)
			{
				synchronized (blockCompressedInputStream)
				{
					if (curr_off == 0 || !less64(curr_off, off[i].v))
					{ // then jump to the next chunk
						if (i == off.length - 1) break; // no more chunks
						if (i >= 0) assert (curr_off == off[i].v); // otherwise bug
						if (i < 0 || off[i].v != off[i + 1].u)
						{ // not adjacent chunks; then seek
							blockCompressedInputStream.seek(off[i + 1].u);
							curr_off = blockCompressedInputStream.getFilePointer();
						}
						++i;
					}
					else
					{
						blockCompressedInputStream.seek(curr_off);
					}
					String s;
					if ((s = readLine(blockCompressedInputStream)) != null)
					{
						TIntv intv;
						char[] str = s.toCharArray();
						curr_off = blockCompressedInputStream.getFilePointer();
						if (str.length == 0 || str[0] == mMeta) continue;
						intv = getIntv(s);
						if (intv.internalChromosomeID != internalChromosomeID || intv.beginPosition >= endPosition) break; // no
						// need
						// to
						// proceed
						else if (intv.end > beginPosition && intv.beginPosition < endPosition) return s; // overlap;
						// return
					}
					else break; // end of file
				}
			}
			isEndOfFile = true;
			return null;
		}
	};

	public Iterator query(final int internalChromosomeID, final int beginPosition, final int endPosition)
	{
		TPair64[] off, chunks;
		long min_off;
		TIndex idx = mIndex[internalChromosomeID];
		int[] bins = new int[MAX_BIN];
		int i, l, n_off, n_bins = reg2bins(beginPosition, endPosition, bins);
		if (idx.l.length > 0) min_off = (beginPosition >> TAD_LIDX_SHIFT >= idx.l.length) ? idx.l[idx.l.length - 1] : idx.l[beginPosition >> TAD_LIDX_SHIFT];
		else min_off = 0;
		for (i = n_off = 0; i < n_bins; ++i)
		{
			if ((chunks = idx.b.get(bins[i])) != null) n_off += chunks.length;
		}
		if (n_off == 0) return null;
		off = new TPair64[n_off];
		for (i = n_off = 0; i < n_bins; ++i)
			if ((chunks = idx.b.get(bins[i])) != null) for (int j = 0; j < chunks.length; ++j)
				if (less64(min_off, chunks[j].v)) off[n_off++] = new TPair64(chunks[j]);
		if (n_off == 0) return null;
		Arrays.sort(off, 0, n_off);
		// resolve completely contained adjacent blocks
		for (i = 1, l = 0; i < n_off; ++i)
		{
			if (less64(off[l].v, off[i].v))
			{
				++l;
				off[l].u = off[i].u;
				off[l].v = off[i].v;
			}
		}
		n_off = l + 1;
		// resolve overlaps between adjacent blocks; this may happen due to the merge in indexing
		for (i = 1; i < n_off; ++i)
			if (!less64(off[i - 1].v, off[i].u)) off[i - 1].v = off[i].u;
		// merge adjacent blocks
		for (i = 1, l = 0; i < n_off; ++i)
		{
			if (off[l].v >> 16 == off[i].u >> 16) off[l].v = off[i].v;
			else
			{
				++l;
				off[l].u = off[i].u;
				off[l].v = off[i].v;
			}
		}
		n_off = l + 1;
		// return
		TPair64[] ret = new TPair64[n_off];
		for (i = 0; i < n_off; ++i)
			ret[i] = new TPair64(off[i].u, off[i].v); // in C, this is inefficient
		return new TabixReader.Iterator(internalChromosomeID, beginPosition, endPosition, ret);
	}

	public Iterator query(final String queryString)
	{
		int[] x = parseReg(queryString);
		int beginPosition = x[0];
		int endPosition = x[1];
		int internalChromosomeID = x[2];
		return query(beginPosition, endPosition, internalChromosomeID);
	}
}
