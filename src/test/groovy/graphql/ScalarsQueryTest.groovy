package graphql

import graphql.execution.batched.BatchedExecutionStrategy
import spock.lang.Specification
import spock.lang.Unroll

import java.util.concurrent.ExecutionException;

class ScalarsQueryTest extends Specification {

    def 'Large BigIntegers'() {
        given:
        def query = """
        query BigInteger {
          bigInteger
          i1: bigIntegerInput(input: 1234567890123456789012345678901234567890)
          i2: bigIntegerInput(input: "1234567890123456789012345678901234567890")
          i3: bigIntegerString(input: "1234567890123456789012345678901234567890")
        }
        """
        def expected = [
                bigInteger: 9999,
                i1: 1234567890123456789012345678901234567890,
                i2: 1234567890123456789012345678901234567890,
                i3: 1234567890123456789012345678901234567890
        ]

        when:
        def result = new GraphQL(ScalarsQuerySchema.scalarsQuerySchema).execute(query).get()

        then:
        result.data == expected
        result.errors.empty == true
    }
    
    def 'Large BigDecimals'() {
        given:
        def query = """
        query BigDecimal {
          bigDecimal
          d1: bigDecimalInput(input: "1234567890123456789012345678901234567890.0")
          d2: bigDecimalInput(input: 1234567890123456789012345678901234567890.0)
          d3: bigDecimalString(input: "1234567890123456789012345678901234567890.0")
        }
        """
        def expected = [
                bigDecimal: 1234.0,
                d1: 1234567890123456789012345678901234567890.0,
                d2: 1234567890123456789012345678901234567890.0,
                d3: 1234567890123456789012345678901234567890.0,
        ]

        when:
        def result = new GraphQL(ScalarsQuerySchema.scalarsQuerySchema).execute(query).get()

        then:
        result.data == expected
        result.errors.empty == true
    }

    def 'Float NaN Not a Number '() {
        given:
        def query = """
        query FloatNaN {
          floatNaN
        }
        """
        def expected = [
                floatNaN: null
        ]

        when:
        def result = new GraphQL(ScalarsQuerySchema.scalarsQuerySchema).execute(query).get()
        def resultBatched = new GraphQL(ScalarsQuerySchema.scalarsQuerySchema, new BatchedExecutionStrategy()).execute(query).get()

        then:
        result.data == expected
        result.errors.empty
        resultBatched.data == expected
        resultBatched.errors.empty
    }

    def 'Escaped characters are handled'() {
        given:
        def query = """
        query {
          stringInput(input: "test \\" \\/ \\b \\f \\n \\r \\t \\u12Aa")
        }
        """
        def expected = [
                stringInput: "test \" / \b \f \n \r \t \u12Aa",
        ]

        when:
        def result = new GraphQL(ScalarsQuerySchema.scalarsQuerySchema).execute(query).get()

        then:
        result.data == expected
        result.errors.empty == true
    }
    
    @Unroll
    def "FooBar String cannot be cast to #number"() {
        given:
        def query = "{ " + number + "String(input: \"foobar\") }"
        
        when:
        def result = new GraphQL(ScalarsQuerySchema.scalarsQuerySchema).execute(query).get()
        
        then:
        //FIXME do not propagate exception, but instead raise an error.
        // TODO Check ExecutionException contains NumberFormatException
        thrown(ExecutionException)
        //TODO result.errors.empty == false
        //TODO result.errors == xyz
        
        where:
        number       | _
        "bigInteger" | _
        "bigDecimal" | _
        "float"      | _
        "long"       | _
        "int"        | _
        "short"      | _
        "byte"       | _
    }
}