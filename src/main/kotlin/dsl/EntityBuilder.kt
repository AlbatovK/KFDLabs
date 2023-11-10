package dsl

@TableBuilderDslMarker
class EntityBuilder(val database: Database) {
    @Suppress("TooGenericExceptionThrown")
    inline fun <reified T : Any> cell(columnName: String, value: T) {
        val type = database.getTableInfo()[columnName]?.clazz
            ?: throw RuntimeException("No such column with name $columnName")
        val columnDataType = (T::class)
        if (type != columnDataType) {
            throw RuntimeException(
                "Incorrect data type. Received ${columnDataType.simpleName}." +
                        " Should be ${type.simpleName}"
            )
        }
        database.insertColumnData(columnName, value)
    }
}
