package graphql;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInputObjectField;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static graphql.Scalars.GraphQLBoolean;
import static graphql.Scalars.GraphQLInt;

public class NestedInputSchema {


    public static GraphQLSchema createSchema() {


        GraphQLObjectType root = rootType();

        return GraphQLSchema.newSchema()
                .query(root)
                .build();
    }

    public static GraphQLObjectType rootType() {
        return GraphQLObjectType.newObject()

                .name("Root")
                .field(GraphQLFieldDefinition.newFieldDefinition()
                        .name("value")
                        .type(GraphQLInt)
                        .dataFetcher(new DataFetcher() {
                            @Override
                            public CompletionStage<Object> get(DataFetchingEnvironment environment) {
                                Integer initialValue = environment.getArgument("initialValue");
                                Map<String, Object> filter = environment.getArgument("filter");
                                if (filter != null) {
                                    if (filter.containsKey("even")) {
                                        Boolean even = (Boolean) filter.get("even");
                                        if (even && (initialValue%2 != 0)) {
                                            return CompletableFuture.completedFuture(0);
                                        } else if (!even && (initialValue%2 == 0)) {
                                            return CompletableFuture.completedFuture(0);
                                        }
                                    }
                                    if (filter.containsKey("range")) {
                                        Map<String, Integer> range = (Map<String, Integer>) filter.get("range");
                                        if (initialValue < range.get("lowerBound") ||
                                                initialValue > range.get("upperBound")) {
                                            return CompletableFuture.completedFuture(0);
                                        }
                                    }
                                }
                                return CompletableFuture.completedFuture(initialValue);
                            }})
                        .argument(GraphQLArgument.newArgument()
                                .name("intialValue")
                                .type(GraphQLInt)
                                .defaultValue(5))
                        .argument(GraphQLArgument.newArgument()
                                .name("filter")
                                .type(filterType())))
                .build();
    }

    public static GraphQLInputObjectType filterType() {
        return GraphQLInputObjectType.newInputObject()
                .name("Filter")
                .field(GraphQLInputObjectField.newInputObjectField()
                        .name("even")
                        .type(GraphQLBoolean))
                .field(GraphQLInputObjectField.newInputObjectField()
                        .name("range")
                        .type(rangeType()))
                .build();
    }

    public static GraphQLInputObjectType rangeType() {
        return GraphQLInputObjectType.newInputObject()
                .name("Range")
                .field(GraphQLInputObjectField.newInputObjectField()
                        .name("lowerBound")
                        .type(GraphQLInt))
                .field(GraphQLInputObjectField.newInputObjectField()
                        .name("upperBound")
                        .type(GraphQLInt))
                .build();
    }
}