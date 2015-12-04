/* Copyright 2015 UniCredit S.p.A.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package eu.unicredit.swagger.generators

import sbt._
import eu.unicredit.swagger.{SwaggerConversion, StringUtils}

import treehugger.forest._
import definitions._
import treehuggerDSL._

import io.swagger.parser.SwaggerParser
import io.swagger.models.properties._
import io.swagger.models._
import io.swagger.models.parameters._
import scala.collection.JavaConversions._

object DefaultPlay {
  val version = "2.4.4"
}

object DefaultModelGenerator {
  def dependencies: Seq[sbt.ModuleID] = Seq()
}

class DefaultModelGenerator extends ModelGenerator with SwaggerConversion {

  def generateClass(name: String, props: Iterable[(String, Property)], comments: Option[String]): String = {
    val GenClass = RootClass.newClass(name)

    val params: Iterable[ValDef] = for ((pname, prop) <- props) yield PARAM(pname, propType(prop, true)): ValDef

    val tree: Tree = CLASSDEF(GenClass) withFlags Flags.CASE withParams params

    val resTree =
      comments.map(tree withComment _).getOrElse(tree)

    treeToString(resTree)
  }

  def generateModelInit(packageName: String): String = {
    //val initTree =
      //PACKAGE(packageName)

    //treeToString(initTree)
    "package "+packageName
  }

  def generate(fileName: String, destPackage: String): Iterable[SyntaxString] = {
    val swagger = new SwaggerParser().read(fileName)

    val models = swagger.getDefinitions

    val modelss =
      for {
        (name, model) <- models
        description = model.getDescription
        properties = model.getProperties
      } yield SyntaxString(name, generateModelInit(destPackage), generateClass(name, properties, Option(description)))

    modelss
  }
}