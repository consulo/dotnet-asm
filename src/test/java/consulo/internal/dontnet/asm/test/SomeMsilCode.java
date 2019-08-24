package consulo.internal.dontnet.asm.test;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import consulo.internal.dotnet.asm.signature.*;
import consulo.internal.dotnet.asm.mbel.TypeDef;
import consulo.internal.dotnet.asm.mbel.TypeRef;
import consulo.internal.dotnet.asm.mbel.TypeSpec;

/**
 * @author VISTALL
 * @since 01.07.2015
 */
public class SomeMsilCode implements SignatureConstants
{
	public static void typeToString(StringBuilder builder, TypeSignature signature, TypeDef typeDef)
	{
		if(signature == null)
		{
			builder.append("void");
			return;
		}
		byte type = signature.getType();
		switch(type)
		{
			case ELEMENT_TYPE_BOOLEAN:
				builder.append("bool");
				break;
			case ELEMENT_TYPE_VOID:
				builder.append("void");
				break;
			case ELEMENT_TYPE_CHAR:
				builder.append("char");
				break;
			case ELEMENT_TYPE_I1:
				builder.append("int8");
				break;
			case ELEMENT_TYPE_U1:
				builder.append("uint8");
				break;
			case ELEMENT_TYPE_I2:
				builder.append("int16");
				break;
			case ELEMENT_TYPE_U2:
				builder.append("uint16");
				break;
			case ELEMENT_TYPE_I4:
				builder.append("int32");
				break;
			case ELEMENT_TYPE_U4:
				builder.append("uint32");
				break;
			case ELEMENT_TYPE_I8:
				builder.append("int64");
				break;
			case ELEMENT_TYPE_U8:
				builder.append("uint64");
				break;
			case ELEMENT_TYPE_R4:
				builder.append("float32");
				break;
			case ELEMENT_TYPE_R8:
				builder.append("float64");
				break;
			case ELEMENT_TYPE_STRING:
				builder.append("string");
				break;
			case ELEMENT_TYPE_OBJECT:
				builder.append("object");
				break;
			case ELEMENT_TYPE_I:
				builder.append("int");
				break;
			case ELEMENT_TYPE_U:
				builder.append("uint");
				break;
			case ELEMENT_TYPE_BYREF:
				typeToString(builder, ((InnerTypeOwner) signature).getInnerType(), typeDef);
				builder.append("&");
				break;
			case ELEMENT_TYPE_TYPEDBYREF:
				builder.append("System.TypedReference");
				break;
			case ELEMENT_TYPE_PTR:
				PointerTypeSignature pointerTypeSignature = (PointerTypeSignature) signature;
				typeToString(builder, pointerTypeSignature.getPointerType(), typeDef);
				builder.append("*");
				break;
			case ELEMENT_TYPE_SZARRAY:
				SZArrayTypeSignature szArrayTypeSignature = (SZArrayTypeSignature) signature;
				typeToString(builder, szArrayTypeSignature.getElementType(), typeDef);
				builder.append("[]");
				break;
			case ELEMENT_TYPE_ARRAY:
				ArrayTypeSignature arrayTypeSignature = (ArrayTypeSignature) signature;
				typeToString(builder, arrayTypeSignature.getElementType(), typeDef);
				ArrayShapeSignature arrayShape = arrayTypeSignature.getArrayShape();
				builder.append("[");
				for(int i = 0; i < arrayShape.getRank(); i++)
				{
					if(i != 0)
					{
						builder.append(", ");
					}
					int low = safeGet(arrayShape.getLowerBounds(), i);
					builder.append(low);
					builder.append("...");
				}
				builder.append("]");
				break;
			case ELEMENT_TYPE_CLASS:
				ClassTypeSignature typeSignature = (ClassTypeSignature) signature;
				toStringFromDefRefSpec(builder, typeSignature.getClassType(), typeDef);
				break;
			case ELEMENT_TYPE_GENERIC_INST:
				TypeSignatureWithGenericParameters mainTypeSignature = (TypeSignatureWithGenericParameters) signature;
				typeToString(builder, mainTypeSignature.getSignature(), typeDef);
				if(!mainTypeSignature.getGenericArguments().isEmpty())
				{
					builder.append("<");
					for(int i = 0; i < mainTypeSignature.getGenericArguments().size(); i++)
					{
						if(i != 0)
						{
							builder.append(", ");
						}
						typeToString(builder, mainTypeSignature.getGenericArguments().get(i), typeDef);
					}
					builder.append(">");
				}
				break;
			case ELEMENT_TYPE_VAR:
				XGenericTypeSignature typeGenericTypeSignature = (XGenericTypeSignature) signature;
				if(typeDef == null)
				{
					builder.append("GENERICERROR");
					return;
				}
				builder.append("!");
				builder.append(typeDef.getGenericParams().get(typeGenericTypeSignature.getIndex()).getName());
				break;
			case ELEMENT_TYPE_MVAR:
				XGenericTypeSignature methodGenericTypeSignature = (XGenericTypeSignature) signature;

				builder.append(methodGenericTypeSignature.getIndex());
				break;
			case ELEMENT_TYPE_VALUETYPE:
				ValueTypeSignature valueTypeSignature = (ValueTypeSignature) signature;
				toStringFromDefRefSpec(builder, valueTypeSignature.getValueType(), typeDef);
				break;
			default:
				builder.append("UNK").append(Integer.toHexString(type).toUpperCase());
				break;
		}
	}

	public static void appendTypeRefFullName(@Nonnull StringBuilder builder, String namespace, @Nonnull String name)
	{
		if(namespace != null && !namespace.isEmpty())
		{
			builder.append(namespace).append(".").append(name);
		}
		else
		{
			builder.append(name);
		}
	}


	public static void toStringFromDefRefSpec(@Nonnull StringBuilder builder, @Nonnull Object o, @Nullable TypeDef typeDef)
	{
		if(o instanceof TypeDef)
		{
			TypeDef parent = ((TypeDef) o).getParent();
			if(parent != null)
			{
				toStringFromDefRefSpec(builder, parent, parent);
				builder.append("/");
				builder.append(((TypeDef) o).getName());
			}
			else
			{
				appendTypeRefFullName(builder, ((TypeRef) o).getNamespace(), ((TypeRef) o).getName());
			}
		}
		else if(o instanceof TypeRef)
		{
			appendTypeRefFullName(builder, ((TypeRef) o).getNamespace(), ((TypeRef) o).getName());
		}
		else if(o instanceof TypeSpec)
		{
			typeToString(builder, ((TypeSpec) o).getSignature(), typeDef);
		}
		else
		{
			throw new IllegalArgumentException(o.toString());
		}
	}

	public static int safeGet(@Nullable int[] array, int index)
	{
		if(array == null)
		{
			return 0;
		}
		if(index < 0 || array.length <= index)
		{
			return 0;
		}
		return array[index];
	}
}
