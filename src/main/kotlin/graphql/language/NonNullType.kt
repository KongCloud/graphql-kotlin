package graphql.language



class NonNullType : AbstractNode, Type {

    var type: Type? = null

    constructor()

    constructor(type: Type) {
        this.type = type
    }

    override val children: List<Node>
        get() {
            val result = mutableListOf<Node>()
            type?.let { result.add(it) }
            return result
        }

    override fun isEqualTo(node: Node): Boolean {
        if (this === node) return true
        if (javaClass != node.javaClass) return false

        return true

    }

    override fun toString(): String {
        return "NonNullType{" +
                "type=" + type +
                '}'
    }
}
