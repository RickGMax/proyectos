package cl.uchile.dcc.model.actions
import cl.uchile.dcc.model.items.Usable


/** Base implementation for actions involving usable items. */
abstract class UsableAction(
                             name: String,
                             initialPossibleUsables: Array[Usable]
                           ) extends Action(name) {

  private val storedPossibleUsables: Array[Usable] =
    initialPossibleUsables.clone()

  /** Returns a copy of the items available to this action. */
  def possibleUsables: Array[Usable] = storedPossibleUsables.clone()
}