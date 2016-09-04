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
 * This class models a single local var, whereas LocalVarList
 * contains a list of all the LocalVars for a whole method
 *
 * @author Michael Stepp
 */
public class LocalVar extends Signature
{
	private List<Constraint> constraints = Collections.emptyList();
	private boolean byref;
	private TypeSignature type;

	/**
	 * Makes a LocalVar of the given type, possibly by reference
	 *
	 * @param BYREF true if the local var is passed by reference
	 * @param t     the type of the parameter
	 */
	public LocalVar(boolean BYREF, @NotNull TypeSignature t) throws SignatureException
	{
		byref = BYREF;
		type = t;
	}

	/**
	 * Makes a LocalVar with the given type and constraints, possibly by reference
	 *
	 * @param con   an array of constraints
	 * @param BYREF true if this local var is passed by reference
	 * @param t     the type of this local var
	 */
	public LocalVar(@NotNull Constraint con[], boolean BYREF, @NotNull TypeSignature t) throws SignatureException
	{
		byref = BYREF;
		type = t;
		if(con.length > 0)
		{
			constraints = new ArrayList<Constraint>(con.length);
			Collections.addAll(constraints, con);
		}
	}

	private LocalVar()
	{
	}

	/**
	 * Factory method for parsing a LocalVar from a raw binary blob
	 *
	 * @param buffer the buffer to read from
	 * @param group  a TypeGroup for reconciling tokens to mbel references
	 * @return a LocalVar representing the given blob, or null if there was a parse error
	 */
	public static LocalVar parse(ByteBuffer buffer, TypeGroup group)
	{
		LocalVar blob = new LocalVar();

		int pos = buffer.getPosition();
		Constraint temp = Constraint.parse(buffer);
		while(temp != null)
		{
			if(blob.constraints.isEmpty())
			{
				blob.constraints = new ArrayList<Constraint>(5);
			}
			blob.constraints.add(temp);
			pos = buffer.getPosition();
			temp = Constraint.parse(buffer);
		}
		buffer.setPosition(pos);

		byte data = buffer.peek();
		if(data == ELEMENT_TYPE_BYREF)
		{
			blob.byref = true;
			buffer.get();
		}
		blob.type = TypeSignatureParser.parse(buffer, group);
		if(blob.type == null)
		{
			return null;
		}
		return blob;
	}

	/**
	 * Getter method for the Constraints applied to this local var
	 */
	@NotNull
	@Immutable
	public List<Constraint> getConstraints()
	{
		return constraints;
	}

	/**
	 * Returns whether or not this local var is used by reference
	 */
	public boolean isByRef()
	{
		return byref;
	}

	/**
	 * Returns the type of this local var
	 */
	public TypeSignature getType()
	{
		return type;
	}
}
