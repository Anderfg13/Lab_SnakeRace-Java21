package co.eci.snake.core;

/**
 * Record inmutable que representa una posición en el tablero de juego.
 * 
 * Utiliza las coordenadas cartesianas (x, y) donde:
 * - x: columna en el tablero (0 = izquierda)
 * - y: fila en el tablero (0 = arriba)
 * 
 * Al ser un record de Java, es inmutable y proporciona automáticamente
 * implementaciones de equals(), hashCode() y toString().
 * 
 * @author Anderson Fabian Garcia Nieto
 * @author Juana Lozano Chaves
 * @version 1.0
 * 
 * @param x Coordenada horizontal (columna) en el tablero
 * @param y Coordenada vertical (fila) en el tablero
 */
public record Position(int x, int y) {

  /**
   * Calcula una nueva posición envuelta dentro de los límites del tablero.
   * 
   * Implementa el comportamiento "wrap-around" (toroidal) donde si una posición
   * sale por un borde del tablero, reaparece por el borde opuesto.
   * Por ejemplo, si x es -1, se convierte en width-1.
   * 
   * @param width  Ancho del tablero (número de columnas)
   * @param height Alto del tablero (número de filas)
   * @return Una nueva posición dentro de los límites [0, width) y [0, height)
   */
  public Position wrap(int width, int height) {
    int nx = ((x % width) + width) % width;
    int ny = ((y % height) + height) % height;
    return new Position(nx, ny);
  }
}
