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

import java.util.List;

import consulo.annotations.Immutable;
import javax.annotation.Nonnull;
import consulo.internal.dotnet.asm.mbel.CustomAttribute;

/**
 * @author VISTALL
 * @since 09.01.14
 */
public interface CustomAttributeOwner
{
	void addCustomAttribute(@Nonnull CustomAttribute ca);

	@Immutable
	@Nonnull
	List<CustomAttribute> getCustomAttributes();

	void removeCustomAttribute(@Nonnull CustomAttribute ca);
}
