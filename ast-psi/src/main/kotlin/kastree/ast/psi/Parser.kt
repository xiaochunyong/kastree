package kastree.ast.psi

import kastree.ast.Node
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.com.intellij.openapi.util.Disposer
import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.com.intellij.psi.PsiErrorElement
import org.jetbrains.kotlin.com.intellij.psi.PsiManager
import org.jetbrains.kotlin.com.intellij.testFramework.LightVirtualFile
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.idea.KotlinFileType
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType

open class Parser(val converter: Converter = Converter) {
    protected val proj by lazy {
        KotlinCoreEnvironment.createForProduction(
            Disposer.newDisposable(),
            CompilerConfiguration(),
            EnvironmentConfigFiles.JVM_CONFIG_FILES
        ).project
    }

    fun parseFile(code: String, throwOnError: Boolean = true) = converter.convertFile(parsePsiFile(code).also { file ->
        if (throwOnError) file.collectDescendantsOfType<PsiErrorElement>().let {
            if (it.isNotEmpty()) throw ParseError(file, it)
        }
    })

    fun parsePsiFile(code: String): KtFile {
        val ktFile = PsiManager.getInstance(proj).findFile(LightVirtualFile("temp.kt", KotlinFileType.INSTANCE, code)) as KtFile
        return ktFile
    }


    data class ParseError(
        val file: KtFile,
        val errors: List<PsiErrorElement>
    ) : IllegalArgumentException("Failed with ${errors.size} errors, first: ${errors.first().errorDescription}")

    companion object : Parser() {
        init {
            // To hide annoying warning on Windows
            System.setProperty("idea.use.native.fs.for.win", "false")
        }
    }

}

fun main() {
    Parser().parseFile("""
        package me.ely.codegen
        import java.lang.String
        /**
         *
         *
         * @author  <a href="mailto:xiaochunyong@gmail.com">Ely</a>
         * @see
         * @since   2019-06-28
         */
        data class Field (
            /**
             * field type
             */
            private var type: String,

            /**
             * field name
             */
            val name: List<String>,

            /**
             * the field modifier, like "private",or "@Setter private" if include annotations
             */
            var modifier: String,

            /**
             * field doc comment
             */
            var comment: String

        )
    """.trimIndent())
}