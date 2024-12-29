package consulo.internal.dotnet.asm.metadata.genericTable.entryReader;

import java.io.IOException;

import jakarta.annotation.Nullable;
import consulo.internal.dotnet.asm.io.MSILInputStream;
import consulo.internal.dotnet.asm.metadata.TableConstants;

/**
 * @author VISTALL
 * @since 09.05.14
 */
public class BlobHeapEntryReader implements EntryReader
{
	@Nullable
	@Override
	public Object read(MSILInputStream in, TableConstants tc) throws IOException
	{
		long index = tc.readHeapIndex(in, TableConstants.BlobHeap);
		return tc.getBlob(index);
	}
}
