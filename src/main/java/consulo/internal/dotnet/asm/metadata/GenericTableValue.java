/* MBEL: The Microsoft Bytecode Engineering Library
 * Copyright (C) 2003 The University of Arizona
 * http://www.cs.arizona.edu/mbel/license.html
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package consulo.internal.dotnet.asm.metadata;

import consulo.internal.dotnet.asm.io.MSILInputStream;
import consulo.internal.dotnet.asm.metadata.genericTable.GenericTableDefinition;
import consulo.internal.dotnet.asm.metadata.genericTable.GenericTableFieldInfo;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;

/**
 * This class is used as a generic construct to hold the data from any arbitrary metadata table.
 * It does this by means of a parsing grammar and a hashtable. The grammar is one of the constant
 * strings defined in the TableConstants.GRAMMAR array.
 *
 * @author Michael Stepp
 */
public class GenericTableValue
{
	private final GenericTableDefinition myTable;
	private final Object[] myValues;

	/**
	 * Initializes this table with the given grammar.
	 *
	 * @param definition the grammar string for this table (one of the constants defined in TableConstants.GRAMMAR)
	 */
	public GenericTableValue(@Nonnull GenericTableDefinition definition)
	{
		myTable = definition;
		myValues = new Object[myTable.getFields().length];
	}

	/**
	 * Parses a metadata table based on its grammar and the parsing methods defined in TableConstants.
	 * The parsed values all go into an internal hash table, which is keyed according to the names given in the ECMA spec.
	 *
	 * @param in the input stream to read from
	 * @param tc a TableConstants instance to read in heap indexes, table indexes, coded indexes, etc.
	 */
	public void parse(MSILInputStream in, TableConstants tc) throws IOException
	{
		GenericTableFieldInfo[] fields = myTable.getFields();

		for(GenericTableFieldInfo fieldInfo : fields)
		{
			myValues[fieldInfo.getIndex()] = fieldInfo.getEntryReader().read(in, tc);
		}
	}

	@Nonnull
	public GenericTableDefinition getTable()
	{
		return myTable;
	}

	/**
	 * Returns a string field with the given name
	 *
	 * @param fieldName the name of the field (i.e. "Name" or "Namespace")
	 * @return the string field, or null if invalid
	 */
	@Nullable
	public String getString(@Nonnull String fieldName)
	{
		if(myValues == null)
		{
			return null;
		}

		Integer nameIndex = myTable.getNameToIndex().get(fieldName);
		if(nameIndex == null)
		{
			return null;
		}

		Object obj = myValues[nameIndex];
		if(obj == null || !(obj instanceof String))
		{
			return null;
		}
		else
		{
			return (String) obj;
		}
	}

	/**
	 * Returns a blob field of this table with the given name
	 *
	 * @param fieldName the name of the field (i.e. "Signature")
	 * @return a byte array blob
	 */
	@Nullable
	public byte[] getBlob(@Nonnull String fieldName)
	{
		if(myValues == null)
		{
			return null;
		}

		Integer nameIndex = myTable.getNameToIndex().get(fieldName);
		if(nameIndex == null)
		{
			return null;
		}

		Object obj = myValues[nameIndex];
		if(obj == null || !(obj instanceof byte[]))
		{
			return null;
		}
		else
		{
			return (byte[]) obj;
		}
	}

	/**
	 * Returns a GUID field of this table
	 *
	 * @param fieldName the name of the field (i.e. "Mvid")
	 * @return a byte array GUID
	 */
	public byte[] getGUID(@Nonnull String fieldName)
	{
		return getBlob(fieldName);
	}

	/**
	 * Returns an integer valued field from this table
	 *
	 * @param fieldName the name of this field (i.e. "BuildNumber" or "MajorVersion")
	 * @return a Number containing the constant value (will either be an Integer or a Long)
	 */
	@Nullable
	public Number getConstant(@Nonnull String fieldName)
	{
		if(myValues == null)
		{
			return null;
		}

		Integer nameIndex = myTable.getNameToIndex().get(fieldName);
		if(nameIndex == null)
		{
			return null;
		}

		Object obj = myValues[nameIndex];
		if(obj == null || !(obj instanceof Number))
		{
			return null;
		}
		else
		{
			return (Number) obj;
		}
	}

	/**
	 * Returns a table index field from this table
	 *
	 * @param fieldName the name of the field (i.e. "MethodList" or "Parent")
	 * @return a Long containing a RID
	 */
	@Nullable
	public Long getTableIndex(@Nonnull String fieldName)
	{
		Number temp = getConstant(fieldName);
		if(temp == null || !(temp instanceof Long))
		{
			return null;
		}
		else
		{
			return (Long) temp;
		}
	}

	/**
	 * Returns a coded index field of this table
	 *
	 * @param fieldName the name of the field
	 * @return a Long containing a coded index
	 */
	@Nullable
	public Long getCodedIndex(@Nonnull String fieldName)
	{
		return getTableIndex(fieldName);
	}

	@Override
	public boolean equals(Object o)
	{
		if(o == null || !(o instanceof GenericTableValue))
		{
			return false;
		}

		GenericTableValue table = (GenericTableValue) o;
		if(!myTable.equals(table.myTable))
		{
			return false;
		}

		for(GenericTableFieldInfo fieldName : myTable.getFields())
		{
			Object obj1 = myValues[fieldName.getIndex()];
			Object obj2 = table.myValues[fieldName.getIndex()];
			if(obj1 instanceof byte[])
			{
				byte[] b1 = (byte[]) obj1;
				byte[] b2 = (byte[]) obj2;
				if(b1.length != b2.length)
				{
					return false;
				}
				for(int j = 0; j < b1.length; j++)
				{
					if(b1[j] != b2[j])
					{
						return false;
					}
				}
			}
			else if(!obj1.equals(obj2))
			{
				return false;
			}
		}
		return true;
	}
}
