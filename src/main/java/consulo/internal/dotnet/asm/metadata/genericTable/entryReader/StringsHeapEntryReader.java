package consulo.internal.dotnet.asm.metadata.genericTable.entryReader;

import java.io.IOException;

import consulo.internal.dotnet.asm.io.MSILInputStream;
import consulo.internal.dotnet.asm.metadata.TableConstants;

import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 09.05.14
 */
public class StringsHeapEntryReader implements EntryReader
{
	@Nullable
	@Override
	public Object read(MSILInputStream in, TableConstants tc) throws IOException
	{
		long index = tc.readHeapIndex(in, TableConstants.StringsHeap);
		return tc.getString(index);
	}
}
