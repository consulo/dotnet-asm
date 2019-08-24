package consulo.internal.dotnet.asm.metadata.genericTable.entryReader;

import java.io.IOException;

import javax.annotation.Nullable;
import consulo.internal.dotnet.asm.io.MSILInputStream;
import consulo.internal.dotnet.asm.metadata.TableConstants;

/**
 * @author VISTALL
 * @since 09.05.14
 */
public interface EntryReader
{
	@Nullable
	Object read(MSILInputStream in, TableConstants tc) throws IOException;
}
