package consulo.internal.dontnet.asm.test;

import java.io.File;

import consulo.internal.dotnet.asm.mbel.GenericParamDef;
import consulo.internal.dotnet.asm.mbel.ModuleParser;
import consulo.internal.dotnet.asm.mbel.TypeDef;
import junit.framework.TestCase;

/**
 * @author VISTALL
 * @since 23.11.13.
 */
public class DotNetAsmTest extends TestCase
{
	private ModuleParser myModuleParser;

	// simple reader test
	public void test1$$Program$exe() throws Exception
	{
		assertTrue(myModuleParser != null);
	}

	// test with class generic parameter
	public void test2$$Program$exe() throws Exception
	{
		assertTrue(myModuleParser != null);
	}

	// test with method generic parameter
	public void test3$$Program$exe() throws Exception
	{
		assertTrue(myModuleParser != null);
	}

	// test with method generic parameter and A constraint
	public void test4$$Program$exe() throws Exception
	{
		assertTrue(myModuleParser != null);
	}

	// test with method generic parameter and struct constraint
	public void test5$$Program$exe() throws Exception
	{
		assertTrue(myModuleParser != null);
	}

	public void test6$$Program$exe() throws Exception
	{
		assertTrue(myModuleParser != null);
	}

	public void test7$$NestClassLibrary$dll() throws Exception
	{
		assertTrue(myModuleParser != null);

		TypeDef[] typeDefs = myModuleParser.getTypeDefs();

		TypeDef typeDef = typeDefs[1];
		assertEquals(typeDef.getNestedClasses().size(), 1);
		assertEquals(typeDefs[2], typeDef.getNestedClasses().get(0));
	}

	public void test8$$IkvmCore$dll() throws Exception
	{
		assertTrue(myModuleParser != null);
	}

	public void test9$$FSharpCore$dll() throws Exception
	{
		assertTrue(myModuleParser != null);
		for(GenericParamDef genericParamDef : myModuleParser.getGenericParams())
		{
			if(!genericParamDef.getCustomAttributes().isEmpty())
			{
				return;
			}
		}
		throw new IllegalArgumentException("Custom attributes not founded at any generic parameter - test failed");
	}

	@Override
	protected void setUp() throws Exception
	{
		String name = getName();
		name = name.replace("$$", "/");
		name = name.replace("$", ".");
		ModuleParser moduleParser = new ModuleParser(new File("testData/" + name));
		moduleParser.parseNext();

		myModuleParser = moduleParser;
	}
}
