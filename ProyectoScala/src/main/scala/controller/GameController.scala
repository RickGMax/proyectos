package cl.uchile.dcc.controller

import cl.uchile.dcc.model.actions.{Action, Source, Target}
import cl.uchile.dcc.model.turns.TurnScheduler
import cl.uchile.dcc.model.units.GameUnit

/**
 * Coordinates battle turns and action execution.
 *
 * @param initialUnits the units participating in the battle
 */
class GameController(initialUnits: Seq[GameUnit]) {
  private val scheduler = new TurnScheduler
  private var activeUnit: Option[GameUnit] = None

  private val initialUnitStates: Seq[(GameUnit, () => Unit)] =
    initialUnits.distinct.map(unit => (unit, unit.captureState()))

  initialUnitStates.foreach { case (unit, _) =>
    scheduler.addUnit(unit)
  }

  /**
   * Returns the actions currently available to a source.
   *
   * @param source the entity whose actions are requested
   * @return the source's available actions
   */
  def availableActions(source: Source): Seq[Action] =
    source.availableActions

  /**
   * Executes a named action and advances the turn after success.
   *
   * @param source the unit performing the action
   * @param actionName the name of the action to execute
   * @param target the entity receiving the action
   * @throws IllegalArgumentException if the source is not active
   *                                  or the requested action is unavailable
   */
  def executeAction(
                     source: Source,
                     actionName: String,
                     target: Target
                   ): Unit = {
    require(
      currentUnit.contains(source),
      "Only the active unit can perform an action."
    )

    source.findAction(actionName) match {
      case Some(action) =>
        source.doAction(action, target)
        nextTurn()

      case None =>
        throw new IllegalArgumentException(
          s"Action '$actionName' is not available."
        )
    }
  }

  /**
   * Returns the unit currently playing its turn.
   *
   * @return the active unit, or None when no turn is active
   */
  def currentUnit: Option[GameUnit] =
    activeUnit

  /**
   * Removes defeated units and selects the next living unit.
   *
   * @return the next active unit, or None when no living units remain
   * @throws IllegalArgumentException if a living unit has invalid action bar values
   */
  def nextTurn(): Option[GameUnit] = {
    val registeredUnits = scheduler.units.toVector
    val livingUnits = registeredUnits.filter(_.lifePoints > 0.0)

    livingUnits.foreach { unit =>
      val maximum = unit.actionBarMaximum

      require(
        maximum.isFinite &&
          maximum > 0.0 &&
          unit.totalWeight > 0.0,
        "A unit must have positive total weight and a positive, finite action bar maximum"
      )
    }

    registeredUnits
      .filterNot(_.lifePoints > 0.0)
      .foreach(scheduler.removeUnit)

    activeUnit.foreach(scheduler.resetActionBar)
    activeUnit = None

    if (scheduler.units.nonEmpty) {
      while (scheduler.nextUnit.isEmpty) {
        scheduler.increaseActionBars(1.0)
      }

      activeUnit = scheduler.nextUnit
    }

    activeUnit
  }

  /** Restores the unit states captured when the controller was created. */
  def resetGame(): Unit = {
    activeUnit = None

    scheduler.units.toVector.foreach(scheduler.removeUnit)

    initialUnitStates.foreach { case (unit, restore) =>
      restore()
      scheduler.addUnit(unit)
    }
  }
}