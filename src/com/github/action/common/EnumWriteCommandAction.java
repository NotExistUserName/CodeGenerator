package com.github.action.common;

import com.intellij.openapi.application.Result;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @Author xiexing01
 * @Description 枚举常量生成执行类
 * @Date 2023/1/2 22:27
 */
public class EnumWriteCommandAction extends WriteCommandAction {

    /**
     * 枚举常量信息
     */
    private String enumConstantInfo;

    /**
     * 需要生成枚举对象的类
     */
    private PsiClass psiClass;

    /**
     * 生成节点工厂对象
     */
    private PsiElementFactory psiElementFactory;

    public EnumWriteCommandAction(@NotNull String enumConstantInfo, @NotNull PsiClass psiClass, @NotNull PsiElementFactory psiElementFactory, @Nullable Project project, @NotNull PsiFile... files) {
        super(project, files);
        this.enumConstantInfo = enumConstantInfo;
        this.psiClass = psiClass;
        this.psiElementFactory = psiElementFactory;
    }

    @Override
    protected void run(@NotNull Result result) throws Throwable {
        PsiField psiField = psiElementFactory.createEnumConstantFromText(this.enumConstantInfo, psiClass);
        this.psiClass.add(psiField);
    }
}
