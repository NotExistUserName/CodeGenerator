package com.github.action.common;

import com.intellij.openapi.application.Result;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @Author xiexing01
 * @Description 标识符生成执行类
 * @Date 2023/1/5 22:41
 */
public class IdentifierWriteCommandAction extends WriteCommandAction {

    /**
     * 需要生成的描述符
     */
    private String identifier;

    /**
     * 需要生成描述符的类
     */
    private PsiClass psiClass;

    /**
     * 生成节点工厂对象
     */
    private PsiElementFactory psiElementFactory;
    
    public IdentifierWriteCommandAction(@NotNull String identifier,@NotNull PsiClass psiClass,@NotNull PsiElementFactory psiElementFactory, @Nullable Project project, @NotNull PsiFile... files) {
        super(project, files);
        this.identifier = identifier;
        this.psiClass = psiClass;
        this.psiElementFactory = psiElementFactory;
    }

    @Override
    protected void run(@NotNull Result result) throws Throwable {
        PsiCodeBlock psiCodeBlock = psiElementFactory.createCodeBlockFromText("YES(\"yes\", \"是\");", null);
        psiClass.add(psiCodeBlock);
    }
}
