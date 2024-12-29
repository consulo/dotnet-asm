/*
 * Copyright 2013-2014 must-be.org
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

import consulo.internal.dotnet.asm.mbel.CustomAttribute;

import jakarta.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * @author VISTALL
 * @since 09.01.14
 */
public class BaseCustomAttributeOwner implements CustomAttributeOwner
{
	private List<CustomAttribute> myCustomAttributes = List.of();

	/**
	 * Adds a CustomAttribute to this MemberRef
	 */
	@Override
	public void addCustomAttribute(@Nonnull CustomAttribute ca)
	{
		if(myCustomAttributes.isEmpty())
		{
			myCustomAttributes = new ArrayList<>();
		}
		myCustomAttributes.add(ca);
	}

	/**
	 * Returns a non-null array of the CustomAttributes on this MemberRef
	 */
	@Nonnull
	@Override
	public List<CustomAttribute> getCustomAttributes()
	{
		return myCustomAttributes;
	}

	/**
	 * Removes a CustomAttribute from thie MemberRef
	 */
	@Override
	public void removeCustomAttribute(@Nonnull CustomAttribute ca)
	{
		myCustomAttributes.remove(ca);
	}
}
