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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import consulo.annotations.Immutable;
import org.jetbrains.annotations.NotNull;
import consulo.internal.dotnet.asm.io.ByteBuffer;
import consulo.internal.dotnet.asm.mbel.TypeGroup;

/**
 * This class represents a type signature for an SZARRAY (Single-dimensional, Zero-based array)
 *
 * @author Michael Stepp
 */
public class SZArrayTypeSignature extends TypeSpecSignature
{
	private List<CustomModifierSignature> customMods = Collections.emptyList();
	private TypeSignature elementTypeSignature;

	/**
	 * Makes an SZArrayTypeSignature with the given element type
	 *
	 * @param type the TypeSignature of the elements of this array
	 */
	public SZArrayTypeSignature(TypeSignature type) throws SignatureException
	{
		this(CustomModifierSignature.EMPTY_ARRAY, type);
	}

	/**
	 * Constructor which allows CustomModifiers to be added to this signature
	 *
	 * @param mods an array of CustomModifiers to be applied to this signature
	 * @param type the TypeSignature of the elements of this array
	 */
	public SZArrayTypeSignature(@NotNull CustomModifierSignature[] mods, TypeSignature type) throws SignatureException
	{
		super(ELEMENT_TYPE_SZARRAY);
		if(type == null)
		{
			throw new SignatureException("SZArrayTypeSignature: null element type given");
		}
		elementTypeSignature = type;

		if(mods.length > 0)
		{
			customMods = new ArrayList<CustomModifierSignature>(mods.length);
			Collections.addAll(customMods, mods);
		}
	}

	private SZArrayTypeSignature()
	{
		super(ELEMENT_TYPE_SZARRAY);
	}

	/**
	 * Factory method for parsing SZArrayTypeSignatures from raw binary blobs
	 *
	 * @param buffer the wrapper around the binary blob
	 * @param group  a TypeGroup for reconciling tokens to mbel references
	 * @return an SZArrayTypeSignature representing this binary blob, or null if there was a parse error
	 */
	public static TypeSignature parse(ByteBuffer buffer, TypeGroup group)
	{
		SZArrayTypeSignature blob = new SZArrayTypeSignature();
		byte data = buffer.get();
		if(data != ELEMENT_TYPE_SZARRAY)
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

		blob.elementTypeSignature = TypeSignatureParser.parse(buffer, group);
		if(blob.elementTypeSignature == null)
		{
			return null;
		}

		return blob;
	}

	/**
	 * Getter method for the CustomModifiers applied to this signature
	 */
	@NotNull
	@Immutable
	public List<CustomModifierSignature> getCustomMods()
	{
		return customMods;
	}

	/**
	 * Getter method for the single-byte code type value
	 */
	public TypeSignature getElementType()
	{
		return elementTypeSignature;
	}
}
