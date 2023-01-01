package com.github.action.enums;

import com.github.action.BaseAction;
import com.github.action.common.ImportWriteCommandAction;
import com.github.action.common.MethodWriteCommandAction;
import com.github.constants.PluginConstants;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiType;
import com.intellij.psi.impl.source.PsiFieldImpl;

import java.util.ArrayList;
import java.util.List;

public class AutoGenEnumInfo extends AnAction implements BaseAction {

    /**
     * 自动导入包 java.util.Objects;
     */
    public static final String AUTO_IMPORT_PACKAGE_JAVA_UTIL_OBJECTS = "java.util.Objects";

    /**
     * 一期先取第一个字段为code,第二个字段为desc,后续可以改为可选字段
     *
     * @param event
     */
    @Override
    public void actionPerformed(AnActionEvent event) {
        PsiClass psiClass = this.getCurrentClass(event);
        if (psiClass == null) {
            return;
        }
        if (!psiClass.isEnum()) {
            Messages.showWarningDialog("Please Focus On A Enum File", PluginConstants.PLUGIN_NAME);
            return;
        }

        PsiField[] psiFields = psiClass.getFields();
        if (psiFields == null || psiFields.length == 0) {
            Messages.showWarningDialog("Cannot Find Field From Current Enum File", PluginConstants.PLUGIN_NAME);
            return;
        }
        if (psiFields.length < 2) {
            Messages.showWarningDialog("Least Two Field To Convert", PluginConstants.PLUGIN_NAME);
            return;
        }
        //当前工程
        Project project = event.getProject();
        //文件创建工厂
        PsiElementFactory psiElementFactory = PsiElementFactory.getInstance(project);
        List<PsiField> twoFirstPsiFieldList = this.getTwoFirstPsiField(psiFields);
        if (twoFirstPsiFieldList.size() < 2) {
            Messages.showWarningDialog("Least Two Field To Convert", PluginConstants.PLUGIN_NAME);
            return;
        }
        //编码字段
        PsiField codeField = twoFirstPsiFieldList.get(0);
        //描述字段字段
        PsiField descFiled = twoFirstPsiFieldList.get(1);
        //生成根据编码查找当前枚举
        this.genFindEnumByCode(codeField, psiClass, psiElementFactory,project);
        //生成编码与描述互转
        this.genConvertCodeToDesc(codeField, descFiled, psiClass, psiElementFactory,project);
        //自动导入包
        ImportWriteCommandAction importWriteCommandAction = new ImportWriteCommandAction(project,psiClass,AUTO_IMPORT_PACKAGE_JAVA_UTIL_OBJECTS);
        importWriteCommandAction.execute();
    }

    /**
     * 获取前两个枚举属性字段
     *
     * @param psiFields
     * @return
     */
    private List<PsiField> getTwoFirstPsiField(PsiField[] psiFields) {
        List<PsiField> psiFieldList = new ArrayList<PsiField>(2);
        for (int i = 0; i < psiFields.length; i++) {
            PsiField psiField = psiFields[i];
            if (psiField instanceof PsiFieldImpl) {
                psiFieldList.add(psiField);
                if (psiFieldList.size() >= 2) {
                    break;
                }
                continue;
            }
        }
        return psiFieldList;
    }

    /**
     * 生成根据编码查找当前枚举
     *
     * @param codeField
     * @param psiClass
     * @param psiElementFactory
     */
    private void genFindEnumByCode(PsiField codeField, PsiClass psiClass, PsiElementFactory psiElementFactory, Project project) {
        //当前编码类型
        PsiType psiType = codeField.getType();
        //当前编码名称
        String codeFieldName = codeField.getName();
        //当前枚举类的名称
        String psiClassName = psiClass.getName();
        //当前枚举变量名称
        String psiVariableName = psiClassName.substring(0,1).toLowerCase() + psiClassName.substring(1);
        //生成方法模板
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("public static ").append(psiClassName);
        stringBuilder.append(" findBy").append(codeFieldName.substring(0,1).toUpperCase()).append(codeFieldName.substring(1));
        stringBuilder.append("(").append(psiType.getPresentableText()).append(" ").append(codeFieldName).append(")");
        stringBuilder.append(" {").append("\n");
        stringBuilder.append("  for (").append(psiClassName).append(" ").append(psiVariableName)
                .append(" : values()) {").append("\n");
        stringBuilder.append("      if (Objects.equals(").append(psiVariableName).append(".get").append(codeFieldName.substring(0,1).toUpperCase()).append(codeFieldName.substring(1)).append("(),").append(codeFieldName).append(")) {").append("\n");
        stringBuilder.append("          return ").append(psiVariableName).append(";").append("\n");
        stringBuilder.append("      }");
        stringBuilder.append("  }").append("\n");
        stringBuilder.append("  return null;");
        stringBuilder.append("}");
        MethodWriteCommandAction methodWriteCommandAction = new MethodWriteCommandAction(psiClass,stringBuilder.toString(),psiElementFactory,project);
        methodWriteCommandAction.execute();
    }

    /**
     * 生成编码与描述互转方法
     *
     * @param codeField
     * @param descFiled
     * @param psiClass
     * @param psiElementFactory
     * @param project
     */
    private void genConvertCodeToDesc(PsiField codeField, PsiField descFiled, PsiClass psiClass, PsiElementFactory psiElementFactory,Project project) {
        //当前编码类型
        PsiType codeFieldType = codeField.getType();
        //当前编码名称
        String codeFieldName = codeField.getName();
        //当前枚举类的名称
        String psiClassName = psiClass.getName();
        //当前枚举变量名称
        String psiVariableName = psiClassName.substring(0,1).toLowerCase() + psiClassName.substring(1);
        //当前描述类型
        PsiType descFiledType = descFiled.getType();
        //当前描述名称
        String descFiledName = descFiled.getName();
        //生成方法模板
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("public static ").append(descFiledType.getPresentableText());
        stringBuilder.append(" convert").append(codeFieldName.substring(0,1).toUpperCase()).append(codeFieldName.substring(1)).append("To").append(descFiledName.substring(0,1).toUpperCase()).append(descFiledName.substring(1));
        stringBuilder.append("(").append(codeFieldType.getPresentableText()).append(" ").append(codeFieldName).append(")");
        stringBuilder.append(" {").append("\n");
        stringBuilder.append("  for (").append(psiClassName).append(" ").append(psiVariableName)
                .append(" : values()) {").append("\n");
        stringBuilder.append("      if (Objects.equals(").append(psiVariableName).append(".get").append(codeFieldName.substring(0,1).toUpperCase()).append(codeFieldName.substring(1)).append("(),").append(codeFieldName).append(")) {").append("\n");
        stringBuilder.append("          return ").append(psiVariableName).append(".get").append(descFiledName.substring(0,1).toUpperCase()).append(descFiledName.substring(1)).append("()").append(";").append("\n");
        stringBuilder.append("      }");
        stringBuilder.append("  }").append("\n");
        stringBuilder.append("  return null;");
        stringBuilder.append("}");
        MethodWriteCommandAction methodWriteCommandAction = new MethodWriteCommandAction(psiClass,stringBuilder.toString(),psiElementFactory,project);
        methodWriteCommandAction.execute();
    }
}
