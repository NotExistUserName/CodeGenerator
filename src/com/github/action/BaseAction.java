package com.github.action;

import com.github.constants.PluginConstants;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.util.PsiTreeUtil;

import java.util.List;

/**
 * @Author CoffeeEngineer
 * @Description 各行为类基础类
 * @Date 2022/12/10 18:21
 */
public interface BaseAction {

    /**
     * 构建当前类类文件
     *
     * @param event
     * @return
     */
    default PsiClass getCurrentClass(AnActionEvent event) {
        //当前文件
        PsiFile psiFile = event.getData(CommonDataKeys.PSI_FILE);
        if (!(psiFile instanceof PsiJavaFile)) {
            Messages.showWarningDialog("Please Focus On A Java File", PluginConstants.PLUGIN_NAME);
            return null;
        }
        List<PsiClass> psiClassList = PsiTreeUtil.getChildrenOfTypeAsList(psiFile, PsiClass.class);
        if (psiClassList == null || psiClassList.isEmpty()) {
            Messages.showWarningDialog("Cannot Find Any Java Class", PluginConstants.PLUGIN_NAME);
            return null;
        }
        //只聚焦当前类
        return psiClassList.get(0);
    }
}
