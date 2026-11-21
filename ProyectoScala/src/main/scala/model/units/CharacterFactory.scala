package cl.uchile.dcc.model.units

import scala.util.Random

/** Identifies the playable character classes. */
enum CharacterType:
  case Knight, Archer, Thief, BlackMage, WhiteMage

/** Creates the units used to start a battle. */
object CharacterFactory {

  /** Creates a playable character with random starting stats. */
  def createCharacter(
                       characterType: CharacterType,
                       name: String,
                       random: Random
                     ): Character = {
    val lifePoints = random.between(80, 121).toDouble
    val defense = random.between(3, 9)
    val weight = random.between(40, 81).toDouble

    characterType match {
      case CharacterType.Knight => new Knight(name, lifePoints, defense, weight)
      case CharacterType.Archer => new Archer(name, lifePoints, defense, weight)
      case CharacterType.Thief  => new Thief(name, lifePoints, defense, weight)
      case CharacterType.BlackMage =>
        val mana = random.between(50, 101).toDouble
        new BlackMage(name, lifePoints, defense, weight, mana)
      case CharacterType.WhiteMage =>
        val mana = random.between(50, 101).toDouble
        new WhiteMage(name, lifePoints, defense, weight, mana)
    }
  }

  /** Creates an enemy with random starting stats. */
  def createEnemy(name: String, random: Random): Enemy = {
    val lifePoints = random.between(80, 121).toDouble
    val attackPoints = random.between(10, 26)
    val defense = random.between(3, 9)
    val weight = random.between(40, 81).toDouble

    new Enemy(name, lifePoints, attackPoints, defense, weight)
  }
}