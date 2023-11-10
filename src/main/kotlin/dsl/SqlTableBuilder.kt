package dsl

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
