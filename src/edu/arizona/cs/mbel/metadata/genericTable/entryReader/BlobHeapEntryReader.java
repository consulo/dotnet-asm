package edu.arizona.cs.mbel.metadata.genericTable.entryReader;

import java.io.IOException;

import org.jetbrains.annotations.Nullable;
import edu.arizona.cs.mbel.MSILInputStream;
import edu.arizona.cs.mbel.metadata.TableConstants;

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
