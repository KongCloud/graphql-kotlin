package graphql;


import graphql.execution.Execution;
import graphql.execution.ExecutionStrategy;
import graphql.language.Document;
import graphql.language.SourceLocation;
import graphql.parser.Parser;
import graphql.schema.GraphQLSchema;
import graphql.validation.ValidationError;
import graphql.validation.Validator;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static graphql.Assert.assertNotNull;

public class GraphQL {


    private final GraphQLSchema graphQLSchema;
    private final ExecutionStrategy executionStrategy;

    private static final Logger log = LoggerFactory.getLogger(GraphQL.class);

    public GraphQL(GraphQLSchema graphQLSchema) {
        this(graphQLSchema, null);
    }


    public GraphQL(GraphQLSchema graphQLSchema, ExecutionStrategy executionStrategy) {
        this.graphQLSchema = graphQLSchema;
        this.executionStrategy = executionStrategy;
    }

    public CompletionStage<ExecutionResult> execute(String requestString) {
        return execute(requestString, null);
    }

    public CompletionStage<ExecutionResult> execute(String requestString, Object context) {
        return execute(requestString, context, Collections.emptyMap());
    }

    public CompletionStage<ExecutionResult> execute(String requestString, String operationName, Object context) {
        return execute(requestString, operationName, context, Collections.emptyMap());
    }

    public CompletionStage<ExecutionResult> execute(String requestString, Object context, Map<String, Object> arguments) {
        return execute(requestString, null, context, arguments);
    }

    public CompletionStage<ExecutionResult> execute(String requestString, String operationName, Object context, Map<String, Object> arguments) {
        CompletableFuture<ExecutionResult> promise = new CompletableFuture<>();

        assertNotNull(arguments, "arguments can't be null");
        log.info("Executing request. operation name: {}. Request: {} ", operationName, requestString);
        Parser parser = new Parser();
        Document document;
        try {
            document = parser.parseDocument(requestString);
        } catch (ParseCancellationException e) {
            RecognitionException recognitionException = (RecognitionException) e.getCause();
            SourceLocation sourceLocation = new SourceLocation(recognitionException.getOffendingToken().getLine(), recognitionException.getOffendingToken().getCharPositionInLine());
            InvalidSyntaxError invalidSyntaxError = new InvalidSyntaxError(sourceLocation);
            promise.complete(new ExecutionResultImpl(Collections.singletonList(invalidSyntaxError)));
            return promise;
        }

        Validator validator = new Validator();
        List<ValidationError> validationErrors = validator.validateDocument(graphQLSchema, document);
        if (validationErrors.size() > 0) {
            promise.complete(new ExecutionResultImpl(validationErrors));
            return promise;
        }

        Execution execution = new Execution(executionStrategy);
        return execution.execute(graphQLSchema, context, document, operationName, arguments);
    }


}