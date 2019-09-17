package consulo.internal.dotnet.asm.metadata.genericTable;

import consulo.internal.dotnet.asm.metadata.genericTable.entryReader.*;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * @author VISTALL
 * @since 09.05.14
 */
public class GenericTableDefinition
{
	@Nonnull
	public static GenericTableDefinition parse(@Nonnull String grammar)
	{
		StringTokenizer outer = new StringTokenizer(grammar, ":");
		String name = outer.nextToken();
		String theRest = outer.nextToken();
		outer = new StringTokenizer(theRest, ",");

		String[] fieldNames = new String[outer.countTokens()];
		String[] types = new String[fieldNames.length];
		Map<String, Integer> nameToIndex = new HashMap<>();
		for(int i = 0; i < fieldNames.length; i++)
		{
			String field = outer.nextToken();
			StringTokenizer fieldtok = new StringTokenizer(field, "=");
			fieldNames[i] = fieldtok.nextToken();
			types[i] = fieldtok.nextToken();
			nameToIndex.put(fieldNames[i], i);
		}

		StringTokenizer tok;

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

			fieldInfos[i] = new GenericTableFieldInfo(i, fieldName, entryReader);
		}
		return new GenericTableDefinition(name, nameToIndex, fieldInfos);
	}

	private final String myName;
	private final Map<String, Integer> myNameToIndex;
	private final GenericTableFieldInfo[] myFieldInfos;

	public GenericTableDefinition(String name, @Nonnull Map<String, Integer> nameToIndex, GenericTableFieldInfo[] fieldInfos)
	{
		myName = name;
		myNameToIndex = nameToIndex;
		myFieldInfos = fieldInfos;
	}

	@Nonnull
	public Map<String, Integer> getNameToIndex()
	{
		return myNameToIndex;
	}

	@Nonnull
	public String getName()
	{
		return myName;
	}

	@Nonnull
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
