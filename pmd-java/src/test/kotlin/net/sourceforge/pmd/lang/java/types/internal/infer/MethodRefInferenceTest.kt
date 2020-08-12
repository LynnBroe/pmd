/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.types.internal.infer

import io.kotest.matchers.shouldBe
import net.sourceforge.pmd.lang.ast.test.shouldBe
import net.sourceforge.pmd.lang.ast.test.shouldMatchN
import net.sourceforge.pmd.lang.java.ast.*
import net.sourceforge.pmd.lang.java.types.JPrimitiveType
import net.sourceforge.pmd.lang.java.types.lub
import net.sourceforge.pmd.lang.java.types.shouldMatchMethod
import net.sourceforge.pmd.lang.java.types.testdata.TypeInferenceTestCases
import net.sourceforge.pmd.lang.java.types.typeDsl
import java.util.function.BiFunction
import java.util.function.BinaryOperator
import java.util.function.Consumer

/**
 *
 */
class MethodRefInferenceTest : ProcessorTestSpec({


    parserTest("Test inexact method ref of generic type") {

        val acu = parser.parse("""
            import java.util.Optional;
            import java.util.List;
            import java.util.stream.Stream;
            import java.util.Objects;

            class Archive {

                private static void loopChecks2(List<Archive> step, List<Archive> pred, List<Archive> fini) {
                    Stream.of(step, pred, fini).flatMap(List::stream).filter(Objects::nonNull).anyMatch(it -> true);
                }

            }
        """.trimIndent())


        val t_Archive = acu.descendants(ASTClassOrInterfaceDeclaration::class.java).firstOrThrow().typeMirror
        val anyMatch = acu.descendants(ASTMethodCall::class.java).first()!!

        anyMatch.shouldMatchN {
            methodCall("anyMatch") {
                it::getTypeMirror shouldBe it.typeSystem.BOOLEAN

                methodCall("filter") {

                    it::getTypeMirror shouldBe with (it.typeDsl) { gen.t_Stream[t_Archive] }

                    methodCall("flatMap") {

                        it::getTypeMirror shouldBe with (it.typeDsl) { gen.t_Stream[t_Archive] }

                        methodCall("of") {
                            it::getQualifier shouldBe unspecifiedChild()

                            it::getTypeMirror shouldBe with (it.typeDsl) { gen.t_Stream[gen.t_List[t_Archive]] }

                            argList(3)
                        }

                        argList(1)
                    }

                    argList(1)
                }

                argList(1)
            }
        }
    }



    parserTest("Test call chain with method reference") {

        asIfIn(TypeInferenceTestCases::class.java)

        val chain = "Stream.of(\"\").map(String::isEmpty).collect(Collectors.toList())"

        inContext(ExpressionParsingCtx) {


            chain should parseAs {
                methodCall("collect") {
                    it.typeMirror shouldBe with(it.typeDsl) { gen.t_List[boolean.box()] }
                    it::getQualifier shouldBe child<ASTMethodCall> {
                        it::getMethodName shouldBe "map"
                        it.typeMirror shouldBe with(it.typeDsl) { gen.t_Stream[boolean.box()] }
                        it::getQualifier shouldBe child<ASTMethodCall> {
                            it::getMethodName shouldBe "of"
                            it.typeMirror shouldBe with(it.typeDsl) { gen.t_Stream[gen.t_String] }
                            it::getQualifier shouldBe typeExpr {
                                classType("Stream")
                            }

                            it::getArguments shouldBe child {
                                stringLit("\"\"")
                            }
                        }

                        it::getArguments shouldBe child {

                            methodRef("isEmpty") {

                                with(it.typeDsl) {
                                    val `t_Function{String, Boolean}` = gen.t_Function[gen.t_String, boolean.box()]

                                    it.typeMirror shouldBe `t_Function{String, Boolean}`
                                    it.referencedMethod.shouldMatchMethod(named = "isEmpty", declaredIn = gen.t_String, withFormals = emptyList(), returning = boolean)
                                    // Function<String, Boolean>.apply(String) -> Boolean
                                    it.functionalMethod.shouldMatchMethod(named = "apply", declaredIn = `t_Function{String, Boolean}`, withFormals = listOf(gen.t_String), returning = boolean.box())
                                }

                                typeExpr {
                                    classType("String")
                                }
                            }
                        }
                    }
                    it::getArguments shouldBe child {
                        unspecifiedChild()
                    }
                }
            }
        }
    }

    parserTest("Test call chain with constructor reference") {

        asIfIn(TypeInferenceTestCases::class.java)

        val chain = "Stream.of(1, 2).map(int[]::new).collect(Collectors.toList())"

        inContext(ExpressionParsingCtx) {

            chain should parseAs {
                methodCall("collect") {

                    it.typeMirror shouldBe with (it.typeDsl) { gen.t_List[int.toArray() ]} // List<int[]>

                    it::getQualifier shouldBe methodCall("map") {
                        it.typeMirror shouldBe with (it.typeDsl) { gen.t_Stream[int.toArray() ]} // Stream<int[]>

                        it::getQualifier shouldBe methodCall("of") {
                            it.typeMirror shouldBe with (it.typeDsl) { gen.t_Stream[int.box()]} // Stream<Integer>

                            it::getQualifier shouldBe typeExpr {
                                classType("Stream")
                            }

                            it::getArguments shouldBe child {
                                int(1)
                                int(2)
                            }
                        }

                        it::getArguments shouldBe child {

                            // Function<Integer, int[]>
                            val `t_Function{Integer, Array{int}}` = with(it.typeDsl) { gen.t_Function[int.box(), int.toArray()] }

                            constructorRef {
                                it.typeMirror shouldBe `t_Function{Integer, Array{int}}`
                                with(it.typeDsl) {
                                    it.referencedMethod.shouldMatchMethod(named = "new", declaredIn = int.toArray(), /* int[]*/ withFormals = listOf(int), returning = int.toArray())
                                    it.functionalMethod.shouldMatchMethod(named = "apply", declaredIn = `t_Function{Integer, Array{int}}`, withFormals = listOf(int.box()), returning = int.toArray())
                                }

                                typeExpr {
                                    arrayType {
                                        primitiveType(JPrimitiveType.PrimitiveTypeKind.INT)
                                        arrayDimList()
                                    }
                                }
                            }
                        }
                    }
                    it::getArguments shouldBe child {
                        unspecifiedChild()
                    }
                }
            }
        }
    }


    parserTest("Test call chain with array method reference") {

        asIfIn(TypeInferenceTestCases::class.java)

        val chain = "Stream.<int[]>of(new int[0]).map(int[]::clone)"


        inContext(ExpressionParsingCtx) {
            chain should parseAs {
                methodCall("map") {

                    it.typeMirror shouldBe with(it.typeDsl) { gen.t_Stream[int.toArray()] }

                    it::getQualifier shouldBe methodCall("of") {
                        it.typeMirror shouldBe with(it.typeDsl) { gen.t_Stream[int.toArray()] }

                        it::getQualifier shouldBe typeExpr {
                            classType("Stream")
                        }

                        it::getExplicitTypeArguments shouldBe child {
                            arrayType()
                        }

                        it::getArguments shouldBe child {
                            unspecifiedChild()
                        }
                    }

                    it::getArguments shouldBe argList {

                        methodRef("clone") {

                            with(it.typeDsl) {
                                // Function<int[], int[]>
                                val `t_Function{Array{int}, Array{int}}` = gen.t_Function[int.toArray(), int.toArray()]

                                it.typeMirror shouldBe `t_Function{Array{int}, Array{int}}`
                                it.referencedMethod.shouldMatchMethod(named = "clone", declaredIn = int.toArray(), withFormals = emptyList(), returning = int.toArray())
                                it.functionalMethod.shouldMatchMethod(named = "apply", declaredIn = `t_Function{Array{int}, Array{int}}`, withFormals = listOf(int.toArray()), returning = int.toArray())
                            }

                            typeExpr {
                                arrayType {
                                    primitiveType(JPrimitiveType.PrimitiveTypeKind.INT)
                                    arrayDimList()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    parserTest("Test method reference overload resolution") {

        asIfIn(TypeInferenceTestCases::class.java)
        val stringBuilder = "java.lang.StringBuilder"

        val chain = "Stream.of(\"\", 4).reduce(new $stringBuilder(), $stringBuilder::append, $stringBuilder::append)"

        inContext(ExpressionParsingCtx) {


            chain should parseAs {
                methodCall("reduce") {


                    // we can't hardcode the lub because it is jdk specific
                    val serialLub = with (it.typeDsl) {
                        ts.lub(gen.t_String, gen.t_Integer)
                    }

                    val t_BiFunction = with (it.typeDsl) { ts.getClassSymbol(BiFunction::class.java)!! }
                    val t_BinaryOperator = with (it.typeDsl) { ts.getClassSymbol(BinaryOperator::class.java)!! }
                    val t_Sb = with (it.typeDsl) { gen.t_StringBuilder }

                    with (it.typeDsl) {

                        it.typeMirror shouldBe t_Sb
                        it.methodType.shouldMatchMethod(
                                named = "reduce",
                                declaredIn = gen.t_Stream[serialLub],
                                withFormals = listOf(
                                        t_Sb,
                                        t_BiFunction[t_Sb, `?` `super` serialLub, t_Sb],
                                        t_BinaryOperator[t_Sb]
                                ),
                                returning = t_Sb
                        )
                    }

                    it::getQualifier shouldBe child<ASTMethodCall> {
                        it::getMethodName shouldBe "of"
                        it.typeMirror shouldBe with(it.typeDsl) { gen.t_Stream[serialLub] }

                        it::getQualifier shouldBe typeExpr {
                            classType("Stream")
                        }

                        it::getArguments shouldBe child {
                            unspecifiedChildren(2)
                        }
                    }

                    it::getArguments shouldBe child {
                        child<ASTConstructorCall>(ignoreChildren = true) {
                        }

                        methodRef("append") {
                            with (it.typeDsl) {
                                val myBifunction = t_BiFunction[t_Sb, serialLub, t_Sb]

                                it.typeMirror shouldBe myBifunction
                                it.referencedMethod.shouldMatchMethod(named = "append", declaredIn = t_Sb, withFormals = listOf(ts.OBJECT), returning = t_Sb)
                                it.functionalMethod.shouldMatchMethod(named = "apply", declaredIn = myBifunction, withFormals = listOf(t_Sb, serialLub), returning = t_Sb)
                            }

                            typeExpr {
                                qualClassType(stringBuilder)
                            }
                        }

                        methodRef("append") {

                            with (it.typeDsl) {
                                val myBifunction = t_BiFunction[t_Sb, t_Sb, t_Sb]

                                it.typeMirror shouldBe t_BinaryOperator[t_Sb]
                                // notice it's more specific than the first append (CharSequence formal)
                                it.referencedMethod.shouldMatchMethod(named = "append", declaredIn = t_Sb, withFormals = listOf(gen.t_CharSequence), returning = t_Sb)
                                // notice the owner of the function is BiFunction and not BinaryOperator. It's inherited by BinaryOperator
                                it.functionalMethod.shouldMatchMethod(named = "apply", declaredIn = myBifunction, withFormals = listOf(t_Sb, t_Sb), returning = t_Sb)
                            }

                            typeExpr {
                                qualClassType(stringBuilder)
                            }
                        }
                    }
                }
            }
        }
    }



    parserTest("Test method ref with this as LHS") {


        val acu = parser.parse("""
            
            package scratch;

            import static java.util.stream.Collectors.joining;

            import java.util.Comparator;
            import java.util.Deque;

            class Archive {
                
                private String getName() {
                    return "foo";
                }

                private String toInversePath(Deque<Archive> path) {
                    return path.stream()
                               .map(Archive::getName)
                               .collect(joining(" <- "));
                }

                private Comparator<Deque<Archive>> comparator() {
                    return Comparator.<Deque<Archive>, String>
                        comparing(deque -> deque.peekFirst().getName())
                        .thenComparingInt(Deque::size)
                        .thenComparing(this::toInversePath);
                }

            }
        """.trimIndent())

        val thisToInversePath = acu.descendants(ASTMethodReference::class.java)[2]!!

        thisToInversePath.shouldMatchN {
            methodRef("toInversePath") {
                it.functionalMethod.toString() shouldBe "java.util.function.Function<java.util.Deque<scratch.Archive>, java.lang.String>.apply(java.util.Deque<scratch.Archive>) -> java.lang.String"
                thisExpr()
            }
        }
    }

    parserTest("Test method ref with void return type") {


        val acu = parser.parse("""
            import java.util.Optional;
            class Archive {

                private String getName() {
                    Optional.of(this).ifPresent(Archive::getName);
                    return "foo";
                }
            }
        """.trimIndent())


        val t_Archive = acu.descendants(ASTClassOrInterfaceDeclaration::class.java).firstOrThrow().typeMirror
        val getName = acu.descendants(ASTMethodDeclaration::class.java).first()!!
        val thisToInversePath = acu.descendants(ASTMethodCall::class.java).first()!!

        thisToInversePath.shouldMatchN {
            methodCall("ifPresent") {
                unspecifiedChild()
                argList {
                    methodRef("getName") {
                        with (it.typeDsl) {
                            it.functionalMethod.shouldMatchMethod(
                                    named = "accept",
                                    declaredIn = Consumer::class[t_Archive],
                                    withFormals = listOf(t_Archive),
                                    returning = ts.NO_TYPE
                            )

                            it.referencedMethod.symbol shouldBe getName.symbol
                        }

                        typeExpr {
                            classType("Archive")
                        }
                    }
                }
            }
        }
    }

    parserTest("Test inexact method ref conflict between static and non-static") {

        val acu = parser.parse("""
            import java.util.stream.*;
            class Archive {

                private String getName(int[] certIds) {
                    return IntStream.of(certIds)
                            // both static Integer::toString(int) and non-static Integer::toString() are applicable
                            .mapToObj(Integer::toString)
                            .collect(Collectors.joining(", "));
                }
            }
        """.trimIndent())

        val collectCall = acu.descendants(ASTMethodCall::class.java).first()!!

        collectCall.shouldMatchN {
            methodCall("collect") {
                with (it.typeDsl) {
                    it.typeMirror shouldBe gen.t_String
                }

                methodCall("mapToObj") {
                    with (it.typeDsl) {
                        it.typeMirror shouldBe gen.t_Stream[gen.t_String]
                    }

                    unspecifiedChildren(2)
                }

                argList(1)
            }
        }
    }



})
