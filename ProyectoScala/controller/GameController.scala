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
  //The scheduler manages action bars and the controller manages the active turn
  private val scheduler = new TurnScheduler
  private var activeUnit: Option[GameUnit] = None

  /**
   * Returns the actions currently available to a source.
   *
   * @param source the entity whose actions are requested
   * @return the source's available actions
   */
  def availableActions(source: Source): Seq[Action] =
    source.availableActions

  initialUnits.foreach(scheduler.addUnit)

  /**
   * Executes a named action for the active unit against a target.
   *
   * @param source     the unit performing the action
   * @param actionName the name of the action to execute
   * @param target     the entity receiving the action
   * @throws IllegalArgumentException if the source is not active
   *                                  or the requested action is unavailable
   */
  def executeAction(
                   source:Source,
                   actionName:String,
                   target:Target
                   ): Unit= {
    require(
      currentUnit.contains(source),
      "Only the active unit can perform an action."
    )
    source.findAction(actionName) match {
      case Some(action) =>
        source.doAction(action,target)

      case None =>
        throw new IllegalArgumentException(
          s"Action '$actionName' is not available"
        )
    }

  }

  /**
   * Returns the unit currently playing its turn
   *
   * @return Some unit when a turn is active,or None otherwise
   */
  def currentUnit: Option[GameUnit] =
    activeUnit


  /**
   * Ends the current turn and selects the next unit.
   *
   * @return the next active unit, or None when no units are registered
   */
  def nextTurn(): Option[GameUnit] = {
    // Validate before changing state to prevent an endless waiting loop
    scheduler.units.foreach { unit =>
      val maximum = unit.actionBarMaximum

      require(
        maximum.isFinite &&
          maximum > 0.0 &&
          unit.totalWeight > 0.0,
        "A unit must have positive total weight and a positive, finite action bar maximum"
      )
    }


    // Starting another turn ends the previous one and resets its bar.
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

  /**
   * Resets all registered action bars and clears the active unit.
   */
  def resetGame(): Unit ={
    scheduler.units.foreach(unit => scheduler.resetActionBar(unit))
    activeUnit = None

  }
}

