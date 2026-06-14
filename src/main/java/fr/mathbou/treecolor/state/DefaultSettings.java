/*
 * Copyright (c) 2018-2020 Pavel Barykin.
 * Copyright (c) 2026 Mathieu Bouzard.
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

package fr.mathbou.treecolor.state;

import fr.mathbou.treecolor.state.beans.ColorSettings;
import fr.mathbou.treecolor.state.beans.MarkType;
import fr.mathbou.treecolor.utils.ColorVariantUtil;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DefaultSettings {
    public static List<ColorSettings> getColorSettingsList() {
        return new ArrayList<>(Arrays.asList(
                pastel(1, 0xf4b5b5, "Color 1"),
                pastel(2, 0xf2c9a6, "Color 2"),
                pastel(3, 0xf4efac, "Color 3"),
                pastel(4, 0xbfe9c1, "Color 4"),
                pastel(5, 0xaeebf0, "Color 5"),
                pastel(6, 0xb2d7f2, "Color 6"),
                pastel(7, 0xd7b7ef, "Color 7"),
                pastel(8, 0xe4e4e4, "Color 8")
        ));
    }

    private static ColorSettings pastel(int id, int lightRgb, String name) {
        Color lightColor = new Color(lightRgb);
        return new ColorSettings(id, lightColor, ColorVariantUtil.deriveDarkColor(lightColor), name, true);
    }

    public static String getMarksForCollapsedHighlights() {
        return MarkType.DOTS.toString();
    }
}
