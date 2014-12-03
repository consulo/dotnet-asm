package dontnet.asm.test;

import java.io.File;

import edu.arizona.cs.mbel.mbel.ModuleParser;

/**
 * @author VISTALL
 * @since 10.12.13.
 */
public class Test
{
	public static void main(String[] args) throws Exception
	{
		long time = System.currentTimeMillis();

		ModuleParser moduleParser = new ModuleParser(new File("C:\\Windows\\Microsoft.NET\\Framework\\v4.0.30319\\mscorlib.dll"));
		moduleParser.parseNext();

		long diff = System.currentTimeMillis() - time;

		System.out.println("Time = " + diff);
	}
}
