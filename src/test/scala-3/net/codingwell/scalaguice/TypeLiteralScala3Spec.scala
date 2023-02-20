/*
 *  Copyright 2010-2014 Benjamin Lings
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.codingwell.scalaguice

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class TypeLiteralScala3Spec extends AnyFunSpec with Matchers {

  import com.google.inject._

  /*
   * These are all syntactically correct, but macro should report compiler error.
   */
  describe("type literal creation unsupported for some Scala 3 types") {
    it("Constant types are not supported") {
      assertTypeError("typeLiteral[1]")
    }

    it("Refinement types are not supported") {
      assertTypeError("typeLiteral[Int { type U }]")
    }

    it("Or types are not supported") {
      assertTypeError("typeLiteral[B | C]")
    }
  }
}
