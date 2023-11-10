import kotlin.reflect.KClass

@DslMarker
annotation class TableBuilderDslMarker

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

@TableBuilderDslMarker
class SqlTableBuilder(private val database: Database) {

    fun columns(initializer: ColumnBuilder.() -> Unit) {
        ColumnBuilder(database).apply(initializer).build()
    }

    fun row(initializer: EntityBuilder.() -> Unit) {
        EntityBuilder(database).apply(initializer)
    }

    fun build() = database

}

data class SqlTableColumn<T : Any>(val name: String, val clazz: KClass<T>)

@TableBuilderDslMarker
class ColumnBuilder(private val database: Database) {

    private val columnInfo = mutableMapOf<String, SqlTableColumn<*>>()

    fun column(name: String, clazz: KClass<*>) {
        val availableDataTypes = database.getAvailableDataTypes()
        if (clazz !in availableDataTypes) throw RuntimeException("Unsupported data type")
        columnInfo[name] = SqlTableColumn(name, clazz)
    }

    fun build() = database.setTableInfo(columnInfo)

}

@TableBuilderDslMarker
class EntityBuilder(val database: Database) {

    inline fun <reified T : Any> cell(columnName: String, value: T) {
        val type = database.getTableInfo()[columnName]?.clazz
            ?: throw RuntimeException("No such column with name $columnName")
        val columnDataType = (T::class)
        if (type != columnDataType) {
            throw RuntimeException("Incorrect data type. Received ${columnDataType.simpleName}." +
                    " Should be ${type.simpleName}")
        }
        database.insertColumnData(columnName, value)
    }
}


interface Database {
    fun getAvailableDataTypes(): List<KClass<*>>
    fun setTableInfo(columnInfo: Map<String, SqlTableColumn<*>>)
    fun getTableInfo(): Map<String, SqlTableColumn<*>>
    fun insertColumnData(string: String, obj: Any)
    fun getColumnData(): Map<String, List<Any>>
}

class TestDatabaseImpl : Database {

    private var tableInfo: Map<String, SqlTableColumn<*>> = mapOf()

    private var entityInfo: MutableMap<String, MutableList<Any>> = mutableMapOf()

    override fun getAvailableDataTypes(): List<KClass<*>> {
        return listOf(Int::class, String::class, Float::class, Boolean::class)
    }

    override fun setTableInfo(columnInfo: Map<String, SqlTableColumn<*>>) {
        tableInfo = columnInfo.toMap()
    }

    override fun getTableInfo(): Map<String, SqlTableColumn<*>> {
        return tableInfo
    }

    override fun insertColumnData(string: String, obj: Any) {
        entityInfo.getOrPut(string, ::mutableListOf).add(obj)
    }

    override fun getColumnData(): Map<String, List<Any>> {
        return entityInfo
    }
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