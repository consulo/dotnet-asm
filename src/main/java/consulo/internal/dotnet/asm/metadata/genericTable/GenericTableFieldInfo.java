package consulo.internal.dotnet.asm.metadata.genericTable;

import consulo.internal.dotnet.asm.metadata.genericTable.entryReader.EntryReader;

import jakarta.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 09.05.14
 */
public class GenericTableFieldInfo
{
	private final int myIndex;
	private final String myName;
	private final EntryReader myEntryReader;

	public GenericTableFieldInfo(int index, @Nonnull String name, @Nonnull EntryReader entryReader)
	{
		myIndex = index;
		myName = name;
		myEntryReader = entryReader;
	}

	public int getIndex()
	{
		return myIndex;
	}

	@Nonnull
	public String getName()
	{
		return myName;
	}

	@Nonnull
	public EntryReader getEntryReader()
	{
		return myEntryReader;
	}
}
