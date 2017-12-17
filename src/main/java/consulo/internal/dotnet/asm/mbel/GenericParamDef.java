package consulo.internal.dotnet.asm.mbel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import consulo.internal.dotnet.asm.signature.BaseCustomAttributeOwner;

/**
 * @author VISTALL
 * @since 23.11.13.
 */
public class GenericParamDef extends BaseCustomAttributeOwner
{
	private final String myName;
	private final int myFlags;
	private List<Object> myConstraints = Collections.emptyList();

	public GenericParamDef(String name, int flags)
	{
		myName = name;
		myFlags = flags;
	}

	public void addConstraint(Object typeDef)
	{
		if(myConstraints.isEmpty())
		{
			myConstraints = new ArrayList<Object>(2);   //TypeRef or TypeDef
		}
		myConstraints.add(typeDef);
	}

	@NotNull
	public List<Object> getConstraints()
	{
		return myConstraints;
	}

	public String getName()
	{
		return myName;
	}

	public int getFlags()
	{
		return myFlags;
	}
}