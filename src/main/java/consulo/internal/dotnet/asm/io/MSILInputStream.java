/* MBEL: The Microsoft Bytecode Engineering Library
 * Copyright (C) 2003 The University of Arizona
 * http://www.cs.arizona.edu/mbel/license.html
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package consulo.internal.dotnet.asm.io;

import consulo.internal.dotnet.asm.parse.SectionHeader;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;

/**
 * This is an input stream that buffers the entire PE/COFF file in advance.
 * Since even the main library files (like mscorlib.dll) are no more than ~2mb,
 * this class should never have any problems with using too much memory.
 * All multi-byte integers are read little-endian.
 *
 * @author Michael Stepp
 */
public class MSILInputStream implements Closeable
{
	private SeekableFileInputStream mySeekableFileInputStream;
	private SectionHeader[] headers;
	private final long size;

	/**
	 * Creates a MSILInputStream from the given InputStream and buffers all the available data.
	 * Note: if input is a network stream, this constructor will only buffer the
	 * data that is available at the time the constructor is called (i.e. as much as is reported by
	 * InputStream.available()).
	 */
	public MSILInputStream(File file) throws IOException
	{
		mySeekableFileInputStream = new SeekableFileInputStream(file);
		// reads in an entire input stream and buffers it
		size = mySeekableFileInputStream.length();
	}

	/**
	 * Moves the file pointer to the given location.
	 */
	public void seek(long pos) throws IOException
	{
		// skips to absolute location 'point' in the file
		if(pos < 0 || pos >= size)
		{
			throw new IOException("MSILInputStream.seek: Seek position outside of file bounds: " + pos);
		}

		mySeekableFileInputStream.seek(pos);
	}

	/**
	 * This method allows the MSILInputStream to computer file offsets from RVAs in the file.
	 * This method must be called before any calls to getFilePointer are made.
	 */
	public void activate(SectionHeader[] hdrs)
	{
		headers = hdrs;
	}

	/**
	 * Returns the file pointer equivalent of the given RVA.
	 * This method cannot be called until activate is called.
	 */
	public long getFilePointer(long RVA)
	{
		for(SectionHeader header : headers)
		{
			if(header.VirtualAddress <= RVA && (header.VirtualAddress + header.SizeOfRawData) > RVA)
			{
				return ((RVA - header.VirtualAddress) + header.PointerToRawData);
			}
		}
		return -1L;
	}

	/**
	 * Reads a null-terminated ASCII string from the file starting
	 * at the current location and ending at the next 0x00 ('\0') byte.
	 */
	public String readASCII() throws IOException
	{
		StringBuilder builder = new StringBuilder();
		int BYTE;
		while((BYTE = readBYTE()) != 0)
		{
			builder.append((char) BYTE);
		}
		return builder.toString();
	}

	/**
	 * Returns the current file position of this input stream
	 */
	public long getCurrent() throws IOException
	{
		return (long) mySeekableFileInputStream.position();
	}

	/**
	 * Asserts that the next bytes to be read from the input stream equal the
	 * byte array given. If sucessful, returns true and advances the file pointer by
	 * bytes.length. If unsuccessful, will either throw an exception (if premature EOF)
	 * or will returns false. The file position after an unsuccessful match is undefined,
	 * but will be somewhere between start and (start+bytes.length).
	 */
	public boolean match(byte[] bytes) throws IOException
	{
		if((mySeekableFileInputStream.position() + bytes.length) > size)
		{
			return false;
		}

		byte[] equalArray = new byte[bytes.length];
		mySeekableFileInputStream.read(equalArray);
		return isEqual(equalArray, bytes);
	}

	private static boolean isEqual(byte[] o1, byte[] o2)
	{
		if(o1.length != o2.length)
		{
			return false;
		}
		for(int i = 0; i < o1.length; i++)
		{
			byte b1 = o1[i];
			byte b2 = o2[i];
			if(b1 != b2)
			{
				return false;
			}
		}
		return true;
	}

	/**
	 * Advances the file pointer by 'amount'.
	 *
	 * @param amount the amount to skip, may be negative.
	 * @return true iff the skip was successful
	 */
	public boolean skip(int amount) throws IOException
	{
		if((mySeekableFileInputStream.position() + amount) > size || (mySeekableFileInputStream.position() + amount) < 0)
		{
			return false;
		}
		mySeekableFileInputStream.skip(amount);
		return true;
	}

	/**
	 * Moves the file pointer to the next higher multiple of 'a'.
	 *
	 * @param a the value to align to, must be positive
	 * @return true iff the align was successful
	 */
	public boolean align(int a) throws IOException
	{
		// skips to the next multiple of 'a' bytes
		if(a <= 0)
		{
			return false;
		}
		if(a == 1)
		{
			return true;
		}
		long position = mySeekableFileInputStream.position();
		long temp = position + (a - (position % a)) % a;
		if(temp > size)
		{
			return false;
		}
		mySeekableFileInputStream.seek(temp);
		return true;
	}

	/**
	 * Reads from the current file pointer into the given array.
	 *
	 * @param bytes the array to read into. this will attempt to read bytes.length bytes from the file
	 */
	public void read(byte[] bytes) throws IOException
	{
		if(bytes == null)
		{
			return;
		}
		if((mySeekableFileInputStream.position() + bytes.length) > size)
		{
			throw new IOException("BufferedMSILInputStream.read: Premature EOF");
		}

		mySeekableFileInputStream.read(bytes);
	}

	/**
	 * Reads an unsigned byte from the file, returned in the lower 8 bits of an int.
	 * Advances the file pointer by 1.
	 */
	public int readBYTE() throws IOException
	{
		return mySeekableFileInputStream.read() & 0xFF;
	}

	/**
	 * Reads an unsigned 2-byte integer from the file, returned in the lower 2 bytes of an int
	 * Advances the file pointer by 2.
	 */
	public int readWORD() throws IOException
	{
		if(mySeekableFileInputStream.position() + 1 >= size)
		{
			throw new IOException("MSILInputStream.readWORD: Premature EOF");
		}

		int b1 = readBYTE();
		int b2 = readBYTE();
		return (int) ((b1 & 0xFF) | ((b2 & 0xFF) << 8));
	}

	/**
	 * Reads an unsigned 4-byte integer from the file and returns it the lower 4 bytes of a long.
	 * Advances the file pointer by 4.
	 */
	public long readDWORD() throws IOException
	{
		if(mySeekableFileInputStream.position() + 3 >= size)
		{
			throw new IOException("MSILInputStream.readDWORD: Premature EOF");
		}
		byte[] bytes = new byte[4];
		mySeekableFileInputStream.read(bytes);

		return (long) ((bytes[0] & 0xFFL) | ((bytes[1] & 0xFFL) << 8) | ((bytes[2] & 0xFFL) << 16) | ((bytes[3] & 0xFFL) << 24));
	}

	/**
	 * Reads an 8-byte (signed) quantity and returns it in a long.
	 * Advances the file pointer by 8.
	 */
	public long readDDWORD() throws IOException
	{
		// throws java.io.IOException if EOF
		if(mySeekableFileInputStream.position() + 7 >= size)
		{
			throw new IOException("MSILInputStream.readDWORD: Premature EOF");
		}
		byte[] bytes = new byte[8];
		mySeekableFileInputStream.read(bytes);
		return (long) ((bytes[0] & 0xFFl) | ((bytes[1] & 0xFFl) << 8) | ((bytes[2] & 0xFFl) << 16) | ((bytes[3] & 0xFFl) << 24) | ((bytes[4] &
				0xFFl) << 32) | ((bytes[5] & 0xFFl) << 40) | ((bytes[6] & 0xFFl) << 48) | ((bytes[7] & 0xFFl) << 56));
	}
	/////////////////////////////

	/**
	 * Zeroes out the data starting at the current file pointer and extending 'length' bytes.
	 * Advances the file pointer by 'length', if successful.
	 *
	 * @param length the number of bytes to zero out, must be positive
	 */
	public void zero(long length) throws IOException
	{
		if((mySeekableFileInputStream.position() + length) >= size || length < 0)
		{
			throw new IOException("MSILInputStream.zero: Invalid length parameter");
		}
		/*for(int i = 0; i < length; i++)
		{
			data[current] = 0;
			current++;
		}             */
		mySeekableFileInputStream.seek(mySeekableFileInputStream.position() + length);
	}

	@Override
	public void close() throws IOException
	{
		mySeekableFileInputStream.close();
	}
}
