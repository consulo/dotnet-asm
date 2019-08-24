package consulo.internal.dotnet.asm.metadata.genericTable;

import javax.annotation.Nonnull;
import consulo.internal.dotnet.asm.metadata.genericTable.entryReader.EntryReader;

/**
 * @author VISTALL
 * @since 09.05.14
 */
public class GenericTableFieldInfo
{
	private final String myName;
	private final EntryReader myEntryReader;

	public GenericTableFieldInfo(@Nonnull String name, @Nonnull EntryReader entryReader)
	{
		myName = name;
		myEntryReader = entryReader;
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
