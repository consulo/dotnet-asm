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

import consulo.internal.dotnet.asm.signature.AssemblyFlags;
import consulo.internal.dotnet.asm.signature.AssemblyHashAlgorithm;
import consulo.internal.dotnet.asm.signature.BaseCustomAttributeOwner;

import jakarta.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class contains the extra information that a prime module would have.
 * The Module instance representing the prime module of this assembly will own an AssemblyInfo
 * object. It directly maps to an Assembly metadata table. An AssemblyIno may have DeclSecurity
 * objects, exported types, and CustomAttributes.
 *
 * @author Michael Stepp
 */
public class AssemblyInfo extends BaseCustomAttributeOwner implements HasSecurity, AssemblyHashAlgorithm, AssemblyFlags, consulo.internal.dotnet.asm.signature.Culture
{
	private long HashAlgId;
	private int MajorVersion, MinorVersion, BuildNumber, RevisionNumber;
	private long Flags;
	private byte[] PublicKey;  // blob
	private String Name;
	private String Culture;
	private DeclSecurity security;
	private List<ExportedTypeRef> exportedTypes = Collections.emptyList();

	/**
	 * Makes a new AssemblyInfo with the given information
	 *
	 * @param hashId  a constant indicating the type of hash algorithm used (defined in AssemblyHashAlgorithm)
	 * @param maj     the major version of this assembly
	 * @param min     the minor version of this assembly
	 * @param bn      the build number of this assembly
	 * @param rn      the revision number of this assembly
	 * @param flags   a bit vector of flags for this assembly (defined in AssemblyFlags)
	 * @param pkey    the public key blob for this assembly
	 * @param name    the logical name of this assembly
	 * @param culture the culture of this assembly (defined in Culture.ValidCultures[])
	 */
	public AssemblyInfo(long hashId, int maj, int min, int bn, int rn, long flags, byte[] pkey, String name, String culture)
	{
		HashAlgId = hashId;
		MajorVersion = maj;
		MinorVersion = min;
		BuildNumber = bn;
		RevisionNumber = rn;
		Flags = flags;
		PublicKey = pkey;
		if(PublicKey == null)
		{
			PublicKey = new byte[0];
		}
		Name = name;
		Culture = culture;
	}

	/**
	 * Returns a non-null array of the exported types in this assembly
	 */
	@Nonnull
	public List<ExportedTypeRef> getExportedTypes()
	{
		return exportedTypes;
	}

	/**
	 * Adds an exported type declaration to this assembly
	 */
	public void addExportedType(ExportedTypeRef ref)
	{
		if(exportedTypes.isEmpty())
		{
			exportedTypes = new ArrayList<ExportedTypeRef>(5);
		}
		exportedTypes.add(ref);
	}

	/**
	 * Removes an exported type declaration from this assembly
	 */
	public void removeExportedType(ExportedTypeRef ref)
	{
		exportedTypes.remove(ref);
	}

	/**
	 * Returns the logical name of this assembly
	 */
	public String getName()
	{
		return Name;
	}

	/**
	 * Sets the logical name of this assembly
	 */
	public void setName(String name)
	{
		Name = name;
	}

	/**
	 * Returns the hash algorithm id value for this assembly (defined in AssemblyHashAlgorithm)
	 */
	public long getHashAlg()
	{
		return HashAlgId;
	}

	/**
	 * Sets the hash algorithm id for this assembly
	 */
	public void setHashAlg(long hash)
	{
		HashAlgId = hash;
	}

	/**
	 * Returns the major version of this assembly
	 */
	public int getMajorVersion()
	{
		return MajorVersion;
	}

	/**
	 * Sets the major version of this assembly
	 */
	public void setMajorVersion(int mv)
	{
		MajorVersion = mv;
	}

	/**
	 * Returns the minor version of this assembly
	 */
	public int getMinorVersion()
	{
		return MinorVersion;
	}

	/**
	 * Sets the minor version of this assembly
	 */
	public void setMinorVersion(int mv)
	{
		MinorVersion = mv;
	}

	/**
	 * Returns the build number of this assembly
	 */
	public int getBuildNumber()
	{
		return BuildNumber;
	}

	/**
	 * Sets the build number of this assembly
	 */
	public void setBuildNumber(int bn)
	{
		BuildNumber = bn;
	}

	/**
	 * Returns the revision numebr of this assembly
	 */
	public int getRevisionNumber()
	{
		return RevisionNumber;
	}

	/**
	 * Sets the revision number of this assembly
	 */
	public void setRevisionNumber(int rn)
	{
		RevisionNumber = rn;
	}

	/**
	 * Returns a bit vector of flags for this assembly (defined in AssemblyFlags)
	 */
	public long getFlags()
	{
		return Flags;
	}

	/**
	 * Sets the flags for this assembly
	 */
	public void setFlags(long flags)
	{
		Flags = flags;
	}

	/**
	 * Returns the public key blob for this assembly
	 */
	public byte[] getPublicKey()
	{
		return PublicKey;
	}

	/**
	 * Sets the public key blob for this assembly
	 */
	public void setPublicKey(byte[] blob)
	{
		PublicKey = blob;
	}

	/**
	 * Returns the culture string for this assembly (defined in Culture.ValidCultures[])
	 */
	public String getCulture()
	{
		return Culture;
	}

	/**
	 * Sets the culture strign for this assembly
	 */
	public void setCulture(String cult)
	{
		Culture = cult;
	}

	/**
	 * Returns the DeclSecurity object for this Assembly (may be null)
	 */
	public DeclSecurity getDeclSecurity()
	{
		return security;
	}

	/**
	 * Sets the DeclSecurity on this assembly (null value removes DeclSecurity)
	 */
	public void setDeclSecurity(DeclSecurity decl)
	{
		security = decl;
	}
   
/*
   public void output(){
      System.out.print("AssemblyInfo[Name=\""+Name+"\"");
      if (security!=null){
         System.out.print(", Security=");
         security.output();
      }
      System.out.print("]");
   }
*/
}
