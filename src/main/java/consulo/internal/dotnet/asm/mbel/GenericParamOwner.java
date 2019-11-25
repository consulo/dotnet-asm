package consulo.internal.dotnet.asm.mbel;

import java.util.List;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 23.11.13.
 */
public interface GenericParamOwner
{
	void addGenericParam(GenericParamDef genericParamDef);

	@Nonnull
	List<GenericParamDef> getGenericParams();
}
