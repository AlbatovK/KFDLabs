package dsl

import kotlin.reflect.KClass

data class SqlTableColumn<T : Any>(val name: String, val clazz: KClass<T>)

@TableBuilderDslMarker
class ColumnBuilder(private val database: Database) {

    private val columnInfo = mutableMapOf<String, SqlTableColumn<*>>()

    @Suppress("TooGenericExceptionThrown")
    fun column(name: String, clazz: KClass<*>) {
        val availableDataTypes = database.getAvailableDataTypes()
        if (clazz !in availableDataTypes) throw RuntimeException("Unsupported data type")
        columnInfo[name] = SqlTableColumn(name, clazz)
    }

    fun build() = database.setTableInfo(columnInfo)
}
