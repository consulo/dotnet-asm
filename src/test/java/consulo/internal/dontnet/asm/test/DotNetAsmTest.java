package consulo.internal.dontnet.asm.test;

import consulo.PathSearcher;
import consulo.internal.dotnet.asm.mbel.GenericParamDef;
import consulo.internal.dotnet.asm.mbel.ModuleParser;
import consulo.internal.dotnet.asm.mbel.TypeDef;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

/**
 * @author VISTALL
 * @since 23.11.13.
 */
public class DotNetAsmTest extends Assert
{
	@Rule
	public TestName myTestName = new TestName();

	private ModuleParser myModuleParser;

	// simple reader test
	@Test
	public void test1$$Program$exe() throws Exception
	{
		assertTrue(myModuleParser != null);
	}

	// test with class generic parameter
	@Test
	public void test2$$Program$exe() throws Exception
	{
		assertTrue(myModuleParser != null);
	}

	// test with method generic parameter
	@Test
	public void test3$$Program$exe() throws Exception
	{
		assertTrue(myModuleParser != null);
	}

	// test with method generic parameter and A constraint
	@Test
	public void test4$$Program$exe() throws Exception
	{
		assertTrue(myModuleParser != null);
	}

	// test with method generic parameter and struct constraint
	@Test
	public void test5$$Program$exe() throws Exception
	{
		assertTrue(myModuleParser != null);
	}

	@Test
	public void test6$$Program$exe() throws Exception
	{
		assertTrue(myModuleParser != null);
	}

	@Test
	public void test7$$NestClassLibrary$dll() throws Exception
	{
		assertTrue(myModuleParser != null);

		TypeDef[] typeDefs = myModuleParser.getTypeDefs();

		TypeDef typeDef = typeDefs[1];
		assertEquals(typeDef.getNestedClasses().size(), 1);
		assertEquals(typeDefs[2], typeDef.getNestedClasses().get(0));
	}

	@Test
	public void test8$$IkvmCore$dll() throws Exception
	{
		assertTrue(myModuleParser != null);
	}

	@Test
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

	@Test
	public void testIssue3$$Newtonsoft$Json$dll()
	{
		assertTrue(myModuleParser != null);
	}

	@Before
	public void before() throws Exception
	{
		String name = myTestName.getMethodName();
		name = name.replace("$$", "/");
		name = name.replace("$", ".");
		ModuleParser moduleParser = new ModuleParser(PathSearcher.getTestPath(name));

		myModuleParser = moduleParser;
	}
}
