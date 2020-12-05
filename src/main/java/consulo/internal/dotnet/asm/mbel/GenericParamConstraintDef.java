package consulo.internal.dotnet.asm.mbel;

import consulo.internal.dotnet.asm.signature.BaseCustomAttributeOwner;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 05/12/2020
 */
public class GenericParamConstraintDef extends BaseCustomAttributeOwner
{
	/**
	 * Can be TypeDef, TypeRef, TypeSpec
	 */
	private AbstractTypeReference myType;

	public GenericParamConstraintDef(@Nonnull AbstractTypeReference type)
	{
		myType = type;
	}

	@Nonnull
	public AbstractTypeReference getType()
	{
		return myType;
	}
}
