package graphql.validation.rules


import graphql.execution.TypeFromAST
import graphql.language.OperationDefinition
import graphql.language.VariableDefinition
import graphql.language.VariableReference
import graphql.validation.*
import java.util.*

class VariableTypesMatchRule(validationContext: ValidationContext, validationErrorCollector: ValidationErrorCollector) : AbstractRule(validationContext, validationErrorCollector) {

    internal var variablesTypesMatcher = VariablesTypesMatcher()

    init {
        isVisitFragmentSpreads = true
    }

    private var variableDefinitionMap: MutableMap<String, VariableDefinition>? = null

    override fun checkOperationDefinition(operationDefinition: OperationDefinition) {
        variableDefinitionMap = LinkedHashMap<String, VariableDefinition>()
    }

    override fun checkVariableDefinition(variableDefinition: VariableDefinition) {
        variableDefinitionMap!!.put(variableDefinition.name, variableDefinition)
    }

    override fun checkVariable(variableReference: VariableReference) {
        val variableDefinition = variableDefinitionMap!![variableReference.name] ?: return
        val variableType = TypeFromAST.getTypeFromAST(validationContext.schema, variableDefinition.type) ?: return
        val inputType = validationContext.inputType
        if (!variablesTypesMatcher.doesVariableTypesMatch(variableType, variableDefinition.defaultValue!!, inputType)) {
            val message = "Variable type doesn't match"
            addError(ValidationError(ValidationErrorType.VariableTypeMismatch, variableReference.sourceLocation, message))
        }
    }


}