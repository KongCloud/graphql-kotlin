package graphql.execution

import graphql.GraphQLException
import graphql.TestUtilKt
import graphql.language.*
import graphql.schema.GraphQLArgument
import graphql.schema.GraphQLList
import graphql.schema.GraphQLNonNull
import spock.lang.Specification
import spock.lang.Unroll

import static graphql.ScalarsKt.*
import static graphql.schema.GraphQLEnumTypeKt.newEnum
import static graphql.schema.GraphQLInputObjectField.newInputObjectField
import static graphql.schema.GraphQLInputObjectType.newInputObject

class ValuesResolverTest extends Specification {

    ValuesResolver resolver = new ValuesResolver()


    @Unroll
    def "getVariableValues: simple variable input #inputValue"() {
        given:
        def schema = TestUtilKt.schemaWithInputType(inputType)
        VariableDefinition variableDefinition = new VariableDefinition("variable", variableType)

        when:
        def resolvedValues = resolver.getVariableValues(schema, [variableDefinition], [variable: inputValue])

        then:
        resolvedValues['variable'] == outputValue

        where:
        inputType      | variableType            | inputValue   || outputValue
        GraphQLInt     | new TypeName("Int")     | 100          || 100
        GraphQLInt     | new TypeName("Int")     | '100'        || 100
        GraphQLLong    | new TypeName("Long")    | 100l         || 100l
        GraphQLLong    | new TypeName("Long")    | '100'        || 100l
        GraphQLString  | new TypeName("String")  | 'someString' || 'someString'
        GraphQLBoolean | new TypeName("Boolean") | 'true'       || true
        GraphQLFloat   | new TypeName("Float")   | 42.43d       || 42.43d
        GraphQLFloat   | new TypeName("Float")   | 42.44f       || 42.44f
        GraphQLFloat   | new TypeName("Float")   | '42.45'      || 42.45d

    }

    def "getVariableValues: object as variable input"() {
        given:
        def nameField = newInputObjectField()
                .name("name")
                .type(GraphQLString)
        def idField = newInputObjectField()
                .name("id")
                .type(GraphQLInt)
        def inputType = newInputObject()
                .name("Person")
                .field(nameField)
                .field(idField)
                .build()
        def schema = TestUtilKt.schemaWithInputType(inputType)
        VariableDefinition variableDefinition = new VariableDefinition("variable", new TypeName("Person"))

        when:
        def resolvedValues = resolver.getVariableValues(schema, [variableDefinition], [variable: inputValue])
        then:
        resolvedValues['variable'] == outputValue
        where:
        inputValue           || outputValue
        [name: 'a', id: 123] || [name: 'a', id: 123]
        [id: 123]            || [id: 123]
        [name: 'x']          || [name: 'x']
    }

    def "getVariableValues: simple value gets resolved to a list when the type is a List"() {
        given:
        def schema = TestUtilKt.schemaWithInputType(new GraphQLList(GraphQLString))
        VariableDefinition variableDefinition = new VariableDefinition("variable", new ListType(new TypeName("String")))
        String value = "world"
        when:
        def resolvedValues = resolver.getVariableValues(schema, [variableDefinition], [variable: value])
        then:
        resolvedValues['variable'] == ['world']

    }


    def "getArgumentValues: resolves argument with variable reference"() {
        given:
        def variables = [var: 'hello']
        def fieldArgument = new GraphQLArgument("arg", null, GraphQLString, null)
        def argument = new Argument("arg", new VariableReference("var"))

        when:
        def values = resolver.argumentValues([fieldArgument], [argument], variables)

        then:
        values['arg'] == 'hello'
    }

    def "getArgumentValues: resolves object literal"() {
        given: "schema defining input object"
        def subObjectType = newInputObject()
                .name("SubType")
                .field(newInputObjectField()
                .name("subKey")
                .type(GraphQLBoolean))
                .build()
        def inputObjectType = newInputObject()
                .name("inputObject")
                .field(newInputObjectField()
                .name("intKey")
                .type(GraphQLInt))
                .field(newInputObjectField()
                .name("stringKey")
                .type(GraphQLString))
                .field(newInputObjectField()
                .name("subObject")
                .type(subObjectType))
                .build()
        def fieldArgument = new GraphQLArgument("arg", null,inputObjectType, null)

        when:
        def argument = new Argument("arg", inputValue)
        def values = resolver.argumentValues([fieldArgument], [argument], [:])

        then:
        values['arg'] == outputValue

        where:
        inputValue << [
                buildObjectLiteral([
                        intKey   : new IntValue(BigInteger.ONE),
                        stringKey: new StringValue("world"),
                        subObject: [
                                subKey: new BooleanValue(true)
                        ]
                ]),
                buildObjectLiteral([
                        intKey   : new IntValue(BigInteger.ONE),
                        stringKey: new StringValue("world")
                ]),
                buildObjectLiteral([
                        intKey: new IntValue(BigInteger.ONE)
                ])
        ]
        outputValue << [
                [intKey: 1, stringKey: 'world', subObject: [subKey: true]],
                [intKey: 1, stringKey: 'world'],
                [intKey: 1]
        ]
    }

    def "getArgumentValues: uses default value if object literal omits field"() {
        given: "schema defining input object"
        def inputObjectType = newInputObject()
                .name("inputObject")
                .field(newInputObjectField()
                .name("intKey")
                .type(new GraphQLNonNull(GraphQLInt))
                .defaultValue(3)
                .build())
                .field(newInputObjectField()
                .name("stringKey")
                .type(GraphQLString)
                .defaultValue("defaultString")
                .build())
                .build()
        def fieldArgument = new GraphQLArgument("arg", null, inputObjectType, null)

        when:
        def argument = new Argument("arg", inputValue)
        def values = resolver.argumentValues([fieldArgument], [argument], [:])

        then:
        values['arg'] == outputValue

        where:
        inputValue << [
                buildObjectLiteral([
                        intKey   : new IntValue(BigInteger.ONE),
                        stringKey: new StringValue("world")
                ]),
                buildObjectLiteral([
                        intKey: new IntValue(BigInteger.ONE)
                ]),
                buildObjectLiteral([:])
        ]
        outputValue << [
                [intKey: 1, stringKey: 'world'],
                [intKey: 1, stringKey: 'defaultString'],
                [intKey: 3, stringKey: 'defaultString']
        ]
    }

    def "getArgumentValues: missing InputObject fields which are non-null cause error"() {
        given: "schema defining input object"
        def inputObjectType = newInputObject()
                .name("inputObject")
                .field(newInputObjectField()
                .name("intKey")
                .type(new GraphQLNonNull(GraphQLInt))
                .build())
                .build()
        def fieldArgument = new GraphQLArgument("arg", null, inputObjectType, null)

        when:
        def argument = new Argument("arg", new ObjectValue())
        resolver.argumentValues([fieldArgument], [argument], [:])

        then:
        thrown(GraphQLException)
    }

    ObjectValue buildObjectLiteral(Map<String, Object> contents) {
        def object = new ObjectValue()
        contents.each { key, value ->
            def transformedValue = value instanceof Map ? buildObjectLiteral(value) : (Value) value
            object.getObjectFields().add(new ObjectField(key, transformedValue))
        }
        return object
    }

    def "getArgumentValues: resolves enum literals"() {
        given: "the ast"
        EnumValue enumValue1 = new EnumValue("PLUTO")
        EnumValue enumValue2 = new EnumValue("MARS")
        def argument1 = new Argument("arg1", enumValue1)
        def argument2 = new Argument("arg2", enumValue2)

        and: "the schema"
        def enumType = newEnum()
                .name("EnumType")
                .value("PLUTO")
                .value("MARS", "mars")
                .build()
        def fieldArgument1 = new GraphQLArgument("arg1", null, enumType, null)
        def fieldArgument2 = new GraphQLArgument("arg2", null, enumType, null)
        when:
        def values = resolver.argumentValues([fieldArgument1, fieldArgument2], [argument1, argument2], [:])

        then:
        values['arg1'] == 'PLUTO'
        values['arg2'] == 'mars'
    }

    def "getArgumentValues: resolves array literals"() {
        given:
        ArrayValue arrayValue = new ArrayValue()
        arrayValue.getValues().add(new BooleanValue(true))
        arrayValue.getValues().add(new BooleanValue(false))
        def argument = new Argument("arg", arrayValue)

        def fieldArgument = new GraphQLArgument("arg", null, new GraphQLList(GraphQLBoolean), null)

        when:
        def values = resolver.argumentValues([fieldArgument], [argument], [:])

        then:
        values['arg'] == [true, false]

    }

    def "getArgumentValues: resolves single value literal to a list when type is a list "() {
        given:
        StringValue stringValue = new StringValue("world")
        def argument = new Argument("arg", stringValue)

        def fieldArgument = new GraphQLArgument("arg", null, new GraphQLList(GraphQLString), null)

        when:
        def values = resolver.argumentValues([fieldArgument], [argument], [:])

        then:
        values['arg'] == ['world']

    }

    def "getVariableValues: enum as variable input"() {
        given:
        def enumDef = newEnum()
                .name("Test")
                .value("A_TEST")
                .value("VALUE_TEST", 1)
                .build()

        def schema = TestUtilKt.schemaWithInputType(enumDef)
        VariableDefinition variableDefinition = new VariableDefinition("variable", new TypeName("Test"))

        when:
        def resolvedValues = resolver.getVariableValues(schema, [variableDefinition], [variable: inputValue])
        then:
        resolvedValues['variable'] == outputValue
        where:
        inputValue   || outputValue
        "A_TEST"     || "A_TEST"
        "VALUE_TEST" || 1

    }

    def "getVariableValues: input object with non-required fields and default values"() {
        given:

        def inputObjectType = newInputObject()
                .name("InputObject")
                .field(newInputObjectField()
                .name("intKey")
                .type(GraphQLInt))
                .field(newInputObjectField()
                .name("stringKey")
                .type(GraphQLString)
                .defaultValue("defaultString"))
                .build()

        def schema = TestUtilKt.schemaWithInputType(inputObjectType)
        VariableDefinition variableDefinition = new VariableDefinition("variable", new TypeName("InputObject"))

        when:
        def resolvedValues = resolver.getVariableValues(schema, [variableDefinition], [variable: inputValue])

        then:
        resolvedValues['variable'] == outputValue

        where:
        inputValue                    || outputValue
        [intKey: 10]                  || [intKey: 10, stringKey: 'defaultString']
        [intKey: 10, stringKey: null] || [intKey: 10, stringKey: 'defaultString']

    }

    def "getVariableInput: Missing InputObject fields which are non-null cause error"() {

        given:
        def inputObjectType = newInputObject()
                .name("InputObject")
                .field(newInputObjectField()
                .name("intKey")
                .type(GraphQLInt))
                .field(newInputObjectField()
                .name("requiredField")
                .type(new GraphQLNonNull(GraphQLString)))
                .build()

        def schema = TestUtilKt.schemaWithInputType(inputObjectType)
        VariableDefinition variableDefinition = new VariableDefinition("variable", new TypeName("InputObject"))

        when:
        resolver.getVariableValues(schema, [variableDefinition], [variable: inputValue])

        then:
        thrown(GraphQLException)

        where:
        inputValue                        | _
        [intKey: 10]                      | _
        [intKey: 10, requiredField: null] | _
    }
}
