package cl.uchile.dcc.model.turns

import scala.collection.mutable
import cl.uchile.dcc.model.units.GameUnit

/** Manages the action bars and turn order */
class TurnScheduler:

  /**Mutable LinkedHashMap preserves the order in which units were registered */
  private val actionBars =
    mutable.LinkedHashMap.empty[GameUnit,Double]

  /**Returns the registered units without exposing the mutable map */
  def units: Seq[GameUnit] =
    actionBars.keys.toSeq

  /**Registers a units with an initial action bar of 0.0 */
  def addUnit(unit:GameUnit):Unit =
    actionBars(unit) = 0.0

  /**Returns Some(currentBar) if the unit is registered, or None if it is not in the scheduler */
  def barAnalyze(unit:GameUnit):Option[Double] =
    actionBars.get(unit)

  /**Removes a unit and its action bar from the scheduler */
  def removeUnit(unit:GameUnit):Unit =
    actionBars.remove(unit)

  /**
   * Returns the registered unit's action bar maximum,
   * or None when the unit is not registered.
   */
  def maxActionBar(unit:GameUnit):Option[Double] =
    if actionBars.contains(unit) then
      Some(unit.actionBarMaximum)
    else
      None

  /**Increases every registered action bar by the given amount */
  def increaseActionBars(amount:Double):Unit =
    actionBars.keys.foreach:unit =>
      actionBars(unit) = actionBars(unit) + amount

  /**Resets the action bar of a registered unit */
  def resetActionBar(unit:GameUnit):Unit =
    if actionBars.contains(unit) then
      actionBars(unit) = 0.0

  /**
   * Returns whether a registered unit has reached its action bar maximum.
   * Returns false when the unit is not registered.
   */
  def hasCompletedBar(unit:GameUnit):Boolean =
    actionBars.get(unit) match {
      case Some(currentBar) =>
        currentBar >= unit.actionBarMaximum
      case None =>
        false

    }

  /** Calculates how much a unit exceeded its maximum action bar*/
  private def excess(unit:GameUnit):Double =
    actionBars(unit) - unit.actionBarMaximum

  /**Keeps only units with a completed bar and orders them from
   * greatest to small excess. The negative value reverses sortBy's
   * default ascending order*/
  def completedUnits:Seq[GameUnit] =
    actionBars.keys.filter(hasCompletedBar).toSeq.sortBy(unit => -excess(unit))

  /** Returns Some(unit) for the first completed unit,
   *  or None if no unit is ready*/
  def nextUnit: Option[GameUnit] =
    completedUnits.headOption
