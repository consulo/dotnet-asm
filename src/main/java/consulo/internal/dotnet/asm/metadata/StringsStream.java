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

package consulo.internal.dotnet.asm.metadata;

import consulo.internal.dotnet.asm.io.MSILInputStream;
import consulo.internal.dotnet.asm.util.StringUtil;

import java.io.IOException;

/**
 * This class represents a stores the #Strings metadata heap.
 * Strings in this heap are ASCII, 8-bit, null-terminated strings.
 * The first string in this heap must always be null, so the first byte
 * in the #Strings heap is always 0.
 *
 * @author Michael Stepp
 */
public class StringsStream
{
	private byte[] raw_bytes;

	/**
	 * Parses a StringsStream from the given input stream, with the given size.
	 *
	 * @param in   the input stream to read from
	 * @param size the size in bytes of the stream
	 */
	public StringsStream(MSILInputStream in, long size) throws IOException
	{
		raw_bytes = new byte[(int) size];
		in.read(raw_bytes);
	}

	/**
	 * Returns a string from the #Strings heap, index by byte offset.
	 * These offset values appear in metadata tables.
	 *
	 * @param offset the byte offset at which the string starts (0-based)
	 * @return the string in the #Strings heap at the given offset
	 */
	public String getStringByOffset(long offset)
	{
		if(offset < 0 || offset >= raw_bytes.length)
		{
			return StringUtil.EMPTY;
		}

		StringBuilder builder = new StringBuilder();
		for(int i = (int) offset; ((int) raw_bytes[i]) != 0; i++)
		{
			builder.append((char) (raw_bytes[i] & 0xFF));
		}
		return builder.toString();
	}

	/**
	 * Returns the size in bytes of this StringsStream
	 */
	public int getSize()
	{
		return raw_bytes.length;
	}
}
