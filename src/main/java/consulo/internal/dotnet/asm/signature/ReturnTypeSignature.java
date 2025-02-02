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
import consulo.internal.dotnet.asm.mbel.TypeGroup;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class describes the return type of a method in a method signature
 *
 * @author Michael Stepp
 */
public class ReturnTypeSignature extends Signature implements InnerTypeOwner
{
	private ParameterInfo paramInfo;
	// Signature fields
	private List<CustomModifierSignature> customMods = Collections.emptyList();
	private TypeSignature type;
	private byte elementType;
	// elementType:    meaning:
	// TYPEONLY        just Type without BYREF
	// BYREF           BYREF and Type
	// TYPEDBYREF      TYPEDBYREF
	// VOID            VOID

	private ReturnTypeSignature()
	{
	}

	/**
	 * Makes a new ReturnTypeSignature object with the given type, possibly by reference
	 *
	 * @param typeSig the type of this return value
	 * @param byref   true iff this value is returned by reference
	 */
	public ReturnTypeSignature(@Nonnull TypeSignature typeSig, boolean byref) throws SignatureException
	{
		type = typeSig;
		elementType = (byref ? ELEMENT_TYPE_BYREF : ELEMENT_TYPE_TYPEONLY);
	}

	/**
	 * Makes a new ReturnTypeSignature object with the given type code
	 *
	 * @param typecode must be one of ELEMENT_TYPE_TYPEDBYREF or ELEMENT_TYPE_VOID
	 */
	public ReturnTypeSignature(byte typecode) throws SignatureException
	{
		if(typecode == ELEMENT_TYPE_TYPEDBYREF)
		{
			elementType = typecode;
		}
		else if(typecode == ELEMENT_TYPE_VOID)
		{
			elementType = typecode;
		}
		else
		{
			throw new SignatureException("ReturnTypeSignature: Invalid byte code given");
		}
	}


	/**
	 * Factory method to parse a ReturnTypeSignature from a ByteBuffer blob
	 *
	 * @param buffer the buffer to read from
	 * @param group  a TypeGroup for reconciling tokens to mbel references
	 * @return a ReturnTypeSignature representing the given blob, or null if there was a parse error
	 */
	public static ReturnTypeSignature parse(ByteBuffer buffer, TypeGroup group)
	{
		ReturnTypeSignature blob = new ReturnTypeSignature();

		int pos = buffer.getPosition();
		CustomModifierSignature temp = CustomModifierSignature.parse(buffer, group);
		while(temp != null)
		{
			if(blob.customMods.isEmpty())
			{
				blob.customMods = new ArrayList<CustomModifierSignature>(5);
			}
			blob.customMods.add(temp);
			pos = buffer.getPosition();
			temp = CustomModifierSignature.parse(buffer, group);
		}
		buffer.setPosition(pos);

		blob.elementType = ELEMENT_TYPE_TYPEONLY;
		byte data = buffer.peek();
		if(data == ELEMENT_TYPE_TYPEDBYREF)
		{
			// TYPEDBYREF
			blob.elementType = data;
			buffer.get();
		}
		else if(data == ELEMENT_TYPE_VOID)
		{
			// VOID
			blob.elementType = data;
			buffer.get();
		}
		else
		{
			if(data == ELEMENT_TYPE_BYREF)
			{
				// BYREF Type
				blob.elementType = data;
				buffer.get();
			}
			blob.type = TypeSignatureParser.parse(buffer, group);
			if(blob.type == null)
			{
				return null;
			}
		}

		return blob;
	}

	/**
	 * Returns the ParamInfo object of this return type (may be null)
	 */
	public ParameterInfo getParameterInfo()
	{
		return paramInfo;
	}

	/**
	 * Sets the ParamInfo object for this return type (may be null)
	 */
	public void setParameterInfo(ParameterInfo info)
	{
		paramInfo = info;
	}

	/**
	 * Returns the type of returntype this is:
	 * Type:                    Meaning:
	 * ELEMENT_TYPE_TYPEONLY    Return value is given by the TypeSignature
	 * ELEMENT_TYPE_BYREF       Return value is given by the TypeSignature, passed by reference
	 * ELEMENT_TYPE_TYPEDBYREF  Return value is typed by reference
	 * ELEMENT_TYPE_VOID        Void return type
	 */
	public int getElementType()
	{
		return elementType;
	}

	/**
	 * Returns the type signature for this return type (may be null)
	 */
	@Nullable
	@Override
	public TypeSignature getInnerType()
	{
		return type;
	}

	/**
	 * Getter method for the CustomModifiers applied to this ReturnTypeSignature
	 */
	public CustomModifierSignature[] getCustomModifiers()
	{
		CustomModifierSignature[] sigs = new CustomModifierSignature[customMods.size()];
		for(int i = 0; i < sigs.length; i++)
		{
			sigs[i] = (CustomModifierSignature) customMods.get(i);
		}

		return sigs;
	}
}
