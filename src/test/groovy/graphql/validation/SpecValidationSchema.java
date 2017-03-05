package graphql.validation;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import graphql.Scalars;
import graphql.schema.FieldDataFetcher;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLEnumType;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInterfaceType;
import graphql.schema.GraphQLNonNull;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLUnionType;
import graphql.schema.TypeResolver;
import graphql.validation.SpecValidationSchemaPojos.Alien;
import graphql.validation.SpecValidationSchemaPojos.Cat;
import graphql.validation.SpecValidationSchemaPojos.Dog;
import graphql.validation.SpecValidationSchemaPojos.Human;

/**
 * Sample schema used in the spec for validation examples
 * http://facebook.github.io/graphql/#sec-Validation
 * @author dwinsor
 *        
 */
public class SpecValidationSchema {
    public static final GraphQLEnumType dogCommand = GraphQLEnumType.Companion.newEnum()
                                                                              .name("DogCommand")
                                                                              .value("SIT")
                                                                              .value("DOWN")
                                                                              .value("HEEL")
                                                                              .build();
            
    public static final GraphQLEnumType catCommand = GraphQLEnumType.Companion.newEnum()
                                                                              .name("CatCommand")
                                                                              .value("JUMP")
                                                                              .build();
            
    public static final GraphQLInterfaceType sentient = GraphQLInterfaceType.Companion.newInterface()
                                                                                      .name("Sentient")
                                                                                      .field(new GraphQLFieldDefinition(
                    "name", null, new GraphQLNonNull(Scalars.INSTANCE.getGraphQLString()), new FieldDataFetcher("name"), Collections.<GraphQLArgument>emptyList(), null))
                                                                                      .typeResolver(new TypeResolver() {
            @Override
            public GraphQLObjectType getType(Object object) {
                if (object instanceof Human) return human;
                if (object instanceof Alien) return alien;
                return null;
            }})
                                                                                      .build();
            
    public static final GraphQLInterfaceType pet = GraphQLInterfaceType.Companion.newInterface()
                                                                                 .name("Pet")
                                                                                 .field(new GraphQLFieldDefinition(
                    "name", null, new GraphQLNonNull(Scalars.INSTANCE.getGraphQLString()), new FieldDataFetcher("name"), Collections.<GraphQLArgument>emptyList(), null))
                                                                                 .typeResolver(new TypeResolver() {
            @Override
            public GraphQLObjectType getType(Object object) {
                if (object instanceof Dog) return dog;
                if (object instanceof Cat) return cat;
                return null;
            }})
                                                                                 .build();
            
    public static final GraphQLObjectType human = GraphQLObjectType.Companion.newObject()
                                                                             .name("Human")
                                                                             .field(new GraphQLFieldDefinition(
                    "name", null, new GraphQLNonNull(Scalars.INSTANCE.getGraphQLString()), new FieldDataFetcher("name"), Collections.<GraphQLArgument>emptyList(), null))
                                                                             .withInterface(SpecValidationSchema.sentient)
                                                                             .build();
            
    public static final GraphQLObjectType alien = GraphQLObjectType.Companion.newObject()
                                                                             .name("Alien")
                                                                             .field(new GraphQLFieldDefinition(
                    "name", null, new GraphQLNonNull(Scalars.INSTANCE.getGraphQLString()), new FieldDataFetcher("name"), Collections.<GraphQLArgument>emptyList(), null))
                                                                             .field(new GraphQLFieldDefinition(
                    "homePlanet", null, Scalars.INSTANCE.getGraphQLString(), new FieldDataFetcher("homePlanet"), Collections.<GraphQLArgument>emptyList(), null))
                                                                             .withInterface(SpecValidationSchema.sentient)
                                                                             .build();
            
    public static final GraphQLArgument dogCommandArg = GraphQLArgument.Companion.newArgument()
                                                                                 .name("dogCommand")
                                                                                 .type(new GraphQLNonNull(dogCommand))
                                                                                 .build();
            
    public static final GraphQLArgument atOtherHomesArg = GraphQLArgument.Companion.newArgument()
                                                                                   .name("atOtherHomes")
                                                                                   .type(Scalars.INSTANCE.getGraphQLBoolean())
                                                                                   .build();
            
    public static final GraphQLArgument catCommandArg = GraphQLArgument.Companion.newArgument()
                                                                                 .name("catCommand")
                                                                                 .type(new GraphQLNonNull(catCommand))
                                                                                 .build();
            
    public static final GraphQLObjectType dog = GraphQLObjectType.Companion.newObject()
                                                                           .name("Dog")
                                                                           .field(new GraphQLFieldDefinition(
                    "name", null, new GraphQLNonNull(Scalars.INSTANCE.getGraphQLString()), new FieldDataFetcher("name"), Collections.<GraphQLArgument>emptyList(), null))
                                                                           .field(new GraphQLFieldDefinition(
                    "nickname", null, Scalars.INSTANCE.getGraphQLString(), new FieldDataFetcher("nickname"), Collections.<GraphQLArgument>emptyList(), null))
                                                                           .field(new GraphQLFieldDefinition(
                    "barkVolume", null, Scalars.INSTANCE.getGraphQLInt(), new FieldDataFetcher("barkVolume"), Collections.<GraphQLArgument>emptyList(), null))
                                                                           .field(new GraphQLFieldDefinition(
                    "doesKnowCommand", null, new GraphQLNonNull(Scalars.INSTANCE.getGraphQLBoolean()), new FieldDataFetcher("doesKnowCommand"),
                    Arrays.asList(dogCommandArg), null))
                                                                           .field(new GraphQLFieldDefinition(
                    "isHousetrained", null, Scalars.INSTANCE.getGraphQLBoolean(), new FieldDataFetcher("isHousetrained"),
                    Arrays.asList(atOtherHomesArg), null))
                                                                           .field(new GraphQLFieldDefinition(
                    "owner", null, human, new FieldDataFetcher("owner"), Collections.<GraphQLArgument>emptyList(), null))
                                                                           .withInterface(SpecValidationSchema.pet)
                                                                           .build();
            
    public static final GraphQLObjectType cat = GraphQLObjectType.Companion.newObject()
                                                                           .name("Cat")
                                                                           .field(new GraphQLFieldDefinition(
                    "name", null, new GraphQLNonNull(Scalars.INSTANCE.getGraphQLString()), new FieldDataFetcher("name"), Collections.<GraphQLArgument>emptyList(), null))
                                                                           .field(new GraphQLFieldDefinition(
                    "nickname", null, Scalars.INSTANCE.getGraphQLString(), new FieldDataFetcher("nickname"), Collections.<GraphQLArgument>emptyList(), null))
                                                                           .field(new GraphQLFieldDefinition(
                    "meowVolume", null, Scalars.INSTANCE.getGraphQLInt(), new FieldDataFetcher("meowVolume"), Collections.<GraphQLArgument>emptyList(), null))
                                                                           .field(new GraphQLFieldDefinition(
                    "doesKnowCommand", null, new GraphQLNonNull(Scalars.INSTANCE.getGraphQLBoolean()), new FieldDataFetcher("doesKnowCommand"),
                    Arrays.asList(catCommandArg), null))
                                                                           .withInterface(SpecValidationSchema.pet)
                                                                           .build();
            
    public static final GraphQLUnionType catOrDog = GraphQLUnionType.Companion.newUnionType()
                                                                              .name("CatOrDog")
                                                                              .possibleTypes(cat, dog)
                                                                              .typeResolver(new TypeResolver() {
            @Override
            public GraphQLObjectType getType(Object object) {
                if (object instanceof Cat) return cat;
                if (object instanceof Dog) return dog;
                return null;
            }})
                                                                              .build();
            
    public static final GraphQLUnionType dogOrHuman = GraphQLUnionType.Companion.newUnionType()
                                                                                .name("DogOrHuman")
                                                                                .possibleTypes(dog, human)
                                                                                .typeResolver(new TypeResolver() {
            @Override
            public GraphQLObjectType getType(Object object) {
                if (object instanceof Human) return human;
                if (object instanceof Dog) return dog;
                return null;
            }})
                                                                                .build();
            
    public static final GraphQLUnionType humanOrAlien = GraphQLUnionType.Companion.newUnionType()
                                                                                  .name("HumanOrAlien")
                                                                                  .possibleTypes(human, alien)
                                                                                  .typeResolver(new TypeResolver() {
            @Override
            public GraphQLObjectType getType(Object object) {
                if (object instanceof Human) return human;
                if (object instanceof Alien) return alien;
                return null;
            }})
                                                                                  .build();
    
    public static final GraphQLObjectType queryRoot = GraphQLObjectType.Companion.newObject()
                                                                                 .name("QueryRoot")
                                                                                 .field(new GraphQLFieldDefinition(
                    "dog", null, dog, new FieldDataFetcher("dog"), Collections.<GraphQLArgument>emptyList(), null))
                                                                                 .build();
            
    @SuppressWarnings("serial")
    public static final Set<GraphQLType> specValidationDictionary = new HashSet<GraphQLType>() {{
        add(dogCommand);
        add(catCommand);
        add(sentient);
        add(pet);
        add(human);
        add(alien);
        add(dog);
        add(cat);
        add(catOrDog);
        add(dogOrHuman);
        add(humanOrAlien);
    }};
    public static final GraphQLSchema specValidationSchema = GraphQLSchema.Companion.newSchema()
                                                                                    .query(queryRoot)
                                                                                    .build(specValidationDictionary);
    
    
}
