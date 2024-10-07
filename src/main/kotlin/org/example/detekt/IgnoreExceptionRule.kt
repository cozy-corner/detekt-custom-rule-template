package org.example.detekt

import io.gitlab.arturbosch.detekt.api.*
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.anyDescendantOfType
import org.jetbrains.kotlin.psi.psiUtil.lastBlockStatementOrThis

class IgnoreExceptionRule(config: Config) : Rule(config) {
    override val issue = Issue(
        javaClass.simpleName,
        Severity.Defect,
        "This rule reports ignored exceptions in catch block",
        Debt.FIVE_MINS,
    )

    override fun visitCatchSection(catchClause: KtCatchClause) {
        super.visitCatchSection(catchClause)
        // if empty catch block then default to report
        val catchBody = catchClause.catchBody ?: return

        if (hasBranches(catchBody) && isHandledBeforePassCatchBlock(catchBody).not()) {
            report(
                CodeSmell(
                    issue,
                    Entity.from(catchClause),
                    "Exception is ignored"
                )
            )
        }
    }

    private fun hasBranches(catchBody: KtExpression): Boolean {
        return catchBody.anyDescendantOfType<KtIfExpression>() || catchBody.anyDescendantOfType<KtWhenExpression>()
    }

    private fun isHandledBeforePassCatchBlock(catchBody: KtExpression): Boolean {
        val lastStatement = catchBody.lastBlockStatementOrThis()
        return lastStatement is KtThrowExpression || lastStatement is KtReturnExpression
    }
}
