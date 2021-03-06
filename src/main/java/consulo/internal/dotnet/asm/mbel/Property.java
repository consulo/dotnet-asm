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

package consulo.internal.dotnet.asm.mbel;

import consulo.internal.dotnet.asm.signature.BaseCustomAttributeOwner;
import consulo.internal.dotnet.asm.signature.PropertyAttributes;
import consulo.internal.dotnet.asm.signature.PropertySignature;

/**
 * This class represents a .NET property. Properties have a name and property signature,
 * as well as references to their getter and setter methods. A Property may optionally have
 * a default value, given in raw byte array form.
 *
 * @author Michael Stepp
 */
public class Property extends BaseCustomAttributeOwner implements PropertyAttributes
{
	private long PropertyRID = -1L;

	private int Flags;
	private String Name;
	private PropertySignature signature;
	private MethodDef getter, setter;
	private byte[] defaultValue;


	/**
	 * Makes a new Property with the given name, flags, and signature
	 *
	 * @param name  the name of the property
	 * @param flags a bit vector of flags (defined in PropertyAttributes)
	 * @param sig   the property signature for this property
	 */
	public Property(String name, int flags, PropertySignature sig)
	{
		Name = name;
		Flags = flags;
		signature = sig;
	}

	/**
	 * Returns the Property RID of this Property (used by emitter)
	 */
	public long getPropertyRID()
	{
		return PropertyRID;
	}

	/**
	 * Sets the Property RID of this Property (used by emitter).
	 * This method can only be called once.
	 */
	public void setPropertyRID(long rid)
	{
		if(PropertyRID == -1L)
		{
			PropertyRID = rid;
		}
	}

	/**
	 * Returns the default value for this property, or null if none
	 */
	public byte[] getDefaultValue()
	{
		return defaultValue;
	}

	/**
	 * Sets the default value for this property. Note that this method does not
	 * enforce that the byte given is the correct size for this property's underlying type.
	 * Passing null or a byte array with 0 length removes the default value.
	 */
	public void setDefaultValue(byte[] blob)
	{
		if(blob == null || blob.length == 0)
		{
			Flags &= ~HasDefault;
			defaultValue = null;
		}
		else
		{
			Flags |= HasDefault;
			defaultValue = blob;
		}
	}

	/**
	 * Returns the method reference of the Getter of this property
	 */
	public MethodDef getGetter()
	{
		return getter;
	}

	/**
	 * Sets the Getter method for this property
	 */
	public void setGetter(MethodDef get)
	{
		getter = get;
	}

	/**
	 * Returns the method reference of the Setter of this property
	 */
	public MethodDef getSetter()
	{
		return setter;
	}

	/**
	 * Sets the Setter method for this property
	 */
	public void setSetter(MethodDef set)
	{
		setter = set;
	}

	/**
	 * Returns the name fot his property
	 */
	public String getName()
	{
		return Name;
	}

	/**
	 * Sets the name of this property
	 */
	public void setName(String name)
	{
		Name = name;
	}

	/**
	 * Returns a bit vector of flags for this property (defined in PropertyAttributes)
	 */
	public int getFlags()
	{
		return Flags;
	}

	/**
	 * Sets the flags for this property
	 */
	public void setFlags(int flags)
	{
		Flags = flags;
	}

	/**
	 * Returns the property signature for this property
	 */
	public PropertySignature getSignature()
	{
		return signature;
	}

	/**
	 * Sets the property signature for this property
	 */
	public void setSignature(PropertySignature sig)
	{
		signature = sig;
	}

	/**
	 * Compares 2 properties
	 * Returns true iff the names are equal (within a TypeDef)
	 */
	public boolean equals(Object o)
	{
		if(o == null || !(o instanceof Property))
		{
			return false;
		}
		Property p = (Property) o;
		return Name.equals(p.Name);
	}
   
/*
   public void output(){
      System.out.print("Property[Name=\""+Name+"\", Signature=");
      signature.output();
      if (getter!=null){
         System.out.print(", Getter="+getter.getName());
      }
      if (setter!=null){
         System.out.print(", Setter="+setter.getName());
      }
      System.out.print("]");
   }
*/
}
