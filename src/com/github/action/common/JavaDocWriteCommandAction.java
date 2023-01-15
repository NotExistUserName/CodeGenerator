package com.github.action.common;

import com.intellij.openapi.application.Result;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.javadoc.PsiDocComment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @Author xiexing01
 * @Description JAVA注释生成执行类
 * @Date 2023/1/2 17:32
 */
public class JavaDocWriteCommandAction extends WriteCommandAction {

    /**
     * 需要生成的注释内容
     */
    private String javaDocContent;

    /**
     * 第一个枚举对象
     */
    private PsiElement firstEnumConstant;

    /**
     * 需要生成注释的类
     */
    private PsiClass psiClass;

    /**
     * 生成节点工厂对象
     */
    private PsiElementFactory psiElementFactory;

    public JavaDocWriteCommandAction(@NotNull String javaDocContent,PsiElement firstEnumConstant,@NotNull PsiClass psiClass,@NotNull PsiElementFactory psiElementFactory, @Nullable Project project, @NotNull PsiFile... files) {
        super(project, files);
        this.javaDocContent = javaDocContent;
        this.firstEnumConstant = firstEnumConstant;
        this.psiClass = psiClass;
        this.psiElementFactory = psiElementFactory;
    }

    @Override
    protected void run(@NotNull Result result) throws Throwable {
        StringBuilder javaDoc = new StringBuilder();
        javaDoc.append("//").append(this.javaDocContent);
        PsiComment psiComment = this.psiElementFactory.createCommentFromText(javaDoc.toString(), null);
        psiClass.addBefore(psiComment,this.firstEnumConstant);
    }
}
