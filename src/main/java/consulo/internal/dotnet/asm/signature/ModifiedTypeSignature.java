/*
 * Copyright 2013-2026 consulo.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package consulo.internal.dotnet.asm.signature;

import consulo.internal.dotnet.asm.mbel.TypeGroup;
import consulo.internal.dotnet.asm.io.ByteBuffer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A type with custom modifiers in a type position - modreq(IsExternalInit) void, int32& modreq(InAttribute): the modifiers come
 * before the type they modify
 *
 * @author VISTALL
 */
public class ModifiedTypeSignature extends TypeSignature implements InnerTypeOwner
{
	private final List<CustomModifierSignature> myCustomModifiers;
	private final TypeSignature myInnerType;

	public ModifiedTypeSignature(List<CustomModifierSignature> customModifiers, TypeSignature innerType)
	{
		super(customModifiers.get(0).getType());
		myCustomModifiers = Collections.unmodifiableList(customModifiers);
		myInnerType = innerType;
	}

	/**
	 * Parses the custom modifiers at the position of the buffer, and the type after them
	 *
	 * @return null if there was a parse error
	 */
	public static ModifiedTypeSignature parse(ByteBuffer buffer, TypeGroup group)
	{
		List<CustomModifierSignature> customModifiers = new ArrayList<>(2);
		while(true)
		{
			int position = buffer.getPosition();
			CustomModifierSignature customModifier = CustomModifierSignature.parse(buffer, group);
			if(customModifier == null)
			{
				buffer.setPosition(position);
				break;
			}
			customModifiers.add(customModifier);
		}

		TypeSignature innerType = TypeSignatureParser.parse(buffer, group);
		if(customModifiers.isEmpty() || innerType == null)
		{
			return null;
		}
		return new ModifiedTypeSignature(customModifiers, innerType);
	}

	public List<CustomModifierSignature> getCustomModifiers()
	{
		return myCustomModifiers;
	}

	@Override
	public TypeSignature getInnerType()
	{
		return myInnerType;
	}
}
