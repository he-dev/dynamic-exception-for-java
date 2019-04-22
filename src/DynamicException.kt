import java.io.File
import java.net.URI
import java.net.URL
import java.net.URLClassLoader
import java.util.*
import javax.tools.DiagnosticCollector
import javax.tools.JavaFileObject
import javax.tools.SimpleJavaFileObject
import javax.tools.ToolProvider

fun dynamicException(name: String, message: String): java.lang.Exception {
    val javaCompiler = ToolProvider.getSystemJavaCompiler()
    val diagnosticCollector = DiagnosticCollector<JavaFileObject>()

    val values = TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER)
    values.put("name", name)
    var sourceCode = SourceCodeJavaFileObject(
        "com.he-dev.${name}Exception",
        dynamicExceptionSourceCode.smartFormat(values)
    )
    val success = javaCompiler.getTask(
        null,
        null,
        diagnosticCollector,
        null,
        null,
        arrayListOf(sourceCode)
    ).call()

    val classLoader = URLClassLoader.newInstance(arrayOf<URL>(File("").toURI().toURL()))
    val ctor = Class.forName("${name}Exception", true, classLoader).getConstructor(String::class.java)
    ctor.isAccessible = true
    return ctor.newInstance(message) as java.lang.Exception
}


val dynamicExceptionSourceCode: String = """
public class {Name}Exception extends java.lang.Exception {
    public {Name}Exception(java.lang.String message) {
        super(message);
    }
    public {Name}Exception(java.lang.String message, java.lang.Throwable inner) {
        super(message, inner);
    }
}
""".trimIndent()

class SourceCodeJavaFileObject : SimpleJavaFileObject {
    val sourceCode: CharSequence

    constructor(className: String, sourceCode: CharSequence) :
            super(
                URI.create("string:///" + className.replace('.', '/') + JavaFileObject.Kind.SOURCE.extension),
                JavaFileObject.Kind.SOURCE
            ) {
        this.sourceCode = sourceCode
    }

    override fun getCharContent(ignoreEncodingErrors: Boolean): CharSequence {
        return sourceCode
    }
}