package graphql.validation.rules

import graphql.language.Argument
import graphql.language.BooleanValue
import graphql.language.StringValue
import graphql.schema.GraphQLArgument
import graphql.schema.GraphQLDirective
import graphql.schema.GraphQLFieldDefinition
import graphql.validation.IValidationContext
import graphql.validation.ValidationContext
import graphql.validation.ValidationErrorCollector
import graphql.validation.ValidationErrorType
import spock.lang.Specification

import static graphql.ScalarsKt.GraphQLBoolean
import static graphql.ScalarsKt.GraphQLString

class KnownArgumentNamesTest extends Specification {

    IValidationContext validationContext = Mock(IValidationContext)
    ValidationErrorCollector errorCollector = new ValidationErrorCollector()
    KnownArgumentNames knownArgumentNames = new KnownArgumentNames(validationContext, errorCollector)

    def "unknown field argument"() {
        given:
        Argument argument = new Argument("unknownArg", new StringValue("value"))
        def fieldDefinition = GraphQLFieldDefinition.newFieldDefinition().name("field").type(GraphQLString)
                .argument(GraphQLArgument.newArgument().name("knownArg").type(GraphQLString).build()).build();
        validationContext.getFieldDef() >> fieldDefinition
        when:
        knownArgumentNames.checkArgument(argument)
        then:
        errorCollector.containsValidationError(ValidationErrorType.UnknownArgument)
    }

    def "known field argument"() {
        given:
        Argument argument = new Argument("knownArg", new StringValue("value"))
        def fieldDefinition = GraphQLFieldDefinition.newFieldDefinition().name("field").type(GraphQLString)
                .argument(GraphQLArgument.newArgument().name("knownArg").type(GraphQLString).build()).build();
        validationContext.getFieldDef() >> fieldDefinition
        when:
        knownArgumentNames.checkArgument(argument)
        then:
        errorCollector.errors().isEmpty()
    }

    def "unknown directive argument"() {
        given:
        Argument argument = new Argument("unknownArg", new BooleanValue(true))
        def fieldDefinition = GraphQLFieldDefinition.newFieldDefinition().name("field").type(GraphQLString).build()
        def directiveDefinition = GraphQLDirective.newDirective().name("directive")
                .argument(GraphQLArgument.newArgument().name("knownArg").type(GraphQLBoolean).build()).build()
        validationContext.getFieldDef() >> fieldDefinition
        validationContext.getDirective() >> directiveDefinition
        when:
        knownArgumentNames.checkArgument(argument)
        then:
        errorCollector.containsValidationError(ValidationErrorType.UnknownDirective)
    }

    def "known directive argument"() {
        given:
        Argument argument = new Argument("knownArg", new BooleanValue(true))
        def fieldDefinition = GraphQLFieldDefinition.newFieldDefinition().name("field").type(GraphQLString).build()
        def directiveDefinition = GraphQLDirective.newDirective().name("directive")
                .argument(GraphQLArgument.newArgument().name("knownArg").type(GraphQLBoolean).build()).build()
        validationContext.getFieldDef() >> fieldDefinition
        validationContext.getDirective() >> directiveDefinition
        when:
        knownArgumentNames.checkArgument(argument)
        then:
        errorCollector.errors().isEmpty()
    }

    def "directive argument not validated against field arguments"() {
        given:
        Argument argument = new Argument("unknownArg", new BooleanValue(true))
        def fieldDefinition = GraphQLFieldDefinition.newFieldDefinition().name("field").type(GraphQLString)
                .argument(GraphQLArgument.newArgument().name("unknownArg").type(GraphQLString).build()).build()
        def directiveDefinition = GraphQLDirective.newDirective().name("directive")
                .argument(GraphQLArgument.newArgument().name("knownArg").type(GraphQLBoolean).build()).build()
        validationContext.getFieldDef() >> fieldDefinition
        validationContext.getDirective() >> directiveDefinition
        when:
        knownArgumentNames.checkArgument(argument)
        then:
        errorCollector.containsValidationError(ValidationErrorType.UnknownDirective)
    }
}
