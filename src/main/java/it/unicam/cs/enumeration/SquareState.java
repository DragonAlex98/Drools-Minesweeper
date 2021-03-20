package it.unicam.cs.enumeration;

/**
 * Enumeration to represent the four possible states of a Square (Covered, Uncovered, Flagged, Exploded).
 *
 */
public enum SquareState {
	/** Covered square **/
	COVERED,
	/** Uncovered square **/
	UNCOVERED,
	/** Covered square with a flag **/
	FLAGGED,
	/** Uncovered square containing a bomb **/
	EXPLODED;
}
