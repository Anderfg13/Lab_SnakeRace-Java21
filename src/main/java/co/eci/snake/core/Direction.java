package co.eci.snake.core;

/**
 * Enumeración que representa las direcciones de movimiento posibles para una serpiente.
 * 
 * Define las cuatro direcciones cardinales con sus respectivos desplazamientos
 * en las coordenadas (dx, dy):
 * - UP: Movimiento hacia arriba (dy = -1)
 * - DOWN: Movimiento hacia abajo (dy = +1)
 * - LEFT: Movimiento hacia la izquierda (dx = -1)
 * - RIGHT: Movimiento hacia la derecha (dx = +1)
 * 
 * @author Anderson Fabian Garcia Nieto
 * @author Juana Lozano Chaves
 * @version 1.0
 */
public enum Direction {
  /** Movimiento hacia arriba (decrementa la coordenada Y) */
  UP(0, -1),
  /** Movimiento hacia abajo (incrementa la coordenada Y) */
  DOWN(0, 1),
  /** Movimiento hacia la izquierda (decrementa la coordenada X) */
  LEFT(-1, 0),
  /** Movimiento hacia la derecha (incrementa la coordenada X) */
  RIGHT(1, 0);

  /** Desplazamiento en el eje X para esta dirección */
  public final int dx;
  /** Desplazamiento en el eje Y para esta dirección */
  public final int dy;

  /**
   * Constructor de la enumeración Direction.
   * 
   * @param dx Desplazamiento en el eje X (-1, 0, o 1)
   * @param dy Desplazamiento en el eje Y (-1, 0, o 1)
   */
  Direction(int dx, int dy) {
    this.dx = dx;
    this.dy = dy;
  }
}
