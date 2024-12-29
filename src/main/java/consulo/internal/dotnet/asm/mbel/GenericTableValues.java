package consulo.internal.dotnet.asm.mbel;

import consulo.internal.dotnet.asm.metadata.GenericTableValue;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 2019-10-08
 */
public class GenericTableValues
{
	private static final GenericTableValue[] ourDroppedValues = new GenericTableValue[0];

	private GenericTableValue[][] myTableValues;

	public GenericTableValues(@Nonnull GenericTableValue[][] tableValues)
	{
		myTableValues = tableValues;
	}

	@Nullable
	public GenericTableValue[] get(int index)
	{
		GenericTableValue[] tableValue = myTableValues[index];
		if(tableValue == ourDroppedValues)
		{
			throw new IllegalArgumentException("already dropped");
		}
		return tableValue;
	}

	@Nullable
	public GenericTableValue[] getAndDrop(int index)
	{
		GenericTableValue[] tableValue = get(index);
		myTableValues[index] = ourDroppedValues;
		return tableValue;
	}
}
