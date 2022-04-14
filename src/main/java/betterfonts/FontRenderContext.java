/*
 * Minecraft OpenType Font Support Mod
 *
 * Copyright (C) 2022 Podcrash Ltd
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package betterfonts;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public interface FontRenderContext
{

    boolean isGraphicsContext();

    boolean isContextCurrent();

    default boolean shouldCache()
    {
        return !isGraphicsContext() || isContextCurrent();
    }

    OglService ensureGraphicsContextCurrent();

    boolean runIfGraphicsContextCurrent(Consumer<OglService> consumer);

    <T> Optional<T> getIfGraphicsContextCurrent(Function<OglService, T> function);
}
