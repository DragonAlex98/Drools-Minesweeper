package it.unicam.cs.enumeration;

/**
 * Enumeration to represent the three possible result when a Square is uncovered (Bomb, Safe, No_action).
 *
 */
public enum UncoverResult {
	/** A bomb is uncovered, you lost! **/
	BOMB,
	/** Safe square, you can uncover another one! **/
	SAFE,
	/** Square that cannot be uncovered! **/
	NO_ACTION;
}
