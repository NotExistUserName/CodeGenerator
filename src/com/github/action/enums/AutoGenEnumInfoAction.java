package com.github.action.enums;

import com.github.action.BaseAction;
import com.github.action.common.*;
import com.github.constants.PluginConstants;
import com.github.dto.ReceiveGenEnumInfoDto;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiType;
import com.intellij.psi.impl.source.PsiFieldImpl;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBTextField;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class AutoGenEnumInfoAction extends AnAction implements BaseAction {

    /**
     * 自动导入包 java.util.Objects;
     */
    public static final String AUTO_IMPORT_PACKAGE_JAVA_UTIL_OBJECTS = "java.util.Objects";

    /**
     * 枚举字段前缀
     */
    public static final String FIELD_PREFIX = "private final String ";

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
        //当前工程
        Project project = event.getProject();
        //创建节点工厂
        PsiElementFactory psiElementFactory = PsiElementFactory.getInstance(project);
        //弹框创建工厂
        JBPopupFactory factory = JBPopupFactory.getInstance();
        //创建输入面板
        JBPanel inputPanel = new JBPanel();
        //弹框
        JBPopup jbPopup = factory.createComponentPopupBuilder(inputPanel, null)
                .setTitle("请输入枚举生成信息")
                .setMovable(true)
                .setRequestFocus(true)
                .setResizable(true)
                .setMinSize(new Dimension(600, 300))
                .createPopup();
        jbPopup.showCenteredInCurrentWindow(project);
        //枚举编码输入框
        JBLabel enumCodeMsg = new JBLabel("请输入枚举编码：");
        inputPanel.add(enumCodeMsg);
        JBTextField enumCodeInput = new JBTextField(16);
        inputPanel.add(enumCodeInput);
        //枚举解析串输入框
        JBLabel enumCodeAnalysisMsg = new JBLabel("请输入枚举解析串：");
        inputPanel.add(enumCodeAnalysisMsg);
        JBTextField enumCodeAnalysisInput = new JBTextField(15);
        inputPanel.add(enumCodeAnalysisInput);
        //确定按钮
        JButton confirmButton = new JButton("确定");
        ReceiveGenEnumInfoDto receiveGenEnumInfoDto = new ReceiveGenEnumInfoDto();
        confirmButton.addActionListener(listener -> {
            receiveGenEnumInfoDto.setEnumCode(enumCodeInput.getText());
            receiveGenEnumInfoDto.setEnumInfoString(enumCodeAnalysisInput.getText());
            jbPopup.setUiVisible(false);
            //未完整输入枚举生成信息
            if (StringUtils.isBlank(receiveGenEnumInfoDto.getEnumCode()) || StringUtils.isBlank(receiveGenEnumInfoDto.getEnumInfoString())) {
                this.genEnumFindCode(project, psiClass, psiElementFactory);
                return;
            }
            //完整输入枚举信息,解析枚举信息串
            this.analysisInputString(project, psiClass, psiElementFactory, receiveGenEnumInfoDto);
        });
        inputPanel.add(confirmButton);
        //取消按钮
        JButton cancelButton = new JButton("取消");
        cancelButton.addActionListener(listener -> {
            jbPopup.setUiVisible(false);
        });
        inputPanel.add(cancelButton);
        JBLabel analysisMsgExample = new JBLabel("解析串例子：是否资质审核;yes-是,no-否");
        analysisMsgExample.setCopyable(true);
        inputPanel.add(analysisMsgExample);
    }

    /**
     * 生成枚举查询代码
     *
     * @param project
     * @param psiClass
     * @param psiElementFactory
     */
    private void genEnumFindCode(Project project, PsiClass psiClass, PsiElementFactory psiElementFactory) {
        PsiField[] psiFields = psiClass.getFields();
        if (psiFields == null || psiFields.length == 0) {
            Messages.showWarningDialog("Cannot Find Field From Current Enum File", PluginConstants.PLUGIN_NAME);
            return;
        }

        if (psiFields.length < 2) {
            Messages.showWarningDialog("Least Two Field To Convert", PluginConstants.PLUGIN_NAME);
            return;
        }
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
        this.genFindEnumByCode(codeField, psiClass, psiElementFactory, project);
        //生成编码与描述互转
        this.genConvertCodeToDesc(codeField, descFiled, psiClass, psiElementFactory, project);
        //自动导入包
        ImportWriteCommandAction importWriteCommandAction = new ImportWriteCommandAction(project, psiClass, AUTO_IMPORT_PACKAGE_JAVA_UTIL_OBJECTS);
        importWriteCommandAction.execute();
    }

    /**
     * 解析输入的枚举信息
     *
     * @param project
     * @param psiClass
     * @param psiElementFactory
     * @param receiveGenEnumInfoDto
     */
    private void analysisInputString(Project project, PsiClass psiClass, PsiElementFactory psiElementFactory, ReceiveGenEnumInfoDto receiveGenEnumInfoDto) {
        //生成枚举
        this.genEnumConstants(project, psiClass, psiElementFactory, receiveGenEnumInfoDto);
        //生成枚举属性
        final String enumCode = FIELD_PREFIX + receiveGenEnumInfoDto.getEnumCode();
        final String desc = "desc";
        final String enumDesc = FIELD_PREFIX + desc;
        FieldWriteCommandAction enumCodeWriteCommandAction = new FieldWriteCommandAction(enumCode,psiClass,psiElementFactory,project);
        enumCodeWriteCommandAction.execute();
        FieldWriteCommandAction enumDescWriteCommandAction = new FieldWriteCommandAction(enumDesc,psiClass,psiElementFactory,project);
        enumDescWriteCommandAction.execute();
        //生成查找方法
        this.genEnumFindCode(project, psiClass, psiElementFactory);
        //生成全参构造器
        this.genAllConstructor(project,psiClass,psiElementFactory,receiveGenEnumInfoDto.getEnumCode(),desc);
        //生成getter方法
        this.genGetter(project,psiClass,psiElementFactory,receiveGenEnumInfoDto.getEnumCode());
        this.genGetter(project,psiClass,psiElementFactory,desc);
        //生成Java Doc
        PsiField[] psiFields = psiClass.getFields();
        JavaDocWriteCommandAction javaDocWriteCommandAction = new JavaDocWriteCommandAction(receiveGenEnumInfoDto.getEnumInfoString(),psiFields[0], psiClass, psiElementFactory,project);
        javaDocWriteCommandAction.execute();
    }

    /**
     * 生成getter方法
     *
     * @param project
     * @param psiClass
     * @param psiElementFactory
     * @param forWho
     */
    private void genGetter(Project project, PsiClass psiClass, PsiElementFactory psiElementFactory,String forWho) {
        StringBuilder getter = new StringBuilder();
        getter.append("public String get").append(forWho.substring(0,1).toUpperCase()).append(forWho.substring(1)).append(" () {")
                .append("   return this.").append(forWho).append(";")
                .append("}");
        MethodWriteCommandAction methodWriteCommandAction = new MethodWriteCommandAction(psiClass,getter.toString(),psiElementFactory,project);
        methodWriteCommandAction.execute();
    }

    /**
     * 生成全参构造器
     *
     * @param project
     * @param psiClass
     * @param psiElementFactory
     * @param enumCode
     * @param enumDesc
     */
    private void genAllConstructor(Project project, PsiClass psiClass, PsiElementFactory psiElementFactory,String enumCode,String enumDesc) {
        StringBuilder allConstructor = new StringBuilder();
        allConstructor.append(psiClass.getName()).append(" ").append("(")
                .append("String ").append(enumCode).append(", ").append("String ").append(enumDesc).append(") {")
                .append("   this.").append(enumCode).append(" = ").append(enumCode).append(";")
                .append("   this.").append(enumDesc).append(" = ").append(enumDesc).append(";")
                .append("}");
        MethodWriteCommandAction methodWriteCommandAction = new MethodWriteCommandAction(psiClass,allConstructor.toString(),psiElementFactory,project);
        methodWriteCommandAction.execute();
    }

    /**
     * 生成枚举常量
     *
     * @param project
     * @param psiClass
     * @param psiElementFactory
     * @param receiveGenEnumInfoDto
     */
    private void genEnumConstants(Project project, PsiClass psiClass, PsiElementFactory psiElementFactory, ReceiveGenEnumInfoDto receiveGenEnumInfoDto) {
        //格式为: 是否资质审核;yes-是,no-否
        //格式化代码 CodeStyleManager.getInstance(project).reformat(psiClass);
        String enumInfoString = receiveGenEnumInfoDto.getEnumInfoString();
        String[] enumInfoArray = enumInfoString.split(";");
        if (enumInfoArray.length < 2) {
            Messages.showErrorDialog("枚举解析串格式有误", PluginConstants.PLUGIN_NAME);
            return;
        }
        String enumInfo = enumInfoArray[1];
        String[] enumConstantInfoArray = enumInfo.split(",");
        for (int i = 0; i < enumConstantInfoArray.length; i++) {
            String enumConstantInfo = enumConstantInfoArray[i];
            String[] realEnumConstantInfo = enumConstantInfo.split("-");
            StringBuilder enumInfoStringBuilder = new StringBuilder(realEnumConstantInfo[0].toUpperCase());
            enumInfoStringBuilder.append("(").append("\"").append(realEnumConstantInfo[0]).append("\",").append("\"").append(realEnumConstantInfo[1]).append("\"").append(")");
            EnumWriteCommandAction enumWriteCommandAction = new EnumWriteCommandAction(enumInfoStringBuilder.toString(), psiClass, psiElementFactory, project);
            enumWriteCommandAction.execute();
        }
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
        String psiVariableName = psiClassName.substring(0, 1).toLowerCase() + psiClassName.substring(1);
        //生成方法模板
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("public static ").append(psiClassName);
        stringBuilder.append(" findBy").append(codeFieldName.substring(0, 1).toUpperCase()).append(codeFieldName.substring(1));
        stringBuilder.append("(").append(psiType.getPresentableText()).append(" ").append(codeFieldName).append(")");
        stringBuilder.append(" {").append("\n");
        stringBuilder.append("  for (").append(psiClassName).append(" ").append(psiVariableName)
                .append(" : values()) {").append("\n");
        stringBuilder.append("      if (Objects.equals(").append(psiVariableName).append(".get").append(codeFieldName.substring(0, 1).toUpperCase()).append(codeFieldName.substring(1)).append("(),").append(codeFieldName).append(")) {").append("\n");
        stringBuilder.append("          return ").append(psiVariableName).append(";").append("\n");
        stringBuilder.append("      }");
        stringBuilder.append("  }").append("\n");
        stringBuilder.append("  return null;");
        stringBuilder.append("}");
        MethodWriteCommandAction methodWriteCommandAction = new MethodWriteCommandAction(psiClass, stringBuilder.toString(), psiElementFactory, project);
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
    private void genConvertCodeToDesc(PsiField codeField, PsiField descFiled, PsiClass psiClass, PsiElementFactory psiElementFactory, Project project) {
        //当前编码类型
        PsiType codeFieldType = codeField.getType();
        //当前编码名称
        String codeFieldName = codeField.getName();
        //当前枚举类的名称
        String psiClassName = psiClass.getName();
        //当前枚举变量名称
        String psiVariableName = psiClassName.substring(0, 1).toLowerCase() + psiClassName.substring(1);
        //当前描述类型
        PsiType descFiledType = descFiled.getType();
        //当前描述名称
        String descFiledName = descFiled.getName();
        //生成方法模板
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("public static ").append(descFiledType.getPresentableText());
        stringBuilder.append(" convert").append(codeFieldName.substring(0, 1).toUpperCase()).append(codeFieldName.substring(1)).append("To").append(descFiledName.substring(0, 1).toUpperCase()).append(descFiledName.substring(1));
        stringBuilder.append("(").append(codeFieldType.getPresentableText()).append(" ").append(codeFieldName).append(")");
        stringBuilder.append(" {").append("\n");
        stringBuilder.append("  for (").append(psiClassName).append(" ").append(psiVariableName)
                .append(" : values()) {").append("\n");
        stringBuilder.append("      if (Objects.equals(").append(psiVariableName).append(".get").append(codeFieldName.substring(0, 1).toUpperCase()).append(codeFieldName.substring(1)).append("(),").append(codeFieldName).append(")) {").append("\n");
        stringBuilder.append("          return ").append(psiVariableName).append(".get").append(descFiledName.substring(0, 1).toUpperCase()).append(descFiledName.substring(1)).append("()").append(";").append("\n");
        stringBuilder.append("      }");
        stringBuilder.append("  }").append("\n");
        stringBuilder.append("  return null;");
        stringBuilder.append("}");
        MethodWriteCommandAction methodWriteCommandAction = new MethodWriteCommandAction(psiClass, stringBuilder.toString(), psiElementFactory, project);
        methodWriteCommandAction.execute();
    }
}
