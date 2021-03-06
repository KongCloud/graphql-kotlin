package graphql.language


data class StringValue(var value: String) : AbstractNode(), Value {

    override fun isEqualTo(node: Node): Boolean {
        if (this === node) return true
        if (javaClass != node.javaClass) return false

        val that = node as StringValue

        return value == that.value
    }

}
