package dsl

fun table(database: Database, initializer: SqlTableBuilder.() -> Unit) =
    SqlTableBuilder(database).apply(initializer).also {

        val contents = it.build().getColumnData()
        println(contents.keys.joinToString(" "))

        println(
            List(contents.values.size) { index ->
                contents.values.joinToString("\t") { j -> j[index].toString() }
            }.joinToString("\n")
        )
    }


/*
 * Supported types are described in Database Implementation
 * Order of columns is not specific
 * Some basic data type checks are being performed
 */
fun main() {
    val dbImpl = TestDatabaseImpl()
    table(dbImpl) {
        columns {
            column("ID", Int::class)
            column("Name", String::class)
            column("Age", Float::class)
            column("Gender", Boolean::class)
        }
        row {
            cell("ID", 12)
            cell("Name", "Daniil")
            cell("Age", 31.2f)
            cell("Gender", false)
        }
        row {
            cell("ID", 54)
            cell("Name", "Name1")
            cell("Gender", true)
            cell("Age", 16.1f)
        }
        row {
            cell("ID", 12)
            cell("Name", "Hello")
            cell("Age", 31.0f)
            cell("Gender", false)
        }
        row {
            cell("Name", "Kotlin")
            cell("Age", 12.9f)
            cell("ID", 10)
            cell("Gender", true)
        }
    }
}