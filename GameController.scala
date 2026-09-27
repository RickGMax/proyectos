package cl.uchile.dcc
package controller
import scala.collection.mutable

/** character classes available */
enum Clase:
  case Knight
  case BlackMagician
  case WhiteMagician
  case Archer
  case Thief

enum Typeweapon:     /** weapons types available   */
  case Sword
  case Dagger
  case Bow
  case Wand
  case Staff

enum Typepotion:  /** potion types available  */
  case Heal
  case Strength
  case Mana
  case MagicForce

trait Usable:
  val name:String

class Potion(val name: String,
             val types: Typepotion
            ) extends Usable

class Weapon(val name: String,
             var attackpoints: Int,
             var weight: Double,
             val owner: Character,
             val types: Typeweapon
            ) extends Usable

class Magicweapon(name: String,
                  attackpoints: Int,
                  var magicattack: Int,
                  types: Typeweapon,
                  weight: Double,
                  owner: Character) extends Weapon(name, attackpoints, weight, owner,types
)
/** common type into characters and enemies  */
abstract class Unity(
                       val name:String,
                       var lifepoints:Double,
                       var defense:Int,
                       var weight:Double
                     ) {
  def totalWeight: Double = weight   /**Weight used to calculate the maximum action bar   */
}
/** common type into characters and enemies  */
class Character(name:String,
               lifepoints:Double,
               defense:Int ,
               weight:Double,
               val classes:Clase,
               var weapon:Option[Weapon], //option represents an equipped weapon or none
               var inventory: Array[Usable]  //
               ) extends Unity(name, lifepoints,defense,weight){
  override def totalWeight:Double =
    weapon match
      case Some(equippedWeapon) =>
        weight + equippedWeapon.weight

      case None =>
        weight
}


class Magic_character(name:String,
                      lifepoints:Double,
                      defense:Int,
                      weight:Double,
                      classes:Clase,
                      weapon:Option[Weapon],
                      val manapoints:Double,
                      inventory: Array[Usable]
                    ) extends Character(name,lifepoints,defense,weight,classes,weapon,inventory)

class Enemy(name:String,
            lifepoints:Double,
            var attackpoints:Int,
            defense:Int,
            weight:Double
           ) extends Unity(name, lifepoints, defense, weight)




class Player(var defeated: Boolean, /**Is a constructor for a player in game   */
             var units: Array[Unity])


class Panel(     /**Represents a map panel and unit positions */
           val x: Int,
           val y: Int,
           var units:Array[Unity],
           var adjacent:Array[Panel]
           )

/**Common abstraction for every available action */
abstract class Action(
                     val name: String
                     )

/**Base class for actions on usable objects */
abstract class UsableAction(
                           name:String,
                           val possibleUsables: Array[Usable]
                           ) extends Action(name)

class Attack extends Action("Attack")

class Move extends Action("Move")

class EquipWeapon(
                 possibleUsables: Array[Usable]
                 ) extends UsableAction("Equip a weapon",possibleUsables)


class ConsumePotion(
                   possibleUsables: Array[Usable]
                   ) extends UsableAction("Consume a potion",possibleUsables)

class Thunder extends Action("Thunder")

class Meteorite extends Action("Meteorite")

class Healing extends Action("Healing")

class Purification extends Action("Purification")


/** Manages the action bars and turn order */
class TurnScheduler:

  /**Mutable LinkedHashMap preserves the order in which units were registered */
  private val actionBars =
    mutable.LinkedHashMap.empty[Unity,Double]

  /**Returns the registered units without exposing the mutable map */
  def units: Seq[Unity] =
    actionBars.keys.toSeq

  /**Registers a units with an initial action bar of 0.0 */
  def addUnit(unit:Unity):Unit =
    actionBars(unit) = 0.0

  /**Returns Some(currentBar) if the unit is registered, or None if it is not in the scheduler */
  def barAnalyze(unit:Unity):Option[Double] =
    actionBars.get(unit)

  /**Removes a unit and its action bar from the scheduler */
  def removeUnit(unit:Unity):Unit =
    actionBars.remove(unit)

  /**Returns the maximum action bar calculated from the unit's total weight */
  def maxActionBar(unit:Unity):Option[Double] =
    if actionBars.contains(unit) then
      Some(unit.totalWeight)
    else
      None

  /**Increases every registered action bar by the given amount */
  def increaseActionBars(amount:Double):Unit =
    actionBars.keys.foreach:unit =>
      actionBars(unit) = actionBars(unit) + amount

  /**Resets the action bar of a registered unit */
  def resetActionBar(unit:Unity):Unit =
    if actionBars.contains(unit) then
      actionBars(unit) = 0.0

  /**Returns True if the units action bar is greater than or equals to its total weight,
   * or false otherwise */
  def hasCompletedBar(unit:Unity):Boolean =
    actionBars.get(unit) match {
      case Some(currentBar) =>
        currentBar >= unit.totalWeight
      case None =>
        false

    }

  /** Calculates how much a unit exceeded its maximum action bar*/
  private def excess(unit:Unity):Double =
    actionBars(unit) - unit.totalWeight

  /**Keeps only units with a completed bar and orders them from
   * greatest to small excess. The negative value reverses sortBy's
   * default ascending order*/
  def completedUnits:Seq[Unity] =
    actionBars.keys.filter(hasCompletedBar).toSeq.sortBy(unit => -excess(unit))

  /** Returns Some(unit) for the first completed unit,
   *  or None if no unit is ready*/
  def nextUnit: Option[Unity] =
    completedUnits.headOption













/**
 * Main game controller.
 * This class implements the logic that connects the different model
 * entities,manages turns,and executes actions.
 */
class GameController {

}
