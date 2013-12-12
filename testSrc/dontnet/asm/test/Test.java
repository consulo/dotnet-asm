package dontnet.asm.test;

import java.io.FileInputStream;

import edu.arizona.cs.mbel.mbel.Module;
import edu.arizona.cs.mbel.mbel.ModuleParser;
import edu.arizona.cs.mbel.signature.MethodAttributes;

/**
 * @author VISTALL
 * @since 10.12.13.
 */
public class Test
{
	public static void main(String[] args) throws Exception
	{
		ModuleParser moduleParser = new ModuleParser(new FileInputStream("H:\\github.com\\consulo-incubator\\consulo-dotnet\\dotnet-asm\\testData" +
				"\\test6\\Program.exe"));

		Module module = moduleParser.parseModule();
		/*ZipFile zipFile = new ZipFile("out\\artifacts\\dist\\csharp\\lib\\csharp.jar");
		Enumeration<? extends ZipEntry> entries = zipFile.entries();
		while(entries.hasMoreElements())
		{
			ZipEntry zipEntry = entries.nextElement();

			System.out.println(zipEntry.getName() + " " + zipEntry.getSize());
		}  */

		long value = 257;
		for(int i = 0; i < 32; i++)
		{
			int mask = 1 << i;
			if((value & mask) == mask)
			{
				System.out.println("mask 0x" + Integer.toHexString(mask).toUpperCase());
			}
		}
		System.out.println((value & MethodAttributes.SpecialName) == MethodAttributes.SpecialName);
	}
}