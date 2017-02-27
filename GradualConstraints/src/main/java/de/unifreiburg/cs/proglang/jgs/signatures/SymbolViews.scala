package de.unifreiburg.cs.proglang.jgs.signatures

import de.unifreiburg.cs.proglang.jgs.constraints.TypeViews.TypeView


/**
  * Created by fennell on 6/28/16.
  */
object SymbolViews {

  abstract sealed trait SymbolView[Level]
  sealed case class Param[Level](val position : Int) extends SymbolView[Level]
  sealed case class Return[Level]() extends SymbolView[Level]
  sealed case class Literal[Level](val level : TypeView[Level]) extends SymbolView[Level]

}
