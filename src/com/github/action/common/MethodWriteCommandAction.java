package com.github.action.common;

import com.intellij.openapi.application.Result;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import org.jetbrains.annotations.NotNull;

/**
 * @Author CoffeeEngineer
 * @Description 在类中生成方法
 * @Date 2022/12/11 9:12
 */
public class MethodWriteCommandAction extends WriteCommandAction {

    /**
     * 需要生成方法的类
     */
    private PsiClass psiClass;

    /**
     * 生成节点工厂对象
     */
    private PsiElementFactory psiElementFactory;

    /**
     * 生成方法文本
     */
    private String methodText;

    public MethodWriteCommandAction(@NotNull PsiClass psiClass,@NotNull String methodText,@NotNull PsiElementFactory psiElementFactory, @NotNull Project project, @NotNull PsiFile... files) {
        super(project, files);
        this.psiClass = psiClass;
        this.methodText = methodText;
        this.psiElementFactory = psiElementFactory;
    }

    @Override
    protected void run(@NotNull Result result) throws Throwable {
        PsiMethod psiMethod = psiElementFactory.createMethodFromText(methodText, null);
        psiClass.add(psiMethod);
    }
}
