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

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class describes a pointer type
 *
 * @author Michael Stepp
 */
public class PointerTypeSignature extends TypeSpecSignature
{
	private List<CustomModifierSignature> customMods = Collections.emptyList();
	private TypeSignature type;            // if null, then VOID

	/**
	 * Constructor that specifies the CustomModifiers applied to this signature
	 *
	 * @param mods an array of CustomModifiers to be applied to this signature (can be null)
	 * @param sig  the type of pointer this is
	 */
	public PointerTypeSignature(@Nonnull CustomModifierSignature[] mods, TypeSignature sig) throws SignatureException
	{
		// for typed ptr
		super(ELEMENT_TYPE_PTR);
		if(sig == null)
		{
			throw new SignatureException("PointerTypeSignature: null type specified");
		}
		type = sig;

		if(mods.length > 0)
		{
			customMods = new ArrayList<CustomModifierSignature>(mods.length);
			Collections.addAll(customMods, mods);
		}
	}

	/**
	 * Constructor for a VOID pointer, with CustomModifiers
	 *
	 * @param mods the CustomModifiers for this pointer (can be null)
	 */
	public PointerTypeSignature(@Nonnull CustomModifierSignature[] mods) throws SignatureException
	{
		// for VOID ptr
		super(ELEMENT_TYPE_PTR);
		type = null;

		if(mods.length > 0)
		{
			customMods = new ArrayList<CustomModifierSignature>(mods.length);
			Collections.addAll(customMods, mods);
		}
	}

	private PointerTypeSignature()
	{
		super(ELEMENT_TYPE_PTR);
	}

	/**
	 * Factory method for parsing a PointerTypeSignature from a binary blob
	 *
	 * @param buffer the buffer to read from
	 * @param group  a TypeGroup to reconcile tokens to mbel references
	 * @return a PointerTypeSignature representing the given binary blob, or null if there was a parse error
	 */
	public static TypeSignature parse(ByteBuffer buffer, TypeGroup group)
	{
		PointerTypeSignature blob = new PointerTypeSignature();
		byte data = buffer.get();
		if(data != ELEMENT_TYPE_PTR)
		{
			return null;
		}

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

		data = buffer.peek();
		if(data != ELEMENT_TYPE_VOID)
		{
			blob.type = TypeSignatureParser.parse(buffer, group);
			if(blob.type == null)
			{
				return null;
			}
		}
		else
		{
			blob.type = null;
			buffer.get();
		}

		return blob;
	}

	/**
	 * Status method telling whether this is a VOID pointer or not
	 * (isVoid() implies getPointerType()==null)
	 *
	 * @return true if this is a VOID pointer, false otherwise
	 */
	public boolean isVoid()
	{
		return (type == null);
	}

	/**
	 * Getter method for the type of pointer this is (or null if VOID)
	 */
	public TypeSignature getPointerType()
	{
		return type;
	}

	/**
	 * Getter method for the CustomModifiers applied to this signature.
	 * This method will never return null, but it may return a 0-length array.
	 */
	public CustomModifierSignature[] getCustomMods()
	{
		CustomModifierSignature[] sigs = new CustomModifierSignature[customMods.size()];
		for(int i = 0; i < sigs.length; i++)
		{
			sigs[i] = (CustomModifierSignature) customMods.get(i);
		}
		return sigs;
	}
}
