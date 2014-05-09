package edu.arizona.cs.mbel.metadata.genericTable;

import org.jetbrains.annotations.NotNull;
import edu.arizona.cs.mbel.metadata.genericTable.entryReader.EntryReader;

/**
 * @author VISTALL
 * @since 09.05.14
 */
public class GenericTableFieldInfo
{
	private final String myName;
	private final EntryReader myEntryReader;

	public GenericTableFieldInfo(@NotNull String name, @NotNull EntryReader entryReader)
	{
		myName = name;
		myEntryReader = entryReader;
	}

	@NotNull
	public String getName()
	{
		return myName;
	}

	@NotNull
	public EntryReader getEntryReader()
	{
		return myEntryReader;
	}
}
