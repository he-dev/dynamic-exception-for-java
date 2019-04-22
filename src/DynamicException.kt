import java.io.File
import java.lang.reflect.Constructor
import java.net.URI
import java.net.URL
import java.net.URLClassLoader
import java.util.*
import javax.tools.DiagnosticCollector
import javax.tools.JavaFileObject
import javax.tools.SimpleJavaFileObject
import javax.tools.ToolProvider

fun dynamicException(name: String, message: String, inner: Throwable? = null): java.lang.Exception {
    val javaCompiler = ToolProvider.getSystemJavaCompiler()
    val diagnosticCollector = DiagnosticCollector<JavaFileObject>()

    val values = TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER)
    values["name"] = name
    var sourceCode = SourceCodeJavaFileObject(
        "com.he-dev.${name}Exception",
        dynamicExceptionSourceCode.smartFormat(values)
    )
    javaCompiler.getTask(
        null,
        null,
        diagnosticCollector,
        null,
        null,
        arrayListOf(sourceCode)
    ).call()

    val classLoader = URLClassLoader.newInstance(arrayOf<URL>(File("").toURI().toURL()))

    var getCtor: () -> Constructor<out Any> = {
        val cls = Class.forName("${name}Exception", true, classLoader)
        val ctor = if (inner == null) {
            cls.getConstructor(String::class.java)
        } else {
            cls.getConstructor(String::class.java, Throwable::class.java)
        }
        ctor.makeAccessible()
    }

    return if (inner == null) {
        getCtor().newInstance(message) as java.lang.Exception
    } else {
        getCtor().newInstance(message, inner) as java.lang.Exception
    }
}

fun Constructor<out Any>.makeAccessible(): Constructor<out Any> {
    this.isAccessible = true
    return this
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
    private val sourceCode: CharSequence

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