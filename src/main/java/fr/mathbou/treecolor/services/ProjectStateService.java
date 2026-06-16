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

package fr.mathbou.treecolor.services;

import com.intellij.configurationStore.StateStorageManagerKt;
import com.intellij.ide.ui.LafManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.components.*;
import com.intellij.openapi.components.State;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.JDOMUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.project.ProjectKt;
import com.intellij.ui.ColorUtil;
import com.intellij.util.xmlb.XmlSerializer;
import com.intellij.util.xmlb.XmlSerializerUtil;

import fr.mathbou.treecolor.state.DefaultSettings;
import fr.mathbou.treecolor.state.ProjectColors;
import fr.mathbou.treecolor.state.ProjectFiles;
import fr.mathbou.treecolor.state.beans.ColorSettings;
import fr.mathbou.treecolor.state.beans.HighlightedFile;
import fr.mathbou.treecolor.state.beans.MarkType;
import fr.mathbou.treecolor.state.beans.ProjectState;
import fr.mathbou.treecolor.utils.ColorVariantUtil;
import fr.mathbou.treecolor.utils.UIUtils;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.*;
import java.util.List;


@State(name = "ProjectTreeColorEnhanced", storages = {@Storage("highlightedFiles.xml")})
public class ProjectStateService implements PersistentStateComponent<ProjectState> {

    private static final Logger LOG = Logger.getInstance(ProjectStateService.class);
    private static final String CURRENT_COMPONENT_NAME = "ProjectTreeColorEnhanced";
    private static final String LEGACY_COMPONENT_NAME = "ProjectTreeColorHighlighter";

    private final AppStateService appStateService = AppStateService.getInstance();
    private final Project project;

    private final ProjectState state = new ProjectState();

    public ProjectColors colors = new ProjectColors(this);
    public ProjectFiles files = new ProjectFiles(this);

    private ProjectStateService(@NotNull Project project) {
        this.project = project;
        state.colorSettingsList = DefaultSettings.getColorSettingsList();
    }

    @Nullable
    public static ProjectStateService getInstance(@Nullable Project project) {
        if (project == null) return null;
        return project.getService(ProjectStateService.class);
    }

    @Nullable
    public static ProjectStateService getInstance(@Nullable AnActionEvent actionEvent) {
        if (actionEvent == null) return null;
        return getInstance(actionEvent.getProject());
    }

    @Override
    @NotNull
    public ProjectState getState() {
        return state;
    }

    @Override
    public void loadState(@NotNull ProjectState state) {
        XmlSerializerUtil.copyBean(state, this.state);
        colors.reassignColorIdsIfNeeded();
        UIUtils.updateUI(project);
    }

    @Nullable
    public Element getStateSnapshot() {
        try {
            return XmlSerializer.serialize(state);
        } catch (Exception ignored) {
            return null;
        }
    }

    public void restoreStateFromSnapshot(@Nullable Element snapshot) {
        try {
            assert snapshot != null;
            ProjectState restoredState = XmlSerializer.deserialize(snapshot, ProjectState.class);
            loadState(restoredState);
        } catch (Exception ignored) {
        }
    }

    public void resetToDefaults() {
        state.colorSettingsList = DefaultSettings.getColorSettingsList();
        state.marksForCollapsedHighlights = DefaultSettings.getMarksForCollapsedHighlights();
    }

    public Project getProject() {
        return project;
    }

    /**
     * Saves state for all project-related persistent-state-components
     * Used for keeping our state file always-up-to-date
     */
    public void saveState() {
        try {
            Method saveComponentManager = StateStorageManagerKt.class
                .getMethod("saveComponentManager", ComponentManager.class, boolean.class);
            saveComponentManager.invoke(null, project, true);
        } catch (Exception ignored) {
            LOG.debug("Can't force save state in older versions prior to 191.4212.41");
        }
    }

    public MarkType getMarkType() {
        return MarkType.valueOfLabel(state.marksForCollapsedHighlights);
    }

    public void setMarkType(@NotNull MarkType markType) {
        state.marksForCollapsedHighlights = markType.toString();
    }

    public void runLegacyMigrationNow() {
        Element root = getStateRoot(project);
        if (root == null) {
            return;
        }

        Element legacyComponent = getComponentByName(root, LEGACY_COMPONENT_NAME);
        Element legacyColorComponent = getComponentWithLegacyColorFormat(root);

        List<ColorSettings> migratedColorSettings = migrateLegacyColors(
            legacyColorComponent != null ? legacyColorComponent.getChild("colors") : null
        );
        List<HighlightedFile> migratedFiles = migrateLegacyFiles(
            legacyComponent != null ? legacyComponent.getChild("files") : null
        );

        boolean didMigrateColors = !migratedColorSettings.isEmpty() && (isUsingDefaultColorSettings() || hasMissingThemeVariants());
        boolean didMigrateFiles = mergeLegacyFiles(migratedFiles);

        if (!didMigrateColors && !didMigrateFiles) {
            return;
        }

        if (didMigrateColors) {
            state.colorSettingsList = migratedColorSettings;
        }

        saveState();
        UIUtils.updateUI(project);
        String sourceComponentName = legacyColorComponent != null ? legacyColorComponent.getAttributeValue("name") : LEGACY_COMPONENT_NAME;
        LOG.info("Migrated legacy state from component '" + sourceComponentName + "' for project " + project.getName());
    }

    private boolean mergeLegacyFiles(@NotNull List<HighlightedFile> legacyFiles) {
        if (legacyFiles.isEmpty()) {
            return false;
        }

        List<HighlightedFile> currentFiles = state.highlightedFileList != null
            ? state.highlightedFileList
            : new ArrayList<>();

        Map<String, HighlightedFile> merged = new LinkedHashMap<>();
        for (HighlightedFile current : currentFiles) {
            merged.put(current.getPath(), current);
        }
        for (HighlightedFile legacy : legacyFiles) {
            merged.put(legacy.getPath(), legacy);
        }

        if (merged.size() == currentFiles.size()) {
            return false;
        }

        state.highlightedFileList = new ArrayList<>(merged.values());
        return true;
    }

    private boolean isUsingDefaultColorSettings() {
        List<ColorSettings> currentColors = state.colorSettingsList;
        List<ColorSettings> defaultColors = DefaultSettings.getColorSettingsList();
        if (currentColors == null || currentColors.size() != defaultColors.size()) {
            return false;
        }

        Map<Integer, ColorSettings> currentById = new HashMap<>();
        for (ColorSettings item : currentColors) {
            currentById.put(item.getId(), item);
        }

        for (ColorSettings defaultColor : defaultColors) {
            ColorSettings current = currentById.get(defaultColor.getId());
            if (current == null) {
                return false;
            }
            if (!Objects.equals(current.getLightColor(), defaultColor.getLightColor())) {
                return false;
            }
            if (!Objects.equals(current.getDarkColor(), defaultColor.getDarkColor())) {
                return false;
            }
            if (!Objects.equals(current.getName(), defaultColor.getName())) {
                return false;
            }
            if (current.isEnabled() != defaultColor.isEnabled()) {
                return false;
            }
        }
        return true;
    }

    @Nullable
    private Element getStateRoot(@NotNull Project currentProject) {
        VirtualFile projectFile = currentProject.getProjectFile();
        if (projectFile == null) {
            return null;
        }

        VirtualFile configFile = ProjectKt.isDirectoryBased(currentProject)
            ? projectFile.getParent().findChild("highlightedFiles.xml")
            : projectFile;
        if (configFile == null || !configFile.exists()) {
            return null;
        }

        try (InputStream inputStream = configFile.getInputStream()) {
            return JDOMUtil.load(inputStream);
        } catch (Exception ignored) {
            LOG.debug("Unable to read legacy state component");
        }

        return null;
    }

    @Nullable
    private Element getComponentByName(@NotNull Element root, @NotNull String name) {
        for (Element component : root.getChildren("component")) {
            if (name.equals(component.getAttributeValue("name"))) {
                return component;
            }
        }
        return null;
    }

    @Nullable
    private Element getComponentWithLegacyColorFormat(@NotNull Element root) {
        Element currentComponent = getComponentByName(root, CURRENT_COMPONENT_NAME);
        if (hasLegacyColorFormat(currentComponent)) {
            return currentComponent;
        }

        Element legacyComponent = getComponentByName(root, LEGACY_COMPONENT_NAME);
        if (hasLegacyColorFormat(legacyComponent)) {
            return legacyComponent;
        }

        return null;
    }

    private boolean hasLegacyColorFormat(@Nullable Element component) {
        if (component == null) {
            return false;
        }
        Element colors = component.getChild("colors");
        if (colors == null) {
            return false;
        }
        for (Element node : colors.getChildren("color")) {
            String legacyValue = node.getAttributeValue("value");
            if (legacyValue != null && !legacyValue.isBlank()) {
                return true;
            }
        }
        return false;
    }

    private boolean hasMissingThemeVariants() {
        if (state.colorSettingsList == null || state.colorSettingsList.isEmpty()) {
            return false;
        }
        for (ColorSettings colorSettings : state.colorSettingsList) {
            if (colorSettings.getLightColor() == null || colorSettings.getDarkColor() == null) {
                return true;
            }
        }
        return false;
    }

    @NotNull
    private List<HighlightedFile> migrateLegacyFiles(@Nullable Element legacyFiles) {
        if (legacyFiles == null) {
            return Collections.emptyList();
        }

        List<HighlightedFile> migratedFiles = new ArrayList<>();
        for (Element node : legacyFiles.getChildren("node")) {
            String path = resolveLegacyPath(node.getAttributeValue("path"));
            Integer colorId = parseInt(node.getAttributeValue("color"));
            if (path == null || path.isBlank() || colorId == null) {
                continue;
            }
            migratedFiles.add(new HighlightedFile(path, colorId));
        }
        return migratedFiles;
    }

    @NotNull
    private List<ColorSettings> migrateLegacyColors(@Nullable Element legacyColors) {
        if (legacyColors == null) {
            return Collections.emptyList();
        }

        List<ColorSettings> migratedColors = new ArrayList<>();
        Map<Integer, ColorSettings> defaultsById = new HashMap<>();
        for (ColorSettings defaultColor : DefaultSettings.getColorSettingsList()) {
            defaultsById.put(defaultColor.getId(), defaultColor);
        }

        boolean isDarkTheme = LafManager.getInstance().getCurrentUIThemeLookAndFeel().isDark();
        for (Element node : legacyColors.getChildren("color")) {
            Integer colorId = parseInt(node.getAttributeValue("id"));
            Color legacyColor = ColorUtil.fromHex(node.getAttributeValue("value"), null);
            if (colorId == null || legacyColor == null) {
                continue;
            }

            Color lightColor = isDarkTheme ? ColorVariantUtil.deriveLightColor(legacyColor) : legacyColor;
            Color darkColor = isDarkTheme ? legacyColor : ColorVariantUtil.deriveDarkColor(legacyColor);
            ColorSettings defaultColor = defaultsById.get(colorId);

            ColorSettings migrated = new ColorSettings();
            migrated.setId(colorId);
            migrated.setLightColor(lightColor);
            migrated.setDarkColor(darkColor);
            migrated.setName(firstNonBlank(node.getAttributeValue("name"), defaultColor != null ? defaultColor.getName() : "Color " + colorId));
            migrated.setEnabled(Boolean.parseBoolean(firstNonBlank(node.getAttributeValue("enabled"), "true")));
            migrated.setLinked(true);
            migratedColors.add(migrated);
        }

        // Keep all color slots to avoid breaking existing references from highlighted nodes.
        for (ColorSettings defaultColor : DefaultSettings.getColorSettingsList()) {
            boolean exists = migratedColors.stream().anyMatch(item -> item.getId() == defaultColor.getId());
            if (!exists) {
                migratedColors.add(new ColorSettings(defaultColor));
            }
        }

        migratedColors.sort(Comparator.comparingInt(ColorSettings::getId));
        return migratedColors;
    }

    @Nullable
    private static Integer parseInt(@Nullable String text) {
        if (text == null) {
            return null;
        }
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    @NotNull
    private static String firstNonBlank(@Nullable String primary, @NotNull String fallback) {
        return primary != null && !primary.isBlank() ? primary : fallback;
    }

    @Nullable
    private String resolveLegacyPath(@Nullable String rawPath) {
        if (rawPath == null || rawPath.isBlank()) {
            return rawPath;
        }

        String normalized = rawPath.replace('\\', '/');
        String basePath = project.getBasePath();
        if (basePath == null || basePath.isBlank()) {
            return normalized;
        }

        String normalizedBasePath = basePath.replace('\\', '/');
        return normalized
            .replace("$PROJECT_DIR$/", normalizedBasePath + "/")
            .replace("$PROJECT_DIR$", normalizedBasePath);
    }

}
