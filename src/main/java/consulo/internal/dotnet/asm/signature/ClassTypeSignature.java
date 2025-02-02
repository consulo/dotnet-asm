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


package consulo.internal.dotnet.asm.signature;

import consulo.internal.dotnet.asm.io.ByteBuffer;
import consulo.internal.dotnet.asm.mbel.AbstractTypeReference;
import consulo.internal.dotnet.asm.mbel.TypeGroup;
import consulo.internal.dotnet.asm.metadata.TableConstants;

import jakarta.annotation.Nonnull;

/**
 * This class describes a class type signature
 *
 * @author Michael Stepp
 */
public class ClassTypeSignature extends TypeSignature
{
	private AbstractTypeReference classType;

	/**
	 * Makes a class signature representing the given type
	 *
	 * @param clazz an mbel reference to the type this signature describes
	 */
	public ClassTypeSignature(@Nonnull AbstractTypeReference clazz)
	{
		this();
		classType = clazz;
	}

	private ClassTypeSignature()
	{
		super(ELEMENT_TYPE_CLASS);
	}

	/**
	 * Factory method for parsing a class signature from a raw binary blob
	 *
	 * @param buffer the buffer to read from
	 * @param group  a TypeGroup for reconciling tokens to mbel references
	 * @return a ClassTypeSignature representing the given blob, or null if there was a parse error
	 */
	public static TypeSignature parse(ByteBuffer buffer, TypeGroup group)
	{
		byte data = buffer.get();
		if(data != ELEMENT_TYPE_CLASS)
		{
			return null;
		}

		ClassTypeSignature blob = new ClassTypeSignature();
		int token[] = parseTypeDefOrRefEncoded(buffer);
		if(token[0] == TableConstants.TypeDef)
		{
			blob.classType = group.getTypeDefs()[token[1] - 1];
		}
		else if(token[0] == TableConstants.TypeRef)
		{
			blob.classType = group.getTypeRefs()[token[1] - 1];
		}
		else if(token[0] == TableConstants.TypeSpec)
		{
			blob.classType = group.getTypeSpecs()[token[1] - 1];
		}
		else
		{
			return null;
		}
		return blob;
	}

	/**
	 * Returns a reference to the type this type signature describes
	 */
	public AbstractTypeReference getClassType()
	{
		return classType;
	}
}
