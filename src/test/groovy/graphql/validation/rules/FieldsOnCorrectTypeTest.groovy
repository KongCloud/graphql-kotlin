package graphql.validation.rules

import graphql.ScalarsKt
import graphql.language.Field
import graphql.schema.GraphQLFieldDefinition
import graphql.schema.GraphQLObjectType
import graphql.validation.IValidationContext
import graphql.validation.ValidationErrorCollector
import graphql.validation.ValidationErrorType
import spock.lang.Specification

class FieldsOnCorrectTypeTest extends Specification {

    ValidationErrorCollector errorCollector = new ValidationErrorCollector()
    IValidationContext validationContext = Mock(IValidationContext)
    FieldsOnCorrectType fieldsOnCorrectType = new FieldsOnCorrectType(validationContext, errorCollector)


    def "should add error to collector when field definition is null"() {
        given:
        def parentType = GraphQLObjectType.newObject().name("parentType").build()
        validationContext.getParentType() >> parentType
        validationContext.getFieldDef() >> null
        def field = new Field("name")

        when:
        fieldsOnCorrectType.checkField(field)

        then:
        errorCollector.containsValidationError(ValidationErrorType.FieldUndefined)
        errorCollector.errors().size() == 1
        errorCollector.errors()[0].message() == "Validation error of type FieldUndefined: Field name is undefined"
    }

    def "should results in no error when field definition is filled"() {
        given:
        def parentType = GraphQLObjectType.newObject().name("parentType").build()
        validationContext.getParentType() >> parentType
        validationContext.getFieldDef() >> GraphQLFieldDefinition.newFieldDefinition().name("n").type(ScalarsKt.GraphQLString).build()
        def field = new Field("name")

        when:
        fieldsOnCorrectType.checkField(field)

        then:
        errorCollector.errors().isEmpty()
    }

    def "should results in no error when parent type is null"() {
        given:
        validationContext.getParentType() >> null
        def field = new Field("name")

        when:
        fieldsOnCorrectType.checkField(field)

        then:
        errorCollector.errors().isEmpty()
    }
}
