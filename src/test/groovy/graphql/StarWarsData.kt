package graphql

import graphql.schema.DataFetcher
import graphql.schema.TypeResolver
import java.util.concurrent.CompletableFuture


val luke = mapOf(
        "id" to "1000",
        "name" to "Luke Skywalker",
        "friends" to listOf("1002", "1003", "2000", "2001"),
        "appearsIn" to listOf(4, 5, 6),
        "homePlanet" to "Tatooine"
);

val vader = mapOf(
        "id" to "1001",
        "name" to "Darth Vader",
        "friends" to listOf("1004"),
        "appearsIn" to listOf(4, 5, 6),
        "homePlanet" to "Tatooine")

val han = mapOf(
        "id" to "1002",
        "name" to "Han Solo",
        "friends" to listOf("1000", "1003", "2001"),
        "appearsIn" to listOf(4, 5, 6)
)

val leia = mapOf(
        "id" to "1003",
        "name" to "Leia Organa",
        "friends" to listOf("1000", "1002", "2000", "2001"),
        "appearsIn" to listOf(4, 5, 6),
        "homePlanet" to "Alderaan"
)

val tarkin = mapOf(
        "id" to "1004",
        "name" to "Wilhuff Tarkin",
        "friends" to listOf("1001"),
        "appearsIn" to listOf(4)
)

val humanData = mapOf(
        "1000" to luke,
        "1001" to vader,
        "1002" to han,
        "1003" to leia,
        "1004" to tarkin
)

val threepio = mapOf(
        "id" to "2000",
        "name" to "C-3PO",
        "friends" to listOf("1000", "1002", "1003", "2001"),
        "appearsIn" to listOf(4, 5, 6),
        "primaryFunction" to "Protocol"
)

val artoo = mapOf(
        "id" to "2001",
        "name" to "R2-D2",
        "friends" to listOf("1000", "1002", "1003"),
        "appearsIn" to listOf(4, 5, 6),
        "primaryFunction" to "Astromech"
)

val droidData = mapOf(
        "2000" to threepio,
        "2001" to artoo
)

fun getCharacter(id: String): Any? {
    if (humanData[id] != null) return humanData[id]
    if (droidData[id] != null) return droidData[id]
    return null
}

val humanDataFetcher: DataFetcher<Any?> = { environment ->
    val id = environment.arguments["id"]
    CompletableFuture.completedFuture(humanData[id])
}


val droidDataFetcher: DataFetcher<Any?> = { environment ->
    val id = environment.arguments["id"]
    CompletableFuture.completedFuture(droidData[id])
}

val characterTypeResolver: TypeResolver = {
    when (it) {
//            is Human
        else -> null
    }
}
//    static characterTypeResolver =
//    {
//        object ->
//        def id = object.id
//        if (humanData[id] != null)
//            return StarWarsSchema.humanType
//        if (droidData[id] != null)
//            return StarWarsSchema.droidType
//        return null;
//    }

val friendsDataFetcher: DataFetcher<Any?> = { environment ->

    //        List<Object> result = []
//        for (String id to environment . source . friends) {
//        result.add(getCharacter(id))
//    }
    //result
    CompletableFuture.completedFuture(null)
}

val heroDataFetcher: DataFetcher<Any?> = { environment ->
    if (environment.containsArgument("episode")
            && 5 == environment.arguments["episode"])
        CompletableFuture.completedFuture("luke")
    else CompletableFuture.completedFuture("artoo")
}