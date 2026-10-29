package cl.uchile.dcc.model.actions

/**
 * Represents an entity capable of initiating actions.
 */
trait Source {
  /** Returns the source's name. */
  def name: String

  /** Returns the actions currently available to this source. */
  def availableActions: Seq[Action]

  /**
   * Finds an available action by its name.
   *
   * @param actionName the action's identifier
   *
   * @return the matching action, or None when unavailable
   */
  def findAction(actionName: String): Option[Action] =
    availableActions.find(_.name == actionName)

  /**
   * Executes an action against a target.
   *
   * @param action the action to perform
   * @param target the entity receiving the action
   */
  def doAction(action: Action, target: Target): Unit
}

