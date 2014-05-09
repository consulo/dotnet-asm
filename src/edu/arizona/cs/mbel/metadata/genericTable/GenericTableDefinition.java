package edu.arizona.cs.mbel.metadata.genericTable;

import java.util.StringTokenizer;

import org.jetbrains.annotations.NotNull;
import edu.arizona.cs.mbel.metadata.genericTable.entryReader.BlobHeapEntryReader;
import edu.arizona.cs.mbel.metadata.genericTable.entryReader.ByteEntryReader;
import edu.arizona.cs.mbel.metadata.genericTable.entryReader.CodeValueEntryReader;
import edu.arizona.cs.mbel.metadata.genericTable.entryReader.DWordEntryReader;
import edu.arizona.cs.mbel.metadata.genericTable.entryReader.EntryReader;
import edu.arizona.cs.mbel.metadata.genericTable.entryReader.GUIDHeapEntryReader;
import edu.arizona.cs.mbel.metadata.genericTable.entryReader.StringsHeapEntryReader;
import edu.arizona.cs.mbel.metadata.genericTable.entryReader.TableValueEntryReader;
import edu.arizona.cs.mbel.metadata.genericTable.entryReader.WordEntryReader;

/**
 * @author VISTALL
 * @since 09.05.14
 */
public class GenericTableDefinition
{
	@NotNull
	public static GenericTableDefinition parse(@NotNull String grammar)
	{
		StringTokenizer outer = new StringTokenizer(grammar, ":");
		String name = outer.nextToken();
		String theRest = outer.nextToken();
		outer = new StringTokenizer(theRest, ",");

		String[] fieldNames = new String[outer.countTokens()];
		String[] types = new String[fieldNames.length];
		for(int i = 0; i < fieldNames.length; i++)
		{
			String field = outer.nextToken();
			StringTokenizer fieldtok = new StringTokenizer(field, "=");
			fieldNames[i] = fieldtok.nextToken();
			types[i] = fieldtok.nextToken();
		}

		StringTokenizer tok = null;

		GenericTableFieldInfo[] fieldInfos = new GenericTableFieldInfo[fieldNames.length];
		for(int i = 0; i < fieldNames.length; i++)
		{
			EntryReader entryReader = null;

			String type = types[i];
			String fieldName = fieldNames[i];
			if(type.startsWith("1"))
			{
				entryReader = new ByteEntryReader();
			}
			else if(type.startsWith("2"))
			{
				entryReader = new WordEntryReader();
			}
			else if(type.startsWith("4"))
			{
				entryReader = new DWordEntryReader();
			}
			else if(type.startsWith("S"))
			{
				entryReader = new StringsHeapEntryReader();
			}
			else if(type.startsWith("B"))
			{
				entryReader = new BlobHeapEntryReader();
			}
			else if(type.startsWith("G"))
			{
				entryReader = new GUIDHeapEntryReader();
			}
			else if(type.startsWith("T"))
			{
				tok = new StringTokenizer(type, "|");
				tok.nextToken();
				int table = Integer.parseInt(tok.nextToken());
				entryReader = new TableValueEntryReader(table);
			}
			else if(type.startsWith("C"))
			{
				tok = new StringTokenizer(type, "|");
				tok.nextToken();
				int coded = Integer.parseInt(tok.nextToken());
				entryReader = new CodeValueEntryReader(coded);
			}
			else
			{
				throw new IllegalArgumentException("Unsupported type: " + type);
			}

			fieldInfos[i] = new GenericTableFieldInfo(fieldName, entryReader);
		}
		return new GenericTableDefinition(name, fieldInfos);
	}

	private final String myName;
	private final GenericTableFieldInfo[] myFieldInfos;

	public GenericTableDefinition(String name, GenericTableFieldInfo[] fieldInfos)
	{
		myName = name;
		myFieldInfos = fieldInfos;
	}

	@NotNull
	public String getName()
	{
		return myName;
	}

	@NotNull
	public GenericTableFieldInfo[] getFields()
	{
		return myFieldInfos;
	}

	@Override
	public boolean equals(Object obj)
	{
		return obj instanceof GenericTableDefinition && ((GenericTableDefinition) obj).getName().equals(getName());
	}
}
