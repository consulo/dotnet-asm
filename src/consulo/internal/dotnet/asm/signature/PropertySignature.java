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
 * This class describes the signature of a Property
 *
 * @author Michael Stepp
 */
public class PropertySignature extends Signature implements CallingConvention
{
	private byte flags;
	private TypeSignature type;
	private List<ParameterSignature> params = Collections.emptyList();

	private PropertySignature()
	{
	}

	/**
	 * Convenience constructor for a non-static property (i.e. HASTHIS and not EXPLICITTHIS are set).
	 *
	 * @param Type   the type of this property
	 * @param Params the parameters for this property
	 */
	public PropertySignature(TypeSignature Type, @NotNull ParameterSignature[] Params) throws SignatureException
	{
		this(true, false, Type, Params);
	}

	/**
	 * Constructs a property with the given flags, type, and parameters.
	 *
	 * @param hasthis      true iff this method has a 'this' pointer (i.e. is non-static)
	 * @param explicitthis true iff this method has an explicit 'this' pointer
	 * @param Type         the type of this property
	 * @param params       the parameters of this property
	 */
	public PropertySignature(boolean hasthis, boolean explicitthis, TypeSignature Type, @NotNull ParameterSignature[] params) throws
			SignatureException
	{
		flags = (byte) (PROPERTY | ((hasthis || explicitthis) ? HASTHIS : 0) | (explicitthis ? EXPLICITTHIS : 0));
		type = Type;
		if(type == null)
		{
			throw new SignatureException("PropertySignature: null type given");
		}
		if(params.length > 0)
		{
			this.params = new ArrayList<ParameterSignature>(params.length);
			Collections.addAll(this.params, params);
		}
	}


	/**
	 * Factory method for parsing a PropertySignature from a blob
	 *
	 * @param buffer the bufer to read from
	 * @param group  a TypeGroup for reconciling tokens to mbel references
	 * @return a PropertySignature representing the given blob, or null if there was a parse error
	 */
	public static PropertySignature parse(ByteBuffer buffer, TypeGroup group)
	{
		PropertySignature blob = new PropertySignature();

		byte data = buffer.get();
		if((data & PROPERTY) == 0)
		{
			return null;
		}
		blob.flags = data;

		int paramCount = readCodedInteger(buffer);

		blob.type = TypeSignatureParser.parse(buffer, group);
		if(blob.type == null)
		{
			return null;
		}

		blob.params = paramCount == 0 ? Collections.<ParameterSignature>emptyList() : new ArrayList<ParameterSignature>(paramCount);
		ParameterSignature temp = null;
		for(int i = 0; i < paramCount; i++)
		{
			temp = ParameterSignature.parse(buffer, group);
			if(temp == null)
			{
				return null;
			}
			blob.params.add(temp);
		}
		return blob;
	}

	/**
	 * Getter method the the TypeSignature associated with this property
	 */
	public TypeSignature getType()
	{
		return type;
	}

	@NotNull
	@Immutable
	public List<ParameterSignature> getParameters()
	{
		return params;
	}

	public byte getFlags()
	{
		return flags;
	}
}
