package consulo.internal.dotnet.asm.io;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

/**
 * License Apache 2.0
 *
 * From https://code.google.com/p/jmxflib/
 */
public class SeekableFileInputStream extends SeekableInputStream
{
	private final RandomAccessFile raf;
	private final FileInputStream fin;
	private final long startPosition;
	private InputStream cin;
	private long pos;

	public SeekableFileInputStream(File file, long offset) throws IOException
	{
		this(raf(file, offset));
	}

	private static RandomAccessFile raf(File file, long offset) throws IOException
	{
		RandomAccessFile raf = new RandomAccessFile(file, "r");
		if(offset != 0)
		{
			forceSeek(raf, offset);
		}
		return raf;
	}

	public SeekableFileInputStream(File file) throws IOException
	{
		this(raf(file, 0));
	}

	public SeekableFileInputStream(RandomAccessFile raf) throws IOException
	{
		this.raf = raf;
		this.fin = new FileInputStream(raf.getFD());
		this.startPosition = raf.getFilePointer();
		_reset();
	}

	private long count = 0;

	private void _reset() throws IOException
	{
		this.pos = raf.getFilePointer() - this.startPosition;
		this.count = 0;
		this.cin = new BufferedInputStream(fin, 4096);
	}

	@Override
	public int read() throws IOException
	{
		int read = cin.read();
		if(read != -1)
		{
			count++;
		}
		return checkRead(read);
	}

	@Override
	public int read(byte[] b) throws IOException
	{
		int read = cin.read(b);
		if(read != -1)
		{
			count += read;
		}
		return checkRead(read);
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException
	{
		int read = cin.read(b, off, len);
		if(read != -1)
		{
			count += read;
		}
		return checkRead(read);
	}

	@Override
	public synchronized void close() throws IOException
	{
		if(cin != null)
		{
			cin.close();
			cin = null;
			raf.close();
		}
	}

	@Override
	public long seek(long position) throws IOException
	{
		checkIdx(position);
		forceSeek(raf, startPosition + position);
		_reset();
		return position();
	}

	@Override
	public long position() throws IOException
	{
		return pos + count;
	}

	@Override
	public long length()
	{
		try
		{
			return raf.length() - startPosition;
		}
		catch(IOException e)
		{
			throw new RuntimeException(e);
		}
	}

}