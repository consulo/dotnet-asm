/*
 * Copyright 2013-2026 consulo.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package consulo.internal.dotnet.asm.io;

import java.io.IOException;
import java.nio.channels.SeekableByteChannel;

/**
 * A buffered stream over a channel - of a file of any file system, a remote one too.
 *
 * @author VISTALL
 * @since 2026-11-13
 */
public class SeekableChannelInputStream extends SeekableInputStream
{
	private static final int BUFFER_SIZE = 64 * 1024;

	private final SeekableByteChannel myChannel;
	private final long myLength;
	private final java.nio.ByteBuffer myBuffer = java.nio.ByteBuffer.allocate(BUFFER_SIZE);
	// the position of the first byte of the buffer in the channel
	private long myBufferStart;

	public SeekableChannelInputStream(SeekableByteChannel channel) throws IOException
	{
		myChannel = channel;
		myLength = channel.size();
		myBuffer.limit(0);
	}

	private boolean fill() throws IOException
	{
		myBufferStart += myBuffer.limit();
		myBuffer.clear();
		myChannel.position(myBufferStart);
		while(myBuffer.hasRemaining())
		{
			if(myChannel.read(myBuffer) < 0)
			{
				break;
			}
		}
		myBuffer.flip();
		return myBuffer.hasRemaining();
	}

	@Override
	public int read() throws IOException
	{
		if(!myBuffer.hasRemaining() && !fill())
		{
			return checkRead(-1);
		}
		return myBuffer.get() & 0xFF;
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException
	{
		if(len == 0)
		{
			return 0;
		}
		int read = 0;
		while(read < len)
		{
			if(!myBuffer.hasRemaining() && !fill())
			{
				break;
			}
			int count = Math.min(len - read, myBuffer.remaining());
			myBuffer.get(b, off + read, count);
			read += count;
		}
		return read == 0 ? checkRead(-1) : read;
	}

	@Override
	public long seek(long position) throws IOException
	{
		checkIdx(position);
		if(position >= myBufferStart && position <= myBufferStart + myBuffer.limit())
		{
			myBuffer.position((int) (position - myBufferStart));
		}
		else
		{
			myBufferStart = position;
			myBuffer.limit(0);
		}
		return position;
	}

	@Override
	public long position()
	{
		return myBufferStart + myBuffer.position();
	}

	@Override
	public long length()
	{
		return myLength;
	}

	@Override
	public void close() throws IOException
	{
		myChannel.close();
	}
}
