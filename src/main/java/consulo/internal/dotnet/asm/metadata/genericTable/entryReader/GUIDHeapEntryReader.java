package consulo.internal.dotnet.asm.metadata.genericTable.entryReader;

import java.io.IOException;

import org.jetbrains.annotations.Nullable;
import consulo.internal.dotnet.asm.io.MSILInputStream;
import consulo.internal.dotnet.asm.metadata.TableConstants;

/**
 * @author VISTALL
 * @since 09.05.14
 */
public class GUIDHeapEntryReader implements EntryReader
{
	@Nullable
	@Override
	public Object read(MSILInputStream in, TableConstants tc) throws IOException
	{
		long index = tc.readHeapIndex(in, TableConstants.GUIDHeap);
		return tc.getGUID(index);
	}
}
