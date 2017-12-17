package consulo.internal.dotnet.asm.mbel;

import java.util.List;

import consulo.annotations.Immutable;
import org.jetbrains.annotations.NotNull;

/**
 * @author VISTALL
 * @since 23.11.13.
 */
public interface GenericParamOwner
{
	void addGenericParam(GenericParamDef genericParamDef);

	@Immutable
	@NotNull
	List<GenericParamDef> getGenericParams();
}
