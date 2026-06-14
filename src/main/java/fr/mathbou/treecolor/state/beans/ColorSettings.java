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

package fr.mathbou.treecolor.state.beans;

import com.intellij.ide.ui.LafManager;
import com.intellij.ui.ColorUtil;
import com.intellij.util.xmlb.Converter;
import com.intellij.util.xmlb.annotations.Attribute;
import com.intellij.util.xmlb.annotations.Property;
import com.intellij.util.xmlb.annotations.Tag;
import com.intellij.util.xmlb.annotations.Transient;
import fr.mathbou.treecolor.utils.UIUtils;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

@Tag("color")
@Property(surroundWithTag = false, alwaysWrite = true, flat = true, style = Property.Style.ATTRIBUTE)
public class ColorSettings {

    // attributes must be public for PersistentStateComponent xml serialization
    @Attribute("id")
    public int _id;

    @Attribute(value = "lightValue", converter = ColorHexConverter.class)
    public Color _lightColor;

    @Attribute(value = "darkValue", converter = ColorHexConverter.class)
    public Color _darkColor;

    @Attribute("name")
    public String _name;

    @Attribute("enabled")
    public boolean _enabled;

    @Attribute("linked")
    public boolean _linked = true;

    // an empty constructor is needed for PersistentStateComponent xml serialization
    @SuppressWarnings({"UnusedDeclaration"})
    public ColorSettings() {
    }

    public ColorSettings(int id, Color color, String name, boolean isEnabled) {
        this(id, color, color, name, isEnabled);
    }

    public ColorSettings(int id, Color lightColor, Color darkColor, String name, boolean isEnabled) {
        setId(id);
        setLightColor(lightColor);
        setDarkColor(darkColor);
        setName(name);
        setEnabled(isEnabled);
        setLinked(true);
    }

    public ColorSettings(ColorSettings colorSettings) {
        setId(colorSettings.getId());
        setLightColor(colorSettings.getLightColor());
        setDarkColor(colorSettings.getDarkColor());
        setName(colorSettings.getName());
        setEnabled(colorSettings.isEnabled());
        setLinked(colorSettings.isLinked());
    }

    @Transient
    public int getId() {
        return _id;
    }

    public void setId(int _id) {
        this._id = _id;
    }

    @Transient
    public Color getColor() {
        return getColorForCurrentTheme();
    }

    public void setColor(Color color) {
        setColorForCurrentTheme(color);
    }

    @Transient
    public Color getColorForCurrentTheme() {
        return isDarkTheme() ? getDarkColor() : getLightColor();
    }

    public void setColorForCurrentTheme(Color color) {
        if (isDarkTheme()) {
            setDarkColor(color);
        } else {
            setLightColor(color);
        }
    }

    @Transient
    public Color getLightColor() {
        return _lightColor;
    }

    public void setLightColor(Color color) {
        _lightColor = color;
    }

    @Transient
    public Color getDarkColor() {
        return _darkColor;
    }

    public void setDarkColor(Color color) {
        _darkColor = color;
    }

    @Transient
    public String getName() {
        return _name;
    }

    public void setName(String name) {
        this._name = name;
    }

    @Transient
    public boolean isEnabled() {
        return _enabled;
    }

    public void setEnabled(boolean isEnabled) {
        this._enabled = isEnabled;
    }

    @Transient
    public boolean isLinked() {
        return _linked;
    }

    public void setLinked(boolean linked) {
        this._linked = linked;
    }

    @Transient
    public boolean isSetAndEnabled() {
        return _enabled && getLightColor() != null && getDarkColor() != null;
    }

    private static boolean isDarkTheme() {
        return LafManager.getInstance().getCurrentUIThemeLookAndFeel().isDark();
    }


    public static class ColorHexConverter extends Converter<Color> {

        @Nullable
        @Override
        public Color fromString(@Nullable String colorHex) {
            return ColorUtil.fromHex(colorHex, null);
        }

        @Nullable
        @Override
        public String toString(@Nullable Color color) {
            return color != null ? UIUtils.toHtmlColorWithOptionalAlpha(color) : "";
        }
    }
}
