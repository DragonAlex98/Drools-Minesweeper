package it.unicam.cs.enumeration;

/**
 * Enumeration to represent the three possible type of a Square (Bomb, Number, Empty).
 *
 */
public enum SquareType {
	/** Square with no bomb in its neighbors **/
	EMPTY,
	/** Square with at least one (at most eight) bomb in its neighbors **/
	NUMBER,
	/** Square containing a Bomb **/
	BOMB;
}
