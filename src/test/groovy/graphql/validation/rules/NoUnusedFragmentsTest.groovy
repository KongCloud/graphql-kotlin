package graphql.validation.rules

import graphql.language.Document
import graphql.parser.Parser
import graphql.validation.*
import spock.lang.Specification

class NoUnusedFragmentsTest extends Specification {

    IValidationContext validationContext = Mock(IValidationContext)
    ValidationErrorCollector errorCollector = new ValidationErrorCollector()
    NoUnusedFragments noUnusedFragments = new NoUnusedFragments(validationContext, errorCollector)

    def setup() {
        def traversalContext = Mock(ITraversalContext)
        validationContext.traversalContext >> traversalContext
    }

    def "all fragment names are used"() {
        given:
        def query = """
                {
                    human(id: 4) {
                        ...HumanFields1
                        ... on Human {
                            ...HumanFields2
                        }
                    }
                }
                fragment HumanFields1 on Human {
                    name
                    ...HumanFields3
                }
                fragment HumanFields2 on Human {
                    name
                }
                fragment HumanFields3 on Human {
                    name
                }
                """

        Document document = new Parser().parseDocument(query)
        LanguageTraversal languageTraversal = new LanguageTraversal();

        when:
        languageTraversal.traverse(document, new RulesVisitor(validationContext, [noUnusedFragments], false));

        then:
        errorCollector.errors().isEmpty()
    }

    def "all fragment names are used by multiple operations"() {
        def query = """
            query Foo {
                human(id: 4) {
                    ...HumanFields1
                }
            }
            query Bar {
                human(id: 4) {
                    ...HumanFields2
                }
            }
            fragment HumanFields1 on Human {
                name
                ...HumanFields3
            }
            fragment HumanFields2 on Human {
                name
            }
            fragment HumanFields3 on Human {
                name
            }
        """

        Document document = new Parser().parseDocument(query)
        LanguageTraversal languageTraversal = new LanguageTraversal();

        when:
        languageTraversal.traverse(document, new RulesVisitor(validationContext, [noUnusedFragments], false));

        then:
        errorCollector.errors().isEmpty()
    }


    def "contains unknown fragments"() {
        def query = """
                query Foo {
                    human(id: 4) {
                        ...HumanFields1
                    }
                }
                query Bar {
                    human(id: 4) {
                        ...HumanFields2
                    }
                }
                fragment HumanFields1 on Human {
                    name
                    ...HumanFields3
                }
                fragment HumanFields2 on Human {
                    name
                }
                fragment HumanFields3 on Human {
                    name
                }
                fragment Unused1 on Human {
                    name
                }
                fragment Unused2 on Human {
                    name
                }
                """

        Document document = new Parser().parseDocument(query)
        LanguageTraversal languageTraversal = new LanguageTraversal()

        when:
        languageTraversal.traverse(document, new RulesVisitor(validationContext, [noUnusedFragments], false));

        then:
        errorCollector.containsValidationError(ValidationErrorType.UnusedFragment)
        errorCollector.errors().size() == 2

    }

    def "contains unknown fragments with ref cycle"() {
        given:
        def query = """
        query Foo {
            human(id: 4) {
                ...HumanFields1
            }
        }
        query Bar {
            human(id: 4) {
                ...HumanFields2
            }
        }
        fragment HumanFields1 on Human {
            name
            ...HumanFields3
        }
        fragment HumanFields2 on Human {
            name
        }
        fragment HumanFields3 on Human {
            name
        }
        fragment Unused1 on Human {
            name
            ...Unused2
        }
        fragment Unused2 on Human {
            name
            ...Unused1
        }
        """

        Document document = new Parser().parseDocument(query)
        LanguageTraversal languageTraversal = new LanguageTraversal();

        when:
        languageTraversal.traverse(document, new RulesVisitor(validationContext, [noUnusedFragments], false));

        then:
        errorCollector.containsValidationError(ValidationErrorType.UnusedFragment)
        errorCollector.errors().size() == 2
    }
}