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

import consulo.internal.dotnet.asm.io.ByteBuffer;
import consulo.internal.dotnet.asm.io.MSILInputStream;
import consulo.internal.dotnet.asm.metadata.GenericTableValue;
import consulo.internal.dotnet.asm.metadata.TableConstants;
import consulo.internal.dotnet.asm.parse.MSILParseException;
import consulo.internal.dotnet.asm.parse.PEModule;
import consulo.internal.dotnet.asm.signature.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * This class is all that is needed to parse a Module from a file.
 * The user will construct a ClassParser, then call parseMetadata and get back an MBEL Module.
 * The ClassParser uses lots of memory, so it should be discarded after the call to parseMetadata.
 *
 * @author Michael Stepp
 */
public class ModuleParser extends BaseCustomAttributeOwner
{
	private static final Logger LOG = LoggerFactory.getLogger(ModuleParser.class);

	private static class TypeDefTempInfo
	{
		long FieldList;
		long MethodList;
		long Extends;
	}

	public static final int VERSION = 2;

	@Nonnull
	public static AssemblyInfo parseAssemblyInfo(File file) throws IOException, MSILParseException
	{
		ModuleParser parser = new ModuleParser(file, TableConstants.Assembly);
		parser.buildAssemblyInfo();
		return parser.getAssemblyInfo();
	}

	private PEModule pe_module;
	private GenericTableValues myTableValues;
	private TableConstants tc;

	private MSILInputStream in;
	/////////////////////////////////////////////
	private TypeDef[] typeDefs = null;

	private TypeRef[] typeRefs = null;
	private TypeSpec[] typeSpecs = null;
	private MethodDef[] methods = null;
	private GenericParamDef[] myGenericParams;
	private GenericParamConstraintDef[] myGenericParamConstraints;
	private Field[] fields = null;
	private ParameterInfo[] params = null;
	private Property[] properties = null;
	private Event[] events = null;
	private FileReference[] fileReferences = null;
	private ExportedTypeRef[] exportedTypes = null;
	private TypeGroup group = null;
	private ManifestResource[] mresources = null;
	private AssemblyInfo assemblyInfo = null;
	private MemberRef[] memberRefs = null;
	private EntryPoint entryPoint = null;
	private AssemblyRefInfo[] assemblyRefs = null;
	private InterfaceImplementation[] interfaceImpls = null;
	private ModuleRefInfo[] moduleRefs = null;
	private DeclSecurity[] declSecurities = null;
	private StandAloneSignature[] standAloneSigs = null;

	private int Generation;
	private String Name;
	private byte[] Mvid;                // GUID
	private byte[] EncID;               // GUID
	private byte[] EncBaseID;           // GUID

	/**
	 * Makes a ClassParser that uses the given input stream.
	 * It is assumed that the input stream is open to the beginning of a .NET module file.
	 */
	public ModuleParser(File file) throws IOException, MSILParseException
	{
		this(file, -1);
	}

	private ModuleParser(File file, int tableIndexStop) throws IOException, MSILParseException
	{
		in = new MSILInputStream(file);
		pe_module = new PEModule(in);
		tc = pe_module.metadata.parseTableConstants(in, tableIndexStop);
		myTableValues = tc.getTables();
		if(tableIndexStop == -1)
		{
			parse();
		}
	}

	/**
	 * Returns a method signature given the metadata token for a StandAloneSig table
	 */
	public MethodSignature getStandAloneSignature(long token)
	{
		long type = (token >> 24) & 0xFFL;
		long tokrow = token & 0xFFFFFFL;

		if(type == TableConstants.StandAloneSig)
		{
			return (MethodSignature) standAloneSigs[(int) tokrow - 1];
		}
		return null;
	}

	/**
	 * Returns the method corresponding to the given Method token.
	 * To be used by VTableFixups as a callback.
	 */
	public MethodDef getVTableFixup(long methodToken)
	{
		long toktype = (methodToken >> 24) & 0xFFL;
		long tokrow = (methodToken & 0xFFFFFFL);

		if(toktype != TableConstants.Method)
		{
			return null;
		}
		return methods[(int) (tokrow - 1L)];
	}

	/**
	 * Returns the Method or MethodRef corresponding to the given token.
	 * This is called by instructions that deal with methods.
	 */
	public MethodDefOrRef getMethodDefOrRef(long token)
	{
		long type = (token >> 24) & 0xFFL;
		long tokrow = token & 0xFFFFFFL;

		if(type == TableConstants.Method)
		{
			return methods[(int) getMethod(tokrow) - 1];
		}
		else if(type == TableConstants.MemberRef)
		{
			return (MethodDefOrRef) memberRefs[(int) tokrow - 1];
		}
		return null;
	}

	/**
	 * Returns the FieldRef corresponding to the given token.
	 * This is called by instructions that deal with fields.
	 */
	public FieldRef getFieldRef(long token)
	{
		long type = (token >> 24) & 0xFFL;
		long tokrow = (token & 0xFFFFFFL);

		if(type == TableConstants.Field)
		{
			return fields[(int) getField(tokrow) - 1];
		}
		else if(type == TableConstants.MemberRef)
		{
			return (FieldRef) memberRefs[(int) tokrow - 1];
		}
		return null;
	}

	/**
	 * Returns the User String corresponding to the given token.
	 * This is called by LDSTR instructions.
	 */
	public String getUserString(long token)
	{
		long type = (token >> 24) & 0xFFL;
		long tokrow = token & 0xFFFFFFL;
		if(type != TableConstants.USString)
		{
			return null;
		}
		return tc.getUSString(tokrow);
	}

	/**
	 * Returns a local variable list, given the token of a StandAloneSig table
	 */
	public LocalVarList getLocalVarList(long token)
	{
		long type = (token >> 24) & 0xFFL;
		long tokrow = (token & 0xFFFFFFL);

		if(type == TableConstants.StandAloneSig)
		{
			return (LocalVarList) standAloneSigs[(int) tokrow - 1];
		}
		return null;
	}

	/**
	 * Returns an AbstractTypeReference corresponding to the given token.
	 * This is called by instructions that deal with types.
	 */
	public AbstractTypeReference getClassRef(long token)
	{
		long type = (token >> 24) & 0xFFL;
		long tokrow = token & 0xFFFFFFL;

		if(type == TableConstants.TypeDef)
		{
			return typeDefs[(int) tokrow - 1];
		}
		else if(type == TableConstants.TypeRef)
		{
			return typeRefs[(int) tokrow - 1];
		}
		else if(type == TableConstants.TypeSpec)
		{
			return typeSpecs[(int) tokrow - 1];
		}

		return null;
	}

	/**
	 * Returns the TypeGroup of all defined types in this module (once made)
	 */
	public TypeGroup getTypeGroup()
	{
		return group;
	}

	/**
	 * This is the big workhorse that calls all the other private methods in this class.
	 * It will parse the various structures in the most convenient order possible, all of which
	 * are either accessible from the Module, or are unimportant and discarded.
	 */
	private void parse() throws IOException, MSILParseException
	{
		// FIXME [VISTALL] drop unused
		myTableValues.getAndDrop(TableConstants.MethodSpec);

		buildAssemblyInfo();
		TypeDefTempInfo[] typeDefTempInfos = buildTypeDefs();
		setNestedClasses();

		buildModule();
		buildModuleRefs();

		buildAssemblyRefs();
		buildFileReferences();
		buildManifestResources();
		buildExportedTypes();

		buildTypeRefs();
		buildTypeSpecs();
		setSuperClasses(typeDefTempInfos);

		group = new TypeGroup(typeDefs, typeRefs, typeSpecs);

		setInterfaceImpls();

		buildFields();
		setFieldLayouts();
		setFieldRVAs();
		if(myTableValues.get(TableConstants.Param) != null)
		{
			params = new ParameterInfo[myTableValues.get(TableConstants.Param).length];
		}
		buildMethods();
		setImplMaps();
		setDeclSecurity();

		setFieldsAndMethods(typeDefTempInfos);

		buildProperties();
		setPropertyMaps();
		setClassLayouts();
		buildEvents();
		setEventMaps();
		setFieldMarshals();
		setMethodSemantics();
		setDefaultValues();

		buildMemberRefs();
		buildGenericParams();
		buildGenericParamConstraints();
		buildEntryPoint();
		buildStandAloneSigs();

		setMethodMaps();

		setCustomAttributes();

		pe_module.bufferSections(in);
		myTableValues = null;
	}

	//////////////////////////////////////////////////////////////////////////////////

	private long getMethod(long token)
	{
		// maps tokens through MethodPtrs, if necessary
		if(myTableValues.get(TableConstants.MethodPtr) != null)
		{
			return myTableValues.get(TableConstants.MethodPtr)[(int) token - 1].getTableIndex("Method");
		}
		else
		{
			return token;
		}
	}

	private long getField(long token)
	{
		if(myTableValues.get(TableConstants.FieldPtr) != null)
		{
			return myTableValues.get(TableConstants.FieldPtr)[(int) token - 1].getTableIndex("Field");
		}
		else
		{
			return token;
		}
	}

	private long getEvent(long token)
	{
		if(myTableValues.get(TableConstants.EventPtr) != null)
		{
			return myTableValues.get(TableConstants.EventPtr)[(int) token - 1].getTableIndex("Event");
		}
		else
		{
			return token;
		}
	}

	private long getParam(long token)
	{
		if(myTableValues.get(TableConstants.ParamPtr) != null)
		{
			return myTableValues.get(TableConstants.ParamPtr)[(int) token - 1].getTableIndex("Param");
		}
		else
		{
			return token;
		}
	}

	private long getProperty(long token)
	{
		if(myTableValues.get(TableConstants.PropertyPtr) != null)
		{
			return myTableValues.get(TableConstants.PropertyPtr)[(int) token - 1].getTableIndex("Property");
		}
		else
		{
			return token;
		}
	}

	//////////////////////////////////////////////////////////////////

	private void buildAssemblyInfo()
	{
		// build Assembly table (after Module) DONE!
		GenericTableValue[] row = myTableValues.getAndDrop(TableConstants.Assembly);
		if(row == null)
		{
			return;
		}

		GenericTableValue ass = row[0];

		long hash = ass.getConstant("HashAlgID").longValue();
		int maj = ass.getConstant("MajorVersion").intValue();
		int min = ass.getConstant("MinorVersion").intValue();
		int bn = ass.getConstant("BuildNumber").intValue();
		int rn = ass.getConstant("RevisionNumber").intValue();
		String name = ass.getString("Name");
		String culture = ass.getString("Culture");
		byte[] publicKey = ass.getBlob("PublicKey");
		long flags = ass.getConstant("Flags").longValue();

		assemblyInfo = new AssemblyInfo(hash, maj, min, bn, rn, flags, publicKey, name, culture);
	}

	private void buildAssemblyRefs()
	{
		// build AssemblyRef table DONE!
		GenericTableValue[] row = myTableValues.getAndDrop(TableConstants.AssemblyRef);
		if(row == null)
		{
			return;
		}

		assemblyRefs = new AssemblyRefInfo[row.length];
		for(int i = 0; i < row.length; i++)
		{
			int Maj = row[i].getConstant("MajorVersion").intValue();
			int Min = row[i].getConstant("MinorVersion").intValue();
			int BN = row[i].getConstant("BuildNumber").intValue();
			int RN = row[i].getConstant("RevisionNumber").intValue();
			long flags = row[i].getConstant("Flags").longValue();
			byte[] pb = row[i].getBlob("PublicKeyOrToken");
			String name = row[i].getString("Name");
			String cult = row[i].getString("Culture");
			byte[] hash = row[i].getBlob("HashValue");

			assemblyRefs[i] = new AssemblyRefInfo(Maj, Min, BN, RN, flags, pb, name, cult, hash);
		}
	}

	private void buildModule()
	{
		// build Module (after Assembly) DONE!
		GenericTableValue[] row = myTableValues.getAndDrop(TableConstants.Module);
		if(row == null)
		{
			return;
		}

		GenericTableValue mod = row[0];
		String name = mod.getString("Name");
		int generation = mod.getConstant("Generation").intValue();
		byte[] mvid = mod.getGUID("Mvid");
		byte[] encid = mod.getGUID("EncID");
		byte[] encbaseid = mod.getGUID("EncBaseID");

		Generation = generation;
		Mvid = mvid;
		EncID = encid;
		EncBaseID = encbaseid;
	}

	private void buildModuleRefs()
	{
		// build ModuleRef tables DONE!
		GenericTableValue[] row = myTableValues.getAndDrop(TableConstants.ModuleRef);
		if(row == null)
		{
			return;
		}

		moduleRefs = new ModuleRefInfo[row.length];
		for(int i = 0; i < row.length; i++)
		{
			String modName = row[i].getString("Name");
			moduleRefs[i] = new ModuleRefInfo(modName);
		}
	}

	private void buildEntryPoint()
	{
		// build Entrypoint (after Module)
		long entrytoken = pe_module.cliHeader.EntryPointToken;
		long type = (entrytoken >> 24) & 0xFFL;
		long tokrow = (entrytoken & 0xFFFFFFL);

		if(type == TableConstants.Method)
		{
			entryPoint = new EntryPoint(methods[(int) getMethod(tokrow) - 1]);
		}
		else if(type == TableConstants.File)
		{
			entryPoint = new EntryPoint(fileReferences[(int) tokrow - 1]);
		}
	}

	private void buildFileReferences()
	{
		// build Files DONE!
		GenericTableValue[] row = myTableValues.getAndDrop(TableConstants.File);
		if(row == null)
		{
			return;
		}

		fileReferences = new FileReference[row.length];
		for(int i = 0; i < row.length; i++)
		{
			long flags = row[i].getConstant("Flags").longValue();
			String name = row[i].getString("Name");
			byte[] hashValue = row[i].getBlob("HashValue");

			fileReferences[i] = new FileReference(flags, name, hashValue);
		}
	}

	private void buildManifestResources() throws IOException
	{
		// build ManifestResources (after FileReferences) DONE!
		GenericTableValue[] row = myTableValues.getAndDrop(TableConstants.ManifestResource);
		if(row == null)
		{
			return;
		}

		mresources = new ManifestResource[row.length];
		for(int i = 0; i < row.length; i++)
		{
			long coded = row[i].getCodedIndex("Implementation");
			String name = row[i].getString("Name");
			long flags = row[i].getConstant("Flags").longValue();

			if(coded == 0)
			{
				// LocalManifestResource
				long RVA = pe_module.cliHeader.Resources.VirtualAddress;
				long fp = in.getFilePointer(RVA);
				fp += row[i].getConstant("Offset").longValue();
				in.seek(fp);
				long size = in.readDWORD();
				byte[] data = new byte[(int) size];
				in.read(data);
				mresources[i] = new LocalManifestResource(name, flags, data);
			}
			else
			{
				long token[] = tc.parseCodedIndex(coded, TableConstants.Implementation);
				if(token[0] == TableConstants.File)
				{
					// FileManifestResource
					mresources[i] = new FileManifestResource(fileReferences[(int) token[1] - 1], flags);
				}
				else if(token[0] == TableConstants.AssemblyRef)
				{
					// AssemblyManifestResource
					AssemblyManifestResource res = new AssemblyManifestResource(name, flags, assemblyRefs[(int) token[1] - 1]);
					mresources[i] = res;
				}
			}
		}
	}

	private void buildExportedTypes()
	{
		// build ExportedTypes (after File) DONE!
		GenericTableValue[] row = myTableValues.getAndDrop(TableConstants.ExportedType);
		if(row == null)
		{
			return;
		}

		exportedTypes = new ExportedTypeRef[row.length];
		for(int i = 0; i < row.length; i++)
		{
			String ns = row[i].getString("TypeNamespace");
			String name = row[i].getString("TypeName");
			long flags = row[i].getConstant("Flags").longValue();

			exportedTypes[i] = new ExportedTypeRef(ns, name, flags);
			if(assemblyInfo != null)
			{
				assemblyInfo.addExportedType(exportedTypes[i]);
			}
		}
		for(int i = 0; i < row.length; i++)
		{
			long coded = row[i].getCodedIndex("Implementation");
			long[] token = tc.parseCodedIndex(coded, TableConstants.Implementation);
			if(token[0] == TableConstants.ExportedType)
			{
				exportedTypes[i].setExportedTypeRef(getByLongIndex(exportedTypes, token[1]));
			}
			else if(token[0] == TableConstants.File)
			{
				exportedTypes[i].setFileReference(getByLongIndex(fileReferences, token[1]));
			}
			else if(token[0] == TableConstants.AssemblyRef)
			{
				exportedTypes[i].setAssemblyRefInfo(getByLongIndex(assemblyRefs, token[1]));
			}
			else
			{
				throw new IllegalArgumentException();
			}
		}
	}

	private void buildFields()
	{
		// build Fields (after TypeGroup) DONE!
		GenericTableValue[] row = myTableValues.getAndDrop(TableConstants.Field);
		if(row == null)
		{
			return;
		}

		fields = new Field[row.length];
		for(int i = 0; i < row.length; i++)
		{
			int Flags = row[i].getConstant("Flags").intValue();
			String name = row[i].getString("Name");
			byte[] blob = row[i].getBlob("Signature");
			FieldSignature sig = FieldSignature.parse(new ByteBuffer(blob),
					group);
			fields[i] = new Field(name, sig);
			fields[i].setFlags(Flags);
			// does not set parent!
		}
	}

	private void setFieldLayouts()
	{
		// build FieldLayouts (after Fields) DONE!
		GenericTableValue[] row = myTableValues.getAndDrop(TableConstants.FieldLayout);
		if(row == null)
		{
			return;
		}

		for(GenericTableValue aRow : row)
		{
			long field = getField(aRow.getTableIndex("Field"));
			long Offset = aRow.getConstant("Offset").longValue();
			fields[(int) field - 1].setOffset(Offset);
		}
	}

	private void buildMethods()
	{
		// build Methods (after Params and TypeGroup)
		GenericTableValue[] row = myTableValues.getAndDrop(TableConstants.Method);
		if(row == null)
		{
			return;
		}

		methods = new MethodDef[row.length];
		for(int i = 0; i < row.length; i++)
		{
			long RVA = row[i].getConstant("RVA").longValue();
			String name = row[i].getString("Name");
			int implFlags = row[i].getConstant("ImplFlags").intValue();
			int flags = row[i].getConstant("Flags").intValue();
			byte[] blob = row[i].getBlob("Signature");

			MethodSignature sig = MethodSignature.parse(new ByteBuffer(blob), group);

			methods[i] = new MethodDef(name, implFlags, flags, sig);

			if((RVA != 0) && (implFlags & MethodDef.CodeTypeMask) == MethodDef.Native)
			{
				methods[i].setMethodRVA(RVA);
			}
		}

		// add params DONE!
		for(int i = 0; i < row.length; i++)
		{
			List<ParameterSignature> pSigs = methods[i].getSignature().getParameters();

			long startI = row[i].getTableIndex("ParamList");
			if(!(startI == 0 || myTableValues.get(TableConstants.Param) == null || startI > myTableValues.get(TableConstants.Param).length))
			{
				// valid start of paramlist
				long endI = myTableValues.get(TableConstants.Param).length + 1;
				if(i < row.length - 1)
				{
					endI = Math.min(endI, row[i + 1].getTableIndex("ParamList").longValue());
				}

				for(long j = startI; j < endI; j++)
				{
					GenericTableValue paramTable = myTableValues.get(TableConstants.Param)[(int) getParam(j) - 1];
					int flags = paramTable.getConstant("Flags").intValue();
					int seq = paramTable.getConstant("Sequence").intValue();
					String name = paramTable.getString("Name");
					if(seq == 0)
					{
						params[(int) getParam(j) - 1] = new ParameterInfo(name, flags);
						methods[i].getSignature().getReturnType().setParameterInfo(params[(int) getParam(j) - 1]);
					}
					else
					{
						params[(int) getParam(j) - 1] = new ParameterInfo(name, flags);
						pSigs.get(seq - 1).setParameterInfo(params[(int) getParam(j) - 1]);
					}
				}
			}
		}
	}

	private void setImplMaps()
	{
		// build ImplMaps (after Methods) DONE!
		GenericTableValue[] row = myTableValues.getAndDrop(TableConstants.ImplMap);
		if(row == null)
		{
			return;
		}

		for(GenericTableValue aRow : row)
		{
			long coded = aRow.getCodedIndex("MemberForwarded");
			long token[] = tc.parseCodedIndex(coded, TableConstants.MemberForwarded);
			if(token[0] != TableConstants.Method)
			{
				continue;
			}
			long method = getMethod(token[1]);

			int flags = aRow.getConstant("MappingFlags").intValue();
			String name = aRow.getString("ImportName");
			long modref = aRow.getTableIndex("ImportScope");

			methods[(int) method - 1].setImplementationMap(new ImplementationMap(flags, name, moduleRefs[(int) modref - 1]));
		}
	}

	private void setDeclSecurity()
	{
		// build DeclSecurity (after Assembly, Method, and TypeDefs) DONE!
		GenericTableValue[] row = myTableValues.getAndDrop(TableConstants.DeclSecurity);
		if(row == null)
		{
			return;
		}

		declSecurities = new DeclSecurity[row.length];
		for(int i = 0; i < row.length; i++)
		{
			long coded = row[i].getCodedIndex("Parent");
			long token[] = tc.parseCodedIndex(coded, TableConstants.HasDeclSecurity);
			int Action = row[i].getConstant("Action").intValue();
			byte[] permission = row[i].getBlob("PermissionSet");
			declSecurities[i] = new DeclSecurity(Action, permission);

			if(token[0] == TableConstants.TypeDef)
			{
				typeDefs[(int) token[1] - 1].setDeclSecurity(declSecurities[i]);
			}
			else if(token[0] == TableConstants.Method)
			{
				methods[(int) getMethod(token[1]) - 1].setDeclSecurity(declSecurities[i]);
			}
			else if(token[0] == TableConstants.Assembly)
			{
				assemblyInfo.setDeclSecurity(declSecurities[i]);
			}
		}
	}

	@Nullable
	private TypeDefTempInfo[] buildTypeDefs()
	{
		// build TypeDefs (after Field and Methods) DONE!
		GenericTableValue[] row = myTableValues.getAndDrop(TableConstants.TypeDef);
		if(row == null)
		{
			return null;
		}

		TypeDefTempInfo[] tempInfos = new TypeDefTempInfo[row.length];

		typeDefs = new TypeDef[row.length];
		for(int i = 0; i < row.length; i++)
		{
			String name = row[i].getString("Name");
			String ns = row[i].getString("Namespace");
			long flags = row[i].getConstant("Flags").longValue();

			typeDefs[i] = new TypeDef(ns, name, flags);

			TypeDefTempInfo tempInfo = new TypeDefTempInfo();
			tempInfo.FieldList = row[i].getTableIndex("FieldList");
			tempInfo.MethodList = row[i].getTableIndex("MethodList");
			tempInfo.Extends = row[i].getTableIndex("Extends");

			tempInfos[i] = tempInfo;
		}

		return tempInfos;
	}

	private void setFieldsAndMethods(@Nullable TypeDefTempInfo[] typeDefTempInfos)
	{
		if(typeDefTempInfos == null)
		{
			return;
		}

		for(int i = 0; i < typeDefTempInfos.length; i++)
		{
			long fieldS = typeDefTempInfos[i].FieldList;
			if(!(fieldS == 0 || fields == null || fieldS > fields.length))
			{
				long fieldE = fields.length + 1;
				if(i < typeDefTempInfos.length - 1)
				{
					fieldE = Math.min(fieldE, typeDefTempInfos[i + 1].FieldList);
				}

				for(long j = fieldS; j < fieldE; j++)
				{
					typeDefs[i].addField(fields[(int) getField(j) - 1]);
				}
				// this sets the field parents
			}

			long methodS = typeDefTempInfos[i].MethodList;
			if(!(methodS == 0 || methods == null || methodS > methods.length))
			{
				long methodE = methods.length + 1;
				if(i < typeDefTempInfos.length - 1)
				{
					methodE = Math.min(methodE, typeDefTempInfos[i + 1].MethodList);
				}

				for(long j = methodS; j < methodE; j++)
				{
					typeDefs[i].addMethod(methods[(int) getMethod(j) - 1]);
				}
			}
		}
	}

	private void buildTypeRefs()
	{
		GenericTableValue[] row = myTableValues.getAndDrop(TableConstants.TypeRef);
		if(row == null)
		{
			return;
		}

		typeRefs = new TypeRef[row.length];
		for(int i = 0; i < row.length; i++)
		{
			long coded = row[i].getCodedIndex("ResolutionScope");

			if(coded == 0L)
			{
				// ExportedType
				String Name = row[i].getString("Name");
				String Namespace = row[i].getString("Namespace");

				for(ExportedTypeRef exportedType : exportedTypes)
				{
					if(Name.equals(exportedType.getName()) && Namespace.equals(exportedType.getNamespace()))
					{
						typeRefs[i] = exportedType;
					}
				}
				continue;
			}

			long[] token = tc.parseCodedIndex(coded, TableConstants.ResolutionScope);
			String Namespace = row[i].getString("Namespace");
			String Name = row[i].getString("Name");

			switch((int) token[0])
			{
				case TableConstants.ModuleRef:
				{
					ModuleTypeRef mod = new ModuleTypeRef(moduleRefs[(int) token[1] - 1], Namespace, Name);
					typeRefs[i] = mod;
					break;
				}

				case TableConstants.TypeRef:
				{
					NestedTypeRef nest = new NestedTypeRef(Namespace, Name, typeRefs[(int) token[1] - 1]);
					typeRefs[i] = nest;
					break;
				}

				case TableConstants.AssemblyRef:
				{
					AssemblyTypeRef assem = new AssemblyTypeRef(assemblyRefs[(int) token[1] - 1], Namespace, Name);
					typeRefs[i] = assem;
					break;
				}

				case TableConstants.Module:
				{
					// (Implementation == 0x4?)
					// search through TypeDefs for Name and Namespace
					for(TypeDef typeDef : typeDefs)
					{
						if(typeDef.getName().equals(Name) && typeDef.getNamespace().equals(Namespace))
						{
							typeRefs[i] = typeDef;
						}
					}
					break;
				}
				default:
					System.out.println("Unsupported token in buildTypeRefs(): 0x" + Long.toHexString(token[0]));
					break;
			}
		}
	}

	private void buildTypeSpecs()
	{
		GenericTableValue[] row = myTableValues.getAndDrop(TableConstants.TypeSpec);
		if(row == null)
		{
			return;
		}

		typeSpecs = new TypeSpec[row.length];
		for(int i = 0; i < row.length; i++)
		{
			typeSpecs[i] = new TypeSpec(null);
		}

		byte[] blob = null;
		TypeSignature sig = null;
		for(int i = 0; i < row.length; i++)
		{
			blob = row[i].getBlob("Signature");
			sig = TypeSignatureParser.parse(new ByteBuffer(blob), new TypeGroup(typeDefs, typeRefs, typeSpecs));
			typeSpecs[i].setSignature(sig);
			//module.addTypeSpec(typeSpecs[i]);
		}
	}

	private void setSuperClasses(TypeDefTempInfo[] tempInfos)
	{
		if(tempInfos == null)
		{
			return;
		}

		for(int i = 0; i < tempInfos.length; i++)
		{
			long coded = tempInfos[i].Extends;
			if(coded != 0L)
			{
				long[] token = tc.parseCodedIndex(coded, TableConstants.TypeDefOrRefOrSpec);
				if(token[0] == TableConstants.TypeDef)
				{
					typeDefs[i].setSuperClass(getByLongIndex(typeDefs, token[1]));
				}
				else if(token[0] == TableConstants.TypeRef)
				{
					typeDefs[i].setSuperClass(getByLongIndex(typeRefs, token[1]));
				}
				else if(token[0] == TableConstants.TypeSpec)
				{
					typeDefs[i].setSuperClass(getByLongIndex(typeSpecs, token[1]));
				}
				else
				{
					throw new IllegalArgumentException();
				}
			}
		}
	}

	private void setInterfaceImpls()
	{
		GenericTableValue[] row = myTableValues.getAndDrop(TableConstants.InterfaceImpl);
		if(row == null)
		{
			return;
		}

		interfaceImpls = new InterfaceImplementation[row.length];
		for(int i = 0; i < row.length; i++)
		{
			long clazz = row[i].getTableIndex("Class");
			TypeDef def = typeDefs[(int) clazz - 1];
			long coded = row[i].getCodedIndex("Interface");
			long inter[] = tc.parseCodedIndex(coded, TableConstants.TypeDefOrRefOrSpec);

			if(inter[0] == TableConstants.TypeDef)
			{
				interfaceImpls[i] = new InterfaceImplementation(typeDefs[(int) inter[1] - 1]);
			}
			else if(inter[0] == TableConstants.TypeRef)
			{
				interfaceImpls[i] = new InterfaceImplementation(typeRefs[(int) inter[1] - 1]);
			}
			else if(inter[0] == TableConstants.TypeSpec)
			{
				interfaceImpls[i] = new InterfaceImplementation(typeSpecs[(int) inter[1] - 1]);
			}
			def.addInterface(interfaceImpls[i]);
		}
	}

	private void buildProperties()
	{
		// build Properties DONE!
		GenericTableValue[] row = myTableValues.getAndDrop(TableConstants.Property);
		if(row == null)
		{
			return;
		}

		properties = new Property[row.length];
		for(int i = 0; i < row.length; i++)
		{
			String name = row[i].getString("Name");
			int flags = row[i].getConstant("Flags").intValue();
			byte[] blob = row[i].getBlob("Type");
			PropertySignature sig = PropertySignature.parse(new ByteBuffer(blob), group);

			properties[i] = new Property(name, flags, sig);
		}
	}

	private void setPropertyMaps()
	{
		// build PropertyMap (after TypeDefs and Property) DONE!
		GenericTableValue[] row = myTableValues.getAndDrop(TableConstants.PropertyMap);
		if(row == null)
		{
			return;
		}

		for(int i = 0; i < row.length; i++)
		{
			long parent = row[i].getTableIndex("Parent");
			if(parent == 0)
			{
				continue;
			}
			long propS = row[i].getTableIndex("PropertyList");
			if(propS == 0 || properties == null || propS > properties.length)
			{
				continue;
			}
			long propE = properties.length + 1;
			if(i < row.length - 1)
			{
				propE = Math.min(propE, row[i + 1].getTableIndex("PropertyList").longValue());
			}
			for(long j = propS; j < propE; j++)
			{
				typeDefs[(int) parent - 1].addProperty(properties[(int) getProperty(j) - 1]);
			}
		}
	}

	private void setNestedClasses()
	{
		GenericTableValue[] row = myTableValues.getAndDrop(TableConstants.NestedClass);
		if(row == null)
		{
			return;
		}

		for(GenericTableValue aRow : row)
		{
			long nest = aRow.getTableIndex("NestedClass");
			long enclose = aRow.getTableIndex("EnclosingClass");

			getByLongIndex(typeDefs, enclose).addNestedClass(getByLongIndex(typeDefs, nest));
		}
	}

	private void setClassLayouts()
	{
		GenericTableValue[] row = myTableValues.getAndDrop(TableConstants.ClassLayout);
		if(row == null)
		{
			return;
		}

		for(GenericTableValue aRow : row)
		{
			long typedef = aRow.getTableIndex("Parent");
			int pSize = aRow.getConstant("PackingSize").intValue();
			long cSize = aRow.getConstant("ClassSize").longValue();

			ClassLayout layout = new ClassLayout(pSize, cSize);
			typeDefs[(int) typedef - 1].setClassLayout(layout);
		}
	}

	private void setFieldRVAs()
	{
		GenericTableValue[] row = myTableValues.getAndDrop(TableConstants.FieldRVA);
		if(row == null)
		{
			return;
		}

		for(GenericTableValue aRow : row)
		{
			long RVA = aRow.getConstant("RVA").longValue();
			long field = getField(aRow.getTableIndex("Field"));
			fields[(int) field - 1].setFieldRVA(RVA);
		}
	}

	private void buildEvents()
	{
		GenericTableValue[] row = myTableValues.getAndDrop(TableConstants.Event);
		if(row == null)
		{
			return;
		}

		events = new Event[row.length];
		for(int i = 0; i < row.length; i++)
		{
			String name = row[i].getString("Name");
			int flags = row[i].getConstant("EventFlags").intValue();

			AbstractTypeReference handler = null;
			long coded = row[i].getCodedIndex("EventType");
			long[] token = tc.parseCodedIndex(coded, TableConstants.TypeDefOrRefOrSpec);
			if(token[0] == TableConstants.TypeDef)
			{
				handler = getByLongIndex(typeDefs, token[1]);
			}
			else if(token[0] == TableConstants.TypeRef)
			{
				handler = getByLongIndex(typeRefs, token[1]);
			}
			else if(token[0] == TableConstants.TypeSpec)
			{
				handler = getByLongIndex(typeSpecs, token[1]);
			}
			else
			{
				throw new IllegalArgumentException();
			}

			events[i] = new Event(name, flags, handler);
		}
	}

	private void setEventMaps()
	{
		GenericTableValue[] row = myTableValues.getAndDrop(TableConstants.EventMap);
		if(row == null)
		{
			return;
		}

		for(int i = 0; i < row.length; i++)
		{
			long parent = row[i].getTableIndex("Parent");
			long eventS = row[i].getTableIndex("EventList");
			long eventE = events.length + 1;
			if(i < row.length - 1)
			{
				eventE = Math.min(eventE, row[i + 1].getTableIndex("EventList").longValue());
			}
			for(long j = eventS; j < eventE; j++)
			{
				typeDefs[(int) parent - 1].addEvent(events[(int) getEvent(j) - 1]);
			}
		}
	}

	private void setFieldMarshals()
	{
		GenericTableValue[] row = myTableValues.getAndDrop(TableConstants.FieldMarshal);
		if(row == null)
		{
			return;
		}

		for(GenericTableValue aRow : row)
		{
			long[] index = tc.parseCodedIndex(aRow.getCodedIndex("Parent"), TableConstants.HasFieldMarshal);
			byte[] blob = aRow.getBlob("NativeType");
			MarshalSignature sig = MarshalSignature.parse(new ByteBuffer(blob));

			if(index[0] == TableConstants.Field) // Field
			{
				fields[(int) getField(index[1]) - 1].setFieldMarshal(sig);
			}
			else // Param
			{
				params[(int) getParam(index[1]) - 1].setFieldMarshal(sig);
			}
		}
	}

	private void setMethodSemantics()
	{
		GenericTableValue[] row = myTableValues.getAndDrop(TableConstants.MethodSemantics);
		if(row == null)
		{
			return;
		}

		for(GenericTableValue aRow : row)
		{
			long method = getMethod(aRow.getTableIndex("Method"));
			int sem = aRow.getConstant("Semantics").intValue();
			long coded = aRow.getCodedIndex("Association");
			long token[] = tc.parseCodedIndex(coded, TableConstants.HasSemantics);
			MethodDef meth = methods[(int) method - 1];

			if(token[0] == TableConstants.Event)
			{
				Event event = events[(int) getEvent(token[1]) - 1];
				meth.setMethodSemantics(new MethodSemantics(sem, event));
				if(sem == MethodSemantics.AddOn)
				{
					event.setAddOnMethod(meth);
				}
				else if(sem == MethodSemantics.RemoveOn)
				{
					event.setRemoveOnMethod(meth);
				}
				else if(sem == MethodSemantics.Fire)
				{
					event.setFireMethod(meth);
				}
			}
			else if(token[0] == TableConstants.Property)
			{
				Property prop = properties[(int) getProperty(token[1]) - 1];
				meth.setMethodSemantics(new MethodSemantics(sem, prop));
				if(sem == MethodSemantics.Getter)
				{
					prop.setGetter(meth);
				}
				else if(sem == MethodSemantics.Setter)
				{
					prop.setSetter(meth);
				}
			}
		}
	}

	private void setDefaultValues()
	{
		GenericTableValue[] row = myTableValues.getAndDrop(TableConstants.Constant);
		if(row == null)
		{
			return;
		}

		for(GenericTableValue aRow : row)
		{
			byte[] blob = aRow.getBlob("Value");
			long coded = aRow.getCodedIndex("Parent");
			long token[] = tc.parseCodedIndex(coded, TableConstants.HasConst);
			if(token[0] == TableConstants.Field)
			{
				fields[(int) getField(token[1]) - 1].setDefaultValue(blob);
			}
			else if(token[0] == TableConstants.Param)
			{
				params[(int) getParam(token[1]) - 1].setDefaultValue(blob);
			}
			else if(token[0] == TableConstants.Property)
			{
				properties[(int) getProperty(token[1]) - 1].setDefaultValue(blob);
			}
		}
	}

	private void buildMemberRefs()
	{
		GenericTableValue[] row = myTableValues.getAndDrop(TableConstants.MemberRef);
		if(row == null)
		{
			return;
		}

		memberRefs = new MemberRef[row.length];
		for(int i = 0; i < row.length; i++)
		{
			byte[] blob = row[i].getBlob("Signature");
			if((blob[0] & 0x0F) == CallingConvention.FIELD)
			{
				// FIELDREF
				long coded = row[i].getCodedIndex("Class");
				long newtok[] = tc.parseCodedIndex(coded, TableConstants.MemberRefParent);
				String name = row[i].getString("Name");
				FieldSignature sig = FieldSignature.parse(new ByteBuffer(blob),
						group);

				if(newtok[0] == TableConstants.TypeRef)
				{
					memberRefs[i] = new FieldRef(name, sig, typeRefs[(int) newtok[1] - 1]);
				}
				else if(newtok[0] == TableConstants.ModuleRef)
				{
					memberRefs[i] = new GlobalFieldRef(moduleRefs[(int) newtok[1] - 1], name, sig);
				}
				else if(newtok[0] == TableConstants.TypeSpec)
				{
					memberRefs[i] = new FieldRef(name, sig, typeSpecs[(int) newtok[1] - 1]);
				}
				else if(newtok[0] == TableConstants.TypeDef)
				{
					memberRefs[i] = new FieldRef(name, sig, typeDefs[(int) newtok[1] - 1]);
				}
			}
			else
			{
				// METHODREF
				long coded = row[i].getCodedIndex("Class");
				long newtok[] = tc.parseCodedIndex(coded, TableConstants.MemberRefParent);
				String name = row[i].getString("Name");

				MethodSignature callsig = MethodSignature.parse(new ByteBuffer
						(blob), group);

				if(newtok[0] == TableConstants.TypeRef)
				{
					memberRefs[i] = new MethodRef(name, typeRefs[(int) newtok[1] - 1], callsig);
				}
				else if(newtok[0] == TableConstants.ModuleRef)
				{
					memberRefs[i] = new GlobalMethodRef(moduleRefs[(int) newtok[1] - 1], name, callsig);
				}
				else if(newtok[0] == TableConstants.Method)
				{
					memberRefs[i] = new VarargsMethodRef(methods[(int) getMethod(newtok[1]) - 1], callsig);
				}
				else if(newtok[0] == TableConstants.TypeSpec)
				{
					memberRefs[i] = new MethodRef(name, typeSpecs[(int) newtok[1] - 1], callsig);
				}
				else if(newtok[0] == TableConstants.TypeDef)
				{
					memberRefs[i] = new MethodRef(name, typeDefs[(int) newtok[1] - 1], callsig);
				}
			}
		}
	}

	private void setMethodMaps()
	{
		GenericTableValue[] row = myTableValues.getAndDrop(TableConstants.MethodImpl);
		if(row == null)
		{
			return;
		}

		for(GenericTableValue aRow : row)
		{
			long typedef = aRow.getTableIndex("Class");
			long coded = aRow.getCodedIndex("MethodDeclaration");
			long decltoken[] = tc.parseCodedIndex(coded, TableConstants.MethodDefOrRef);
			coded = aRow.getCodedIndex("MethodBody");
			long bodytoken[] = tc.parseCodedIndex(coded, TableConstants.MethodDefOrRef);

			MethodDefOrRef body = null, decl = null;

			if(bodytoken[0] == TableConstants.Method)
			{
				// Method
				body = methods[(int) getMethod(bodytoken[1]) - 1];
			}
			else
			{
				// MemberRef
				body = (MethodDefOrRef) memberRefs[(int) bodytoken[1] - 1];
			}

			if(decltoken[0] == TableConstants.Method)
			{
				// Method
				decl = methods[(int) getMethod(decltoken[1]) - 1];
			}
			else
			{
				// MemberRef
				decl = (MethodDefOrRef) memberRefs[(int) decltoken[1] - 1];
			}

			MethodMap map = new MethodMap(decl, body);
			TypeDef def = typeDefs[(int) typedef - 1];
			def.addMethodMap(map);
		}
	}

	private void buildStandAloneSigs()
	{
		GenericTableValue[] row = myTableValues.getAndDrop(TableConstants.StandAloneSig);
		if(row == null)
		{
			return;
		}

		standAloneSigs = new StandAloneSignature[row.length];
		for(int i = 0; i < row.length; i++)
		{
			byte[] blob = row[i].getBlob("Signature");
			if((blob[0] & 0x0F) == CallingConvention.LOCAL_SIG)
			{
				// LocalVarList
				standAloneSigs[i] = LocalVarList.parse(new ByteBuffer(blob), group);
			}
			else if((blob[0] & 0x0F) == CallingConvention.FIELD)
			{
				// field
				standAloneSigs[i] = FieldSignature.parse(new ByteBuffer(blob), group);
			}
			else
			{
				// MethodSignature
				standAloneSigs[i] = MethodSignature.parse(new ByteBuffer(blob), group);
			}
		}
	}

	private void buildGenericParams()
	{
		GenericTableValue[] table = myTableValues.getAndDrop(TableConstants.GenericParam);
		if(table == null)
		{
			return;
		}

		myGenericParams = new GenericParamDef[table.length];

		int i = 0;
		for(GenericTableValue genericTableValue : table)
		{
			String name = genericTableValue.getString("Name");

			int flags = genericTableValue.getConstant("Flags").intValue();
			long owner = genericTableValue.getTableIndex("Parent");

			GenericParamOwner paramOwner = null;
			long[] token = tc.parseCodedIndex(owner, TableConstants.TypeOrMethodDef);
			if(token[0] == TableConstants.TypeDef)
			{
				paramOwner = getByLongIndex(typeDefs, token[1]);
			}
			else
			{
				paramOwner = getByLongIndex(methods, token[1]);
			}

			GenericParamDef paramDef = new GenericParamDef(name, flags);
			paramOwner.addGenericParam(paramDef);

			myGenericParams[i++] = paramDef;
		}
	}

	private void buildGenericParamConstraints()
	{
		GenericTableValue[] table = myTableValues.getAndDrop(TableConstants.GenericParamConstraint);
		if(table == null)
		{
			return;
		}

		myGenericParamConstraints = new GenericParamConstraintDef[table.length];

		int i = 0;
		for(GenericTableValue genericTableValue : table)
		{
			long parent = genericTableValue.getTableIndex("Parent");
			long constraint = genericTableValue.getCodedIndex("Constraint");

			long[] values = tc.parseCodedIndex(constraint, TableConstants.TypeDefOrRefOrSpec);

			GenericParamDef paramDef = getByLongIndex(myGenericParams, parent);
			assert paramDef != null : parent;

			AbstractTypeReference typeReference = null;
			if(values[0] == TableConstants.TypeDef)
			{
				typeReference = getByLongIndex(typeDefs, values[1]);
			}
			else if(values[0] == TableConstants.TypeRef)
			{
				typeReference = getByLongIndex(typeRefs, values[1]);
			}
			else if(values[0] == TableConstants.TypeSpec)
			{
				typeReference = getByLongIndex(typeSpecs, values[1]);
			}

			if(typeReference == null)
			{
				i ++;
				LOG.warn("Unknown type for generuc param constraint " + Long.toHexString(values[0]));
				continue;
			}

			myGenericParamConstraints[i++] = new GenericParamConstraintDef(typeReference);
		}
	}

	private void setCustomAttributes()
	{
		GenericTableValue[] row = myTableValues.getAndDrop(TableConstants.CustomAttribute);
		if(row == null)
		{
			return;
		}

		for(GenericTableValue aRow : row)
		{
			byte[] blob = aRow.getBlob("Value");
			long coded = aRow.getCodedIndex("Type");
			long[] token = tc.parseCodedIndex(coded, TableConstants.CustomAttributeType);

			CustomAttribute ca = null;

			if(token[0] == TableConstants.Method)
			{
				ca = new CustomAttribute(blob, methods[(int) getMethod(token[1]) - 1]);
			}
			else if(token[0] == TableConstants.MemberRef)
			{
				ca = new CustomAttribute(blob, (MethodDefOrRef) memberRefs[(int) token[1] - 1]);
			}

			assert ca != null;

			coded = aRow.getCodedIndex("Parent");
			token = tc.parseCodedIndex(coded, TableConstants.HasCustomAttribute);

			if(token[0] == TableConstants.Method)
			{
				methods[(int) getMethod(token[1]) - 1].addCustomAttribute(ca);
			}
			else if(token[0] == TableConstants.Field)
			{
				fields[(int) getField(token[1]) - 1].addCustomAttribute(ca);
			}
			else if(token[0] == TableConstants.TypeRef)
			{
				typeRefs[(int) token[1] - 1].addCustomAttribute(ca);
			}
			else if(token[0] == TableConstants.TypeDef)
			{
				typeDefs[(int) token[1] - 1].addCustomAttribute(ca);
			}
			else if(token[0] == TableConstants.Param)
			{
				params[(int) getParam(token[1]) - 1].addCustomAttribute(ca);
			}
			else if(token[0] == TableConstants.InterfaceImpl)
			{
				interfaceImpls[(int) token[1] - 1].addCustomAttribute(ca);
			}
			else if(token[0] == TableConstants.MemberRef)
			{
				memberRefs[(int) token[1] - 1].addCustomAttribute(ca);
			}
			else if(token[0] == TableConstants.Module)
			{
				addCustomAttribute(ca);
			}
			else if(token[0] == TableConstants.DeclSecurity)
			{
				declSecurities[(int) token[1] - 1].addCustomAttribute(ca);
			}
			else if(token[0] == TableConstants.Property)
			{
				properties[(int) getProperty(token[1]) - 1].addCustomAttribute(ca);
			}
			else if(token[0] == TableConstants.Event)
			{
				events[(int) getEvent(token[1]) - 1].addCustomAttribute(ca);
			}
			else if(token[0] == TableConstants.StandAloneSig)
			{
				standAloneSigs[(int) token[1] - 1].addCustomAttribute(ca);
			}
			else if(token[0] == TableConstants.ModuleRef)
			{
				moduleRefs[(int) token[1] - 1].addCustomAttribute(ca);
			}
			else if(token[0] == TableConstants.TypeSpec)
			{
				typeSpecs[(int) token[1] - 1].addCustomAttribute(ca);
			}
			else if(token[0] == TableConstants.Assembly)
			{
				assemblyInfo.addCustomAttribute(ca);
			}
			else if(token[0] == TableConstants.AssemblyRef)
			{
				assemblyRefs[(int) token[1] - 1].addCustomAttribute(ca);
			}
			else if(token[0] == TableConstants.File)
			{
				fileReferences[(int) token[1] - 1].addCustomAttribute(ca);
			}
			else if(token[0] == TableConstants.ExportedType)
			{
				exportedTypes[(int) token[1] - 1].addCustomAttribute(ca);
			}
			else if(token[0] == TableConstants.ManifestResource)
			{
				mresources[(int) token[1] - 1].addCustomAttribute(ca);
			}
			else if(token[0] == TableConstants.GenericParam)
			{
				getByLongIndex(myGenericParams, token[1]).addCustomAttribute(ca);
			}
			else if(token[0] == TableConstants.GenericParamConstraint)
			{
				getByLongIndex(myGenericParamConstraints, token[1]).addCustomAttribute(ca);
			}
			else
			{
				LOG.warn("Unsupported parent type: " + Long.toHexString(token[0]));
			}
		}
	}

	private static <T> T getByLongIndex(T[] array, long index)
	{
		return array[(int) (index - 1)];
	}

	public AssemblyInfo getAssemblyInfo()
	{
		return assemblyInfo;
	}

	public TypeDef[] getTypeDefs()
	{
		return typeDefs;
	}

	public GenericParamDef[] getGenericParams()
	{
		return myGenericParams;
	}
}
