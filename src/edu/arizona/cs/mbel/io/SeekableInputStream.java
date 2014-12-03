package edu.arizona.cs.mbel.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

/**
 * License Apache 2.0
 *
 * From https://code.google.com/p/jmxflib/
 */
public abstract class SeekableInputStream extends InputStream
{

	public abstract long seek(long position) throws IOException;

	public abstract long length() throws IOException;

	public abstract long position() throws IOException;

	@Override
	public synchronized void reset() throws IOException
	{
		seek(0);
	}

	public static long forceSeek(RandomAccessFile raf, long offset) throws IOException
	{
		assert offset >= 0;
		long length = raf.length();
		assert offset <= length : "trying to seek to (" + (offset) + ") outside of file len (" + length + ")";
		raf.seek(offset);
		assert offset == raf.getFilePointer();
		return offset;
	}

	protected void checkIdx(long position) throws IOException
	{
		long len = length();
		if(position < 0 || position > len)
		{
			throw new IndexOutOfBoundsException("trying to seek to " + position + " while len is " + len);
		}
	}

	protected final int checkRead(int read) throws IOException
	{
		if(read != -1)
		{
			return read;
		}
		long pos = position();
		long len = length();
		if(pos != len)
		{
			throw new IllegalStateException("position " + pos + " at end of stream != length " + len);
		}
		return read;
	}

	@Override
	public long skip(long n) throws IOException
	{
		long pos = position();
		long newpos = seek(pos + n);
		return newpos - pos;
	}
}