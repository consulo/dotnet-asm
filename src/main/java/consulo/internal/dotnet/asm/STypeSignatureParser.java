package consulo.internal.dotnet.asm;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

import consulo.internal.dotnet.asm.mbel.AssemblyTypeRef;
import consulo.internal.dotnet.asm.signature.ArrayShapeSignature;
import consulo.internal.dotnet.asm.signature.ArrayTypeSignature;
import consulo.internal.dotnet.asm.signature.ClassTypeSignature;
import consulo.internal.dotnet.asm.signature.TypeSignature;
import consulo.internal.dotnet.asm.signature.TypeSignatureWithGenericParameters;

/**
 * @author VISTALL
 * @since 01.07.2015
 */
public class STypeSignatureParser
{
	public static TypeSignature parse(CharSequence signature)
	{
		Deque<LexerToken> lexerTokenDeque = buildTokens(signature);

		Deque<LexerToken> typeRefName = eatQualified(lexerTokenDeque);

		List<Integer> arrayInfo = new ArrayList<Integer>(2);
		List<TypeSignature> generics = Collections.emptyList();

		if(expect(lexerTokenDeque, LexerToken.PRIME))
		{
			LexerToken primeToken = lexerTokenDeque.removeFirst();

			if(expect(lexerTokenDeque, LexerToken.TEXT))
			{
				typeRefName.add(primeToken);
				typeRefName.add(lexerTokenDeque.removeFirst());
			}
		}

		if(expect(lexerTokenDeque, LexerToken.LBRACKET))
		{
			lexerTokenDeque.removeFirst();

			// generic info
			if(expect(lexerTokenDeque, LexerToken.LBRACKET))
			{
				int openCount = 0;
				List<List<LexerToken>> lists = new ArrayList<List<LexerToken>>();

				List<LexerToken> temp = new ArrayList<LexerToken>();
				while(!lexerTokenDeque.isEmpty())
				{
					LexerToken lexerToken = lexerTokenDeque.removeFirst();
					if(lexerToken.getType() == LexerToken.LBRACKET)
					{
						openCount++;
					}

					if(lexerToken.getType() == LexerToken.RBRACKET)
					{
						openCount--;
						if(openCount < 0)
						{
							break;
						}
					}

					if(openCount == 0)
					{
						if(lexerToken.getType() == LexerToken.COMMA)
						{
							continue;
						}
						else
						{
							temp.add(lexerToken);
						}

						lists.add(new ArrayList<LexerToken>(temp));
						temp = new ArrayList<LexerToken>();
					}
					else
					{
						temp.add(lexerToken);
					}
				}

				if(!temp.isEmpty())
				{
					lists.add(temp);
				}

				if(expect(lexerTokenDeque, LexerToken.RBRACKET))
				{
					lexerTokenDeque.removeFirst();
				}

				generics = new ArrayList<TypeSignature>(lists.size());
				for(List<LexerToken> list : lists)
				{
					generics.add(parse(mergeToText(list.subList(1, list.size() - 1))));
				}
			}
			else
			{
				// restore token
				lexerTokenDeque.addFirst(new LexerToken(LexerToken.LBRACKET, '['));
			}
		}

		while(expect(lexerTokenDeque, LexerToken.LBRACKET))
		{
			lexerTokenDeque.removeFirst();

			int i = 0;
			while(expect(lexerTokenDeque, LexerToken.COMMA))
			{
				lexerTokenDeque.removeFirst();
				i++;
			}

			if(expect(lexerTokenDeque, LexerToken.RBRACKET))
			{
				lexerTokenDeque.removeFirst();
				arrayInfo.add(i);
			}
			else
			{
				break;
			}
		}

		Deque<LexerToken> nameToken = new ArrayDeque<LexerToken>();

		LexerToken lexerToken;
		while((lexerToken = typeRefName.pollLast()) != null)
		{
			LexerToken.LexerTokenType type = lexerToken.getType();
			if(type == LexerToken.DOT)
			{
				break;
			}
			nameToken.addFirst(lexerToken);
		}
		AssemblyTypeRef assemblyTypeRef = new AssemblyTypeRef(null, mergeToText(typeRefName).toString(), mergeToText(nameToken).toString());

		TypeSignature sig = new ClassTypeSignature(assemblyTypeRef);
		if(!generics.isEmpty())
		{
			sig = new TypeSignatureWithGenericParameters(sig, generics);
		}

		for(Integer integer : arrayInfo)
		{
			sig = new ArrayTypeSignature(sig, new ArrayShapeSignature(integer, new int[0], new int[0]));
		}
		return sig;
	}


	private static boolean expect(Deque<LexerToken> lexerTokenDeque, LexerToken.LexerTokenType tokenType)
	{
		LexerToken lexerToken = lexerTokenDeque.peekFirst();
		return lexerToken != null && lexerToken.getType() == tokenType;
	}

	public static CharSequence mergeToText(Collection<LexerToken> tokens)
	{
		StringBuilder builder = new StringBuilder();
		for(LexerToken lexerToken : tokens)
		{
			builder.append(lexerToken.getText());
		}
		return builder;
	}


	private static Deque<LexerToken> eatQualified(Deque<LexerToken> lexerTokenDeque)
	{
		Deque<LexerToken> tokens = new ArrayDeque<LexerToken>(5);

		while(!lexerTokenDeque.isEmpty())
		{
			LexerToken lexerToken = lexerTokenDeque.peekFirst();

			if(lexerToken.getType() == LexerToken.TEXT)
			{
				tokens.add(lexerTokenDeque.removeFirst());
			}
			else
			{
				break;
			}

			lexerToken = lexerTokenDeque.peekFirst();
			if(lexerToken == null)
			{
				break;
			}

			if(lexerToken.getType() == LexerToken.DOT)
			{
				tokens.add(lexerTokenDeque.removeFirst());
			}
			else
			{
				break;
			}
		}
		return tokens;
	}

	public static Deque<LexerToken> buildTokens(CharSequence text)
	{
		Deque<LexerToken> lexerTokenDeque = new ArrayDeque<LexerToken>();

		StringBuilder compositor = null;

		for(int i = 0; i < text.length(); i++)
		{
			char c = text.charAt(i);
			if(Character.isLetterOrDigit(c) || c == '_')
			{
				if(compositor != null)
				{
					compositor.append(c);
				}
				else
				{
					compositor = new StringBuilder().append(c);
				}
			}
			else
			{
				if(compositor != null)
				{
					lexerTokenDeque.add(new LexerToken(LexerToken.TEXT, compositor.toString()));
					compositor = null;
				}

				switch(c)
				{
					case ',':
						lexerTokenDeque.add(new LexerToken(LexerToken.COMMA, c));
						break;
					case '.':
						lexerTokenDeque.add(new LexerToken(LexerToken.DOT, c));
						break;
					case '[':
						lexerTokenDeque.add(new LexerToken(LexerToken.LBRACKET, c));
						break;
					case ']':
						lexerTokenDeque.add(new LexerToken(LexerToken.RBRACKET, c));
						break;
					case '`':
						lexerTokenDeque.add(new LexerToken(LexerToken.PRIME, c));
						break;
					case '=':
						lexerTokenDeque.add(new LexerToken(LexerToken.EQ, c));
						break;
					default:
						if(Character.isSpaceChar(c))
						{
							break;
						}
						throw new IllegalArgumentException("Unknown char: " + c + " in '" + text + "'");
				}
			}
		}

		if(compositor != null)
		{
			lexerTokenDeque.add(new LexerToken(LexerToken.TEXT, compositor.toString()));
		}

		return lexerTokenDeque;
	}
}
