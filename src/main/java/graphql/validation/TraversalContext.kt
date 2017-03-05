package graphql.validation


import graphql.ShouldNotHappenException
import graphql.execution.TypeFromAST
import graphql.introspection.Introspection.SchemaMetaFieldDef
import graphql.introspection.Introspection.TypeMetaFieldDef
import graphql.introspection.Introspection.TypeNameMetaFieldDef
import graphql.language.*
import graphql.schema.*

import java.util.ArrayList


class TraversalContext(internal var schema: GraphQLSchema) : QueryLanguageVisitor {
    internal var outputTypeStack: MutableList<GraphQLOutputType> = ArrayList()
    internal var parentTypeStack: MutableList<GraphQLCompositeType> = ArrayList()
    internal var inputTypeStack: MutableList<GraphQLInputType> = ArrayList()
    internal var fieldDefStack: MutableList<GraphQLFieldDefinition<*>> = ArrayList()
    var directive: GraphQLDirective? = null
        internal set
    var argument: GraphQLArgument? = null
        internal set

    internal var schemaUtil = SchemaUtil()

    override fun enter(node: Node, path: List<Node>) {
        when (node) {
            is OperationDefinition -> enterImpl(node)
            is SelectionSet        -> enterImpl(node)
            is Field               -> enterImpl(node)
            is Directive           -> enterImpl(node)
            is InlineFragment      -> enterImpl(node)
            is FragmentDefinition  -> enterImpl(node)
            is VariableDefinition  -> enterImpl(node)
            is Argument            -> enterImpl(node)
            is ArrayValue          -> enterImpl(node)
            is ObjectField         -> enterImpl(node)
        }
    }


    private fun enterImpl(selectionSet: SelectionSet) {
        val rawType = outputType?.let { SchemaUtil().getUnmodifiedType(it) }
        if (rawType is GraphQLCompositeType) {
            addParentType(rawType)
        }
    }

    private fun enterImpl(field: Field) {
        val parentType = parentType
        var fieldDefinition: GraphQLFieldDefinition<*>? = null
        if (parentType != null) {
            fieldDefinition = getFieldDef(schema, parentType, field)
        }
        if (fieldDefinition != null) {
            addFieldDef(fieldDefinition)
            addType(fieldDefinition.type)
        }
    }

    private fun enterImpl(directive: Directive) {
        this.directive = schema.directive(directive.name)
    }

    private fun enterImpl(operationDefinition: OperationDefinition) {
        if (operationDefinition.operation === OperationDefinition.Operation.MUTATION && schema.isSupportingMutations) {
            addType(schema.mutationType!!)
        } else if (operationDefinition.operation === OperationDefinition.Operation.QUERY) {
            addType(schema.queryType)
        } else {
            throw ShouldNotHappenException()
        }
    }

    private fun enterImpl(inlineFragment: InlineFragment) {
        val typeCondition = inlineFragment.typeCondition
        val type: GraphQLOutputType
        if (typeCondition != null) {
            type = schema.type(typeCondition.name) as GraphQLOutputType
        } else {
            type = parentType as GraphQLOutputType
        }
        addType(type)
    }

    private fun enterImpl(fragmentDefinition: FragmentDefinition) {
        val type = schema.type(fragmentDefinition.typeCondition.name)
        addType(type as GraphQLOutputType)
    }

    private fun enterImpl(variableDefinition: VariableDefinition) {
        val type = TypeFromAST.getTypeFromAST(schema, variableDefinition.type)
        addInputType(type as GraphQLInputType)
    }

    private fun enterImpl(argument: Argument) {
        val argumentType: GraphQLArgument? =
                if (directive != null) {
                    find(directive!!.arguments, argument.name)
                } else if (fieldDef != null) {
                    find(fieldDef!!.arguments, argument.name)
                } else {
                    null
                }

        if (argumentType != null)
            addInputType(argumentType.type)
        this.argument = argumentType
    }

    private fun enterImpl(arrayValue: ArrayValue) {
        val nullableType = getNullableType(inputType)
        var inputType: GraphQLInputType? = null
        if (nullableType is GraphQLList) {
            inputType = nullableType.wrappedType as GraphQLInputType
        }
        if (inputType != null) {
            addInputType(inputType)
        }
    }

    private fun enterImpl(objectField: ObjectField) {
        val objectType = schemaUtil.getUnmodifiedType(inputType)
        var inputType: GraphQLInputType? = null
        if (objectType is GraphQLInputObjectType) {
            val inputField = objectType.field(objectField.name!!)
            if (inputField != null)
                inputType = inputField.type
        }
        if (inputType != null) {
            addInputType(inputType)
        }
    }

    private fun find(arguments: List<GraphQLArgument>, name: String): GraphQLArgument? {
        return arguments.firstOrNull { it.name == name }
    }


    override fun leave(node: Node, path: List<Node>) {
        when (node) {
            is OperationDefinition -> outputTypeStack.removeAt(outputTypeStack.size - 1)
            is SelectionSet        -> parentTypeStack.removeAt(parentTypeStack.size - 1)
            is Field               -> {
                fieldDefStack.removeAt(fieldDefStack.size - 1)
                outputTypeStack.removeAt(outputTypeStack.size - 1)
            }
            is Directive           -> directive = null
            is InlineFragment      -> outputTypeStack.removeAt(outputTypeStack.size - 1)
            is FragmentDefinition  -> outputTypeStack.removeAt(outputTypeStack.size - 1)
            is VariableDefinition  -> inputTypeStack.removeAt(inputTypeStack.size - 1)
            is Argument            -> {
                argument = null
                inputTypeStack.removeAt(inputTypeStack.size - 1)
            }
            is ArrayValue          -> inputTypeStack.removeAt(inputTypeStack.size - 1)
            is ObjectField         -> inputTypeStack.removeAt(inputTypeStack.size - 1)
        }
    }


    private fun getNullableType(type: GraphQLType): GraphQLNullableType =
            when (type) {
                is GraphQLNonNull -> type.wrappedType
                else              -> type
            } as GraphQLNullableType

    val outputType: GraphQLOutputType?
        get() {
            return lastElement(outputTypeStack)
        }

    private fun addType(type: GraphQLOutputType) {
        outputTypeStack.add(type)
    }

    private fun <T> lastElement(list: List<T>): T? {
        if (list.isEmpty()) return null
        return list[list.size - 1]
    }

    val parentType: GraphQLCompositeType?
        get() {
            return lastElement(parentTypeStack)
        }

    private fun addParentType(compositeType: GraphQLCompositeType) {
        parentTypeStack.add(compositeType)
    }

    val inputType: GraphQLInputType
        get() {
            return lastElement(inputTypeStack)!!
        }

    private fun addInputType(graphQLInputType: GraphQLInputType) {
        inputTypeStack.add(graphQLInputType)
    }

    val fieldDef: GraphQLFieldDefinition<*>?
        get() {
            return lastElement(fieldDefStack)
        }

    private fun addFieldDef(fieldDefinition: GraphQLFieldDefinition<*>) {
        fieldDefStack.add(fieldDefinition)
    }


    private fun getFieldDef(schema: GraphQLSchema, parentType: GraphQLType, field: Field):
            GraphQLFieldDefinition<*>? {
        if (schema.queryType == parentType) {
            if (field.name == SchemaMetaFieldDef.name) {
                return SchemaMetaFieldDef
            }
            if (field.name == TypeMetaFieldDef.name) {
                return TypeMetaFieldDef
            }
        }
        if (field.name == TypeNameMetaFieldDef.name && (parentType is GraphQLObjectType ||
                parentType is GraphQLInterfaceType ||
                parentType is GraphQLUnionType)) {
            return TypeNameMetaFieldDef
        }
        if (parentType is GraphQLFieldsContainer) {
            return parentType.fieldDefinitions.firstOrNull { it.name == field.name }
        }
        return null
    }
}