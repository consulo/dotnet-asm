package dontnet.asm.test;

import java.util.Deque;

import org.junit.Test;
import org.mustbe.dotnet.asm.LexerToken;
import org.mustbe.dotnet.asm.STypeSignatureParser;
import edu.arizona.cs.mbel.signature.TypeSignature;
import junit.framework.Assert;

/**
 * @author VISTALL
 * @since 01.07.2015
 */
public class STypeSignatureParserTest extends Assert
{
	@Test
	public void testIssue2()
	{
		parseAndMergeAndDoEqual("System.Collections.Generic.Mscorlib_CollectionDebugView`1",
				"System.Collections.Generic.Mscorlib_CollectionDebugView`1");
	}

	@Test
	public void testLexerTokensWithGenericsButNoGeneric()
	{
		parseAndMergeAndDoEqual("System.Collections.Generic.List`1", "System.Collections.Generic.List`1");
	}

	@Test
	public void testLexerListGenerics()
	{
		parseAndMergeAndDoEqual("System.Collections.Generic.List`1[[System.String, mscorlib, " +
				"Version=2.0.0.0, Culture=neutral, PublicKeyToken=b77a5c561934e089]], mscorlib, Version=2.0.0.0, Culture=neutral, " +
				"PublicKeyToken=b77a5c561934e089", "System.Collections.Generic.List`1<System.String>");
	}

	@Test
	public void testLexerDictionaryGenerics()
	{
		parseAndMergeAndDoEqual("System.Collections.Generic.Dictionary`2[[System.String, mscorlib, Version=2.0.0.0, Culture=neutral, " +
				"PublicKeyToken=b77a5c561934e089],[System.Int32, mscorlib, Version=2.0.0.0, Culture=neutral, PublicKeyToken=b77a5c561934e089]], " +
				"mscorlib, Version=2.0.0.0, Culture=neutral, PublicKeyToken=b77a5c561934e089", "System.Collections.Generic.Dictionary`2<System" +
				".String, System.Int32>");
	}

	@Test
	public void testLexerArray()
	{
		parseAndMergeAndDoEqual("System.String[], mscorlib, Version=2.0.0.0, Culture=neutral, PublicKeyToken=b77a5c561934e089", "System.String[]");
	}

	@Test
	public void testMultiLexerArray()
	{
		parseAndMergeAndDoEqual("System.String[,], mscorlib, Version=2.0.0.0, Culture=neutral, PublicKeyToken=b77a5c561934e089",
				"System.String[0...]");
	}

	private static void parseAndMergeAndDoEqual(String text, String eq)
	{
		text = text.replace(" ", "");

		Deque<LexerToken> lexerTokenDeque = STypeSignatureParser.buildTokens(text);
		CharSequence sequence = STypeSignatureParser.mergeToText(lexerTokenDeque);
		assertEquals(text, sequence.toString());

		TypeSignature sig = STypeSignatureParser.parse(text);
		StringBuilder builder = new StringBuilder();
		SomeMsilCode.typeToString(builder, sig, null);
		assertEquals(builder.toString(), eq);
	}
}
