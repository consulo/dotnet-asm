package consulo.internal.dotnet.asm.metadata.genericTable.entryReader;

import java.io.IOException;

import org.jetbrains.annotations.Nullable;
import consulo.internal.dotnet.asm.io.MSILInputStream;
import consulo.internal.dotnet.asm.metadata.TableConstants;

/**
 * @author VISTALL
 * @since 09.05.14
 */
public class TableValueEntryReader implements EntryReader
{
	private int myTableIndex;

	public TableValueEntryReader(int tableIndex)
	{
		myTableIndex = tableIndex;
	}

	@Nullable
	@Override
	public Object read(MSILInputStream in, TableConstants tc) throws IOException
	{
		return tc.readTableIndex(in, myTableIndex);
	}
}
