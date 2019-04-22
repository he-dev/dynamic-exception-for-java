import java.util.*

fun String.smartFormat(values: TreeMap<String, String>): String {
    val regex = Regex("""\{(?<name>[a-z][a-z0-9_.-]*)\}""", RegexOption.IGNORE_CASE)
    return regex.replace(this) {
        var key = it.groups["name"]?.value
        if (values.containsKey(key)) values[key]!! else it.value
    }
}