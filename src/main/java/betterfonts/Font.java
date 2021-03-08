/*
 * Minecraft OpenType Font Support Mod
 *
 * Copyright (C) 2021 Podcrash Ltd
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

public interface Font
{
    /** The plain style constant */
    int PLAIN = java.awt.Font.PLAIN;
    /**
     * The bold style constant.  This can be combined with the other style
     * constants (except PLAIN) for mixed styles.
     */
    int BOLD = java.awt.Font.BOLD;
    /**
     * The italicized style constant.  This can be combined with the other
     * style constants (except PLAIN) for mixed styles.
     */
    int ITALIC = java.awt.Font.ITALIC;

    /** @return the font face name of this Font */
    String getName();

    /**
     * Returns the size of this Font.
     *
     * This is implementation dependant, as it might return either the point size (see {@link java.awt.Font#getSize()}
     * or the actual max height of the font. Therefore, it should only be used as a reference to derive fonts, not
     * for actual rendering.
     *
     * @return the size of this Font
     */
    int getSize();

    /**
     * Returns the style of this Font. The style can be PLAIN, BOLD, ITALIC, or BOLD+ITALIC.
     *
     * @return the style of this Font
     */
    int getStyle();

    /**
     * Creates a new Font object by replicating this Font object and applying a new style and size.
     *
     * @param style the style for the new Font
     * @return a new Font object.
     */
    default Font deriveFont(int style) {
        return deriveFont(style, getSize());
    }

    /**
     * Creates a new Font object by replicating this Font object and applying a new style and size.
     *
     * @param style the style for the new Font
     * @param size the size for the new Font
     * @return a new Font object.
     */
    Font deriveFont(int style, float size);
}
