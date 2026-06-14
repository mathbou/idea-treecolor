/*
 * Copyright (c) 2018-2020 Pavel Barykin.
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

package dev.pnbarx.idea.treecolor.utils;

import org.jetbrains.annotations.Nullable;

import java.awt.*;

public final class ColorVariantUtil {

    private ColorVariantUtil() {
    }

    @Nullable
    public static Color deriveDarkColor(@Nullable Color sourceColor) {
        return sourceColor != null ? deriveColor(sourceColor, true) : null;
    }

    @Nullable
    public static Color deriveLightColor(@Nullable Color sourceColor) {
        return sourceColor != null ? deriveColor(sourceColor, false) : null;
    }

    private static Color deriveColor(@Nullable Color sourceColor, boolean targetDark) {
        if (sourceColor == null) return null;

        float[] sourceHSB = Color.RGBtoHSB(sourceColor.getRed(), sourceColor.getGreen(), sourceColor.getBlue(), null);
        float hue = sourceHSB[0];
        float saturation = sourceHSB[1];
        float brightness = sourceHSB[2];

        float targetSaturation;
        float targetBrightness;
        if (targetDark) {
            targetSaturation = Math.min(1.0f, saturation * 1.08f + 0.06f);
            targetBrightness = Math.max(0.08f, Math.min(0.40f, brightness * 0.28f));
        } else {
            targetSaturation = Math.max(0.08f, saturation * 0.82f);
            targetBrightness = Math.max(0.62f, Math.min(1.0f, 0.68f + brightness * 0.30f));
        }

        int rgb = Color.HSBtoRGB(hue, targetSaturation, targetBrightness);
        return new Color((rgb >> 16) & 0xff, (rgb >> 8) & 0xff, rgb & 0xff, sourceColor.getAlpha());
    }
}
