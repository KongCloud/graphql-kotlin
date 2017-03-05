package graphql.validation.rules


import graphql.language.Field
import graphql.schema.GraphQLOutputType
import graphql.schema.SchemaUtil
import graphql.validation.*

class ScalarLeafs(validationContext: ValidationContext,
                  validationErrorCollector: ValidationErrorCollector)
    : AbstractRule(validationContext, validationErrorCollector) {

    private val schemaUtil = SchemaUtil()

    override fun checkField(field: Field) {
        val type = validationContext.outputType ?: return
        if (schemaUtil.isLeafType(type)) {
            if (field.selectionSet != null) {
                val message = String.format("Sub selection not allowed on leaf type %s", type.name)
                addError(ValidationError(ValidationErrorType.SubSelectionNotAllowed, field.sourceLocation, message))
            }
        } else {
            if (field.selectionSet == null) {
                val message = String.format("Sub selection required for type %s", type.name)
                addError(ValidationError(ValidationErrorType.SubSelectionRequired, field.sourceLocation, message))
            }
        }
    }
}
