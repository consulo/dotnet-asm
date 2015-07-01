package org.mustbe.dotnet.asm;

/**
 * @author VISTALL
 * @since 01.07.2015
 */
public class LexerToken
{
	public static class LexerTokenType
	{
		private final String myName;

		public LexerTokenType(String name)
		{
			myName = name;
		}

		@Override
		public String toString()
		{
			return myName;
		}
	}

	public static final LexerTokenType COMMA = new LexerTokenType("COMMA");
	public static final LexerTokenType TEXT = new LexerTokenType("TEXT");
	public static final LexerTokenType LBRACKET = new LexerTokenType("LBRACKET");
	public static final LexerTokenType RBRACKET = new LexerTokenType("RBRACKET");
	public static final LexerTokenType EQ = new LexerTokenType("EQ");
	public static final LexerTokenType DOT = new LexerTokenType("DOT");
	public static final LexerTokenType PRIME = new LexerTokenType("PRIME");

	private final LexerTokenType myType;
	private String myText;

	public LexerToken(LexerTokenType type, char c)
	{
		this(type, String.valueOf(c));
	}

	public LexerToken(LexerTokenType type, String text)
	{
		myType = type;
		myText = text;
	}

	public String getText()
	{
		return myText;
	}

	public LexerTokenType getType()
	{
		return myType;
	}

	@Override
	public String toString()
	{
		return myType.toString() + ":'" + myText.replace("\n", "\\n").replace("\t", "\\t") + "'";
	}
}
