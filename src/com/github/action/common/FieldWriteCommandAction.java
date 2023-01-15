package com.github.action.common;

import com.intellij.openapi.application.Result;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @Author xiexing01
 * @Description 字段生成执行类
 * @Date 2023/1/12 22:05
 */
public class FieldWriteCommandAction extends WriteCommandAction {

    /**
     * 生成字段字符串
     */
    private String fieldString;

    /**
     * 需要生成字段的类
     */
    private PsiClass psiClass;

    /**
     * 生成节点工厂对象
     */
    private PsiElementFactory psiElementFactory;

    public FieldWriteCommandAction(String fieldString,PsiClass psiClass,PsiElementFactory psiElementFactory,@Nullable Project project, @NotNull PsiFile... files) {
        super(project, files);
        this.fieldString = fieldString;
        this.psiClass = psiClass;
        this.psiElementFactory = psiElementFactory;
    }

    @Override
    protected void run(@NotNull Result result) throws Throwable {
        PsiField psiField = psiElementFactory.createFieldFromText(this.fieldString + ";", null);
        this.psiClass.add(psiField);
    }
}
