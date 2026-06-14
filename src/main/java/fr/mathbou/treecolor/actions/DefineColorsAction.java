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

package fr.mathbou.treecolor.actions;

import com.intellij.openapi.util.IconLoader;
import fr.mathbou.treecolor.ui.views.ColorsDialog;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;


public class DefineColorsAction extends AnAction {

    public DefineColorsAction() {
        super(
            "Define Colors...",
            "Define highlight colors",
            IconLoader.getIcon("/icons/colors.svg", DefineColorsAction.class)
        );
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        Project project = anActionEvent.getProject();
        if (project == null) return;
        ColorsDialog dialog = new ColorsDialog(project);
        dialog.show();
    }

}
