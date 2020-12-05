package consulo.internal.dotnet.asm.mbel;

import consulo.internal.dotnet.asm.signature.BaseCustomAttributeOwner;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * @author VISTALL
 * @since 23.11.13.
 */
public class GenericParamDef extends BaseCustomAttributeOwner
{
	private final String myName;
	private final int myFlags;
	private List<GenericParamConstraintDef> myConstraints = List.of();

	public GenericParamDef(String name, int flags)
	{
		myName = name;
		myFlags = flags;
	}

	public void addConstraint(GenericParamConstraintDef genericParamConstraintDef)
	{
		if(myConstraints.isEmpty())
		{
			myConstraints = new ArrayList<>();
		}
		myConstraints.add(genericParamConstraintDef);
	}

	@Nonnull
	public List<GenericParamConstraintDef> getConstraints()
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
