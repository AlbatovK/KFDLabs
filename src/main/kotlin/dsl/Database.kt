package dsl

import kotlin.reflect.KClass

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
