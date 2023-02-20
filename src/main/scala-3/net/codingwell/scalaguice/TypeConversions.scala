package net.codingwell.scalaguice

import com.google.inject.internal.MoreTypes.WildcardTypeImpl
import com.google.inject.util.{Types => GuiceTypes}
import java.lang.reflect.{Type => JavaType}
import scala.annotation.nowarn
import scala.compiletime.erasedValue
import scala.quoted._

/**
 * Copyright (C) 22/04/2018 - REstore NV
 */
private[scalaguice] object TypeConversions {
  inline def scalaTypeToJavaType[T]: JavaType = ${ // top-level splice == macro
    scalaTypeToJavaTypeImpl[T]
  }

  /*
   * Macro implementation, where we handle these --> and the others are reported as compiler errors.
   * TypeRepr (from https://scala-lang.org/api/3.x/scala/quoted/Quotes$reflectModule.html)

  -->         -+- NamedType -+- TermRef
  -->          |             +- TypeRef
               +- ConstantType
               +- SuperType
               +- Refinement
  -->          +- AppliedType
  -->          +- AnnotatedType
  -->          +- AndOrType -+- AndType
               |             +- OrType
               +- MatchType
  -->          +- ByNameType
               +- ParamRef
               +- ThisType
               +- RecursiveThis
               +- RecursiveType
               +- LambdaType -+- MethodOrPoly -+- MethodType
               |              |                +- PolyType
               |              +- TypeLambda
               +- MatchCase
  -->          +- TypeBounds
               +- NoPrefix
   */
  def scalaTypeToJavaTypeImpl[T: Type](using Quotes): Expr[JavaType] = {
    import quotes.reflect.*

    val nothingType: TypeRepr = TypeRepr.of[Nothing]
    val arrayType: TypeRepr = TypeRepr.of[Array]

    // Provides low-level translation of Scala type to a Java type expression
    // Since this is inlined, the classOf call is performed at the call site
    def toJavaType(scalaType: TypeRepr): Expr[JavaType] = scalaType.show match {
      // Special cases -- usually just where Scala aliases for Java types should just be Java types
      case "scala.Predef.String" => Expr(classOf[java.lang.String]) // strings
      case "scala.Throwable" => Expr(classOf[java.lang.Throwable]) // throwables
      case "scala.AnyRef" => Expr(classOf[java.lang.Object]) // Objects
      case "scala.Any" => Expr(classOf[java.lang.Object]) // Objects (mostly)
      case "scala.AnyVal" => Expr(classOf[java.lang.Object]) // Objects (kind of)

      // A couple of extra-special cases
      case "scala.Unit" => Expr(classOf[scala.runtime.BoxedUnit]) // units are boxed
      case "scala.Nothing" => Expr(classOf[scala.runtime.Nothing$]) // nothing's special

      // General case -- everything else is resolved as a class through the magic of generic type matching
      case _ => scalaType.asType match {
        case '[t] => Ref(defn.Predef_classOf).appliedToType(scalaType).asExprOf[Class[t]]
        case other => report.errorAndAbort(s"Unable to use type $other")
      }
    }

    // Recursively evaluate Scala type representations as higher-level Java type expressions
    def javaTypeExpr(scalaType: TypeRepr, allowPrimitive: Boolean = false): Expr[JavaType] =
      scalaType.dealias match {

        // For example, String is TypeRef(
        //   ThisType(TypeRef(NoPrefix,module class lang)),class String
        // )
        case ref: TypeRef =>
          // Only allow primitive references in array definitions, otherwise they are boxed
          if (allowPrimitive) ref.show match {
            case "scala.Byte" | "scala.Predef.Byte" => Expr(java.lang.Byte.TYPE) // B
            case "scala.Short" | "scala.Predef.Short" => Expr(java.lang.Short.TYPE) // S
            case "scala.Char" | "scala.Predef.Char" => Expr(java.lang.Character.TYPE) // C
            case "scala.Int" | "scala.Predef.Int" => Expr(java.lang.Integer.TYPE) // I
            case "scala.Long" | "scala.Predef.Long" => Expr(java.lang.Long.TYPE) // J
            case "scala.Float" | "scala.Predef.Float" => Expr(java.lang.Float.TYPE) // F
            case "scala.Double" | "scala.Predef.Double" => Expr(java.lang.Double.TYPE) // D
            case "scala.Boolean" | "scala.Predef.Boolean" => Expr(java.lang.Boolean.TYPE) // Z
            case "scala.Null" | "scala.Predef.Null" => Expr(java.lang.Void.TYPE) // null
            case _ => toJavaType(ref)
          }
          else ref.show match {
            case "scala.Byte" | "scala.Predef.Byte" => Expr(classOf[java.lang.Byte]) // B
            case "scala.Short" | "scala.Predef.Short" => Expr(classOf[java.lang.Short]) // S
            case "scala.Char" | "scala.Predef.Char" => Expr(classOf[java.lang.Character]) // C
            case "scala.Int" | "scala.Predef.Int" => Expr(classOf[java.lang.Integer]) // I
            case "scala.Long" | "scala.Predef.Long" => Expr(classOf[java.lang.Long]) // J
            case "scala.Float" | "scala.Predef.Float" => Expr(classOf[java.lang.Float]) // F
            case "scala.Double" | "scala.Predef.Double" => Expr(classOf[java.lang.Double]) // D
            case "scala.Boolean" | "scala.Predef.Boolean" => Expr(classOf[java.lang.Boolean]) // Z
            case "scala.Null" | "scala.Predef.Null" => Expr(classOf[java.lang.Void]) // null
            case _ => toJavaType(ref)
          }

        // For example, List[String] is AppliedType(
        //  TypeRef(ThisType(TypeRef(NoPrefix,module class immutable)),class List),
        //  List( TypeRef(TermRef(ThisType(TypeRef(NoPrefix,module class scala)),object Predef),type String) )
        // )
        case fullType@AppliedType(classType: TypeRepr, paramTypes: List[TypeRepr]) =>
          if (classType =:= arrayType) {
            val paramJavaType: Expr[JavaType] = javaTypeExpr(paramTypes.head, allowPrimitive = true)
            '{ GuiceTypes.arrayOf($paramJavaType) }
          } else {
            val rawJavaType: Expr[JavaType] = toJavaType(fullType)
            val paramJavaTypes: Expr[Seq[JavaType]] = Expr.ofSeq(paramTypes.map(javaTypeExpr(_)))
            findOwnerOf(classType) match {
              case Some(ownerJavaType) =>
                '{ GuiceTypes.newParameterizedTypeWithOwner($ownerJavaType, $rawJavaType, $paramJavaTypes *) }
              case None =>
                '{ GuiceTypes.newParameterizedType($rawJavaType, $paramJavaTypes *) }
            }
          }

        // For example, the wildcard type bounds part of List[_] is TypeBounds(
        //   TypeRef(ThisType(TypeRef(NoPrefix,module class scala)),class Nothing),
        //   TypeRef(ThisType(TypeRef(NoPrefix,module class scala)),class Any)
        // )
        case TypeBounds(lowerBound: TypeRepr, upperBound: TypeRepr) =>
          val lowerBoundsExpr = Expr.ofSeq(collectBoundExprs(lowerBound, exclude = Some(nothingType)))
          val upperBoundsExpr = Expr.ofSeq(collectBoundExprs(upperBound))
          '{ new WildcardTypeImpl($upperBoundsExpr.toArray, $lowerBoundsExpr.toArray) }

        // For example, the LHS of (=> Unit) => String is ByNameType(
        //  TypeRef(TermRef(ThisType(TypeRef(NoPrefix,module class <root>)),object scala),class Unit)
        // )
        // where "ByNameType" is shown as "ExprType" in debug output
        case ByNameType(underlying: TypeRepr) => javaTypeExpr(underlying)

        // For example, SomeClazz with Augmentation is AndType(
        //   TypeRef(ThisType(TypeRef(NoPrefix,module class russ)),class SomeClazz),
        //   TypeRef(ThisType(TypeRef(NoPrefix,module class russ)),trait Augmentation)
        // )
        case AndType(firstType: TypeRepr, _) => javaTypeExpr(firstType)

        // Ignore annotations on types
        case AnnotatedType(underlying, _) => javaTypeExpr(underlying)

        // These generally happen when you bind[Something.type]
        case ref: TermRef => toJavaType(ref)

        /*
         * Unsupported (at least for now)
         */

        // These are not fully-applied types, so not legal as a type parameter
        case _: LambdaType => notSupported("Type lambdas and their ilk")
        case _: ParamRef => notSupported("Type parameter references")
        case _: MatchCase | _: MatchType => notSupported("Match cases as types")

        // Unclear how these would ever get passed in as type parameters
        case _: SuperType => notSupported("Super type references")
        case _: RecursiveType | _: RecursiveThis => notSupported("Recursive types")

        // Shouldn't appear at the top level unless something has gone wrong
        case _: NoPrefix | _: ThisType => notSupported("Root no prefix and this types")

        // Doesn't make sense to use DI for a constant type
        // For example (from tests), typeLiteral[1]
        // Guice arrives at "java.lang.Object" as the type (too broad to be useful)
        case _: ConstantType => notSupported("Constant types")

        // Legal as a type parameter, but unclear how to support it
        // For example (from tests), typeLiteral[Int { type U }]
        // Guice arrives at "java.lang.Object" as the type (too broad to be useful)
        case _: Refinement => notSupported("Refinement types")

        // Unclear how this could be used
        // For example (from tests), typeLiteral[B | C]
        // Guice arrives at "java.lang.Object" as the type (too broad to be useful)
        case _: OrType => notSupported("Or types")
      }

    // Recursively evaluate and collect Scala type representations as higher-level Java type expressions
    // Only handling the "and" case right (e.g., A with B with C)
    def collectBoundExprs(scalaType: TypeRepr, exclude: Option[TypeRepr] = None): Seq[Expr[JavaType]] =
      (scalaType.dealias, exclude) match {
        case (ref: TypeRef, Some(excludedType)) if ref =:= excludedType =>
          Seq.empty

        case (AndType(firstType, secondType), _) =>
          collectBoundExprs(firstType, exclude) ++ collectBoundExprs(secondType, exclude)

        case (other: TypeRepr, _) =>
          Seq(javaTypeExpr(other))
      }

    // Anything we don't handle directly above returns a compiler error
    def notSupported(theseTypes: String): Expr[JavaType] = {
      report.errorAndAbort(s"$theseTypes are not supported")
    }

    def findOwnerOf(ref: TypeRepr): Option[Expr[JavaType]] = {
      val owner = ref.typeSymbol.maybeOwner
      val hasOwnerClass = owner.isClassDef && !owner.isNoSymbol && !owner.isPackageDef
      if (hasOwnerClass) {
        if (owner.flags.is(Flags.Module)) {
          // Owner is a module (object)
          // Example from tests: net.codingwell.scalaguice.Outer (object) is owner of Gen (trait)
          // Would prefer to use toJavaType(owner.typeRef) call always (without stringification), but...
          // here we need to resolve class without the $ suffix for some reason
          // Unknown if there is a better way than using ".typeRef.show" to get the full class name for these
          val clazz: Expr[String] = Expr(owner.typeRef.show)
          Some('{ Class.forName($clazz) })
        } else {
          // Owner is not a module (class, trait)
          // Example from tests: net.codingwell.scalaguice.ScalaMapBinderSpec (class) is owner of W (class)
          Some(toJavaType(owner.typeRef))
        }
      } else None
    }

    // process recursively
    javaTypeExpr(TypeRepr.of[T])
  }
}
