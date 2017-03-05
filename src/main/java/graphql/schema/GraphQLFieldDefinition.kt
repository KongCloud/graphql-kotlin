package graphql.schema


import graphql.Assert.assertNotNull
import java.util.*
import kotlin.properties.Delegates

class GraphQLFieldDefinition<T>(val name: String,
                                val description: String?,
                                type: GraphQLOutputType,
                                val dataFetcher: DataFetcher<T>,
                                val arguments: List<GraphQLArgument>,
                                val deprecationReason: String?) {
    var type: GraphQLOutputType by Delegates.notNull<GraphQLOutputType>()
        private set


    init {
        assertNotNull(type, "type can't be null")
        this.type = type
    }


    internal fun replaceTypeReferences(typeMap: Map<String, GraphQLType>) {
        type = SchemaUtil().resolveTypeReference(type, typeMap) as GraphQLOutputType
    }

    fun getArgument(name: String): GraphQLArgument? {
        for (argument in arguments) {
            if (argument.name == name) return argument
        }
        return null
    }

    val deprecated: Boolean
        get() = deprecationReason != null

    class Builder<T> {
        private var name: String by Delegates.notNull<String>()
        private var description: String? = null
        private var type: GraphQLOutputType by Delegates.notNull<GraphQLOutputType>()
        private var dataFetcher: DataFetcher<T>? = null
        private val arguments = ArrayList<GraphQLArgument>()
        private var deprecationReason: String? = null
        private var isField: Boolean = false


        fun name(name: String): Builder<T> {
            this.name = name
            return this
        }

        fun description(description: String): Builder<T> {
            this.description = description
            return this
        }

        fun type(builder: GraphQLObjectType.Builder): Builder<T> {
            return type(builder.build())
        }

        fun type(builder: GraphQLInterfaceType.Builder): Builder<T> {
            return type(builder.build())
        }

        fun type(builder: GraphQLUnionType.Builder): Builder<T> {
            return type(builder.build())
        }

        fun type(type: GraphQLOutputType): Builder<T> {
            this.type = type
            return this
        }

        fun dataFetcher(dataFetcher: DataFetcher<T>): Builder<T> {
            this.dataFetcher = dataFetcher
            return this
        }

        fun staticValue(value: T): Builder<T> {
            this.dataFetcher = staticDataFetcher(value)

            return this
        }

        /**
         * Get the data from a field, rather than a property.

         * @return this builder
         */
        fun fetchField(): Builder<T> {
            this.isField = true
            return this
        }

        fun argument(argument: GraphQLArgument): Builder<T> {
            this.arguments.add(argument)
            return this
        }

        /**
         * Take an argument builder in a function definition and apply. Can be used in a jdk8 lambda
         * e.g.:
         * <pre>
         * `argument(a -> a.name("argumentName"))
        ` *
        </pre> *

         * @param builderFunction a supplier for the builder impl
         * *
         * @return this
         */
        fun argument(builderFunction: BuilderFunction<GraphQLArgument.Builder>): Builder<T> {
            var builder: GraphQLArgument.Builder = newArgument()
            builder = builderFunction.apply(builder)
            return argument(builder)
        }

        /**
         * Same effect as the argument(GraphQLArgument). Builder.build() is called
         * from within

         * @param builder an un-built/incomplete GraphQLArgument
         * *
         * @return this
         */
        fun argument(builder: GraphQLArgument.Builder): Builder<T> {
            this.arguments.add(builder.build())
            return this
        }

        fun argument(arguments: List<GraphQLArgument>): Builder<T> {
            this.arguments.addAll(arguments)
            return this
        }

        fun deprecate(deprecationReason: String): Builder<T> {
            this.deprecationReason = deprecationReason
            return this
        }

        fun build(): GraphQLFieldDefinition<T> {
            val fetcher: DataFetcher<T> = dataFetcher ?:
                if (isField) {
                    fieldDataFetcher<T>(name)
                } else {
                    propertyDataFetcher<T>(name)
                }

            return GraphQLFieldDefinition(name, description, type, fetcher, arguments, deprecationReason)
        }
    }

    companion object {
        fun <T> newFieldDefinition(): Builder<T> {
            return Builder()
        }
    }
}