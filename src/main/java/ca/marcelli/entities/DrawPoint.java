package ca.marcelli.entities;

import java.awt.Color;
import java.awt.Point;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class DrawPoint extends Drawable {

  /**
   * The point will be offset by 2 so it centres when it draws
   *
   * @param p Point to draw
   */
  public DrawPoint(Point p) {
    super(p.x - 2, p.y - 2, 5, 5, Color.CYAN);
  }

  public DrawPoint(int x, int y) {
    this(new Point(x, y));
  }

  /**
   * Get the points true place
   *
   * @return point, with offset removed
   */
  public Point getPoint() {
    return new Point(getX() + 2, getY() + 2);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof DrawPoint)) {
      return false;
    }
    DrawPoint drawPoint = (DrawPoint) o;
    return drawPoint.getPoint().equals(getPoint());
  }

  @Override
  public int hashCode() {
    return getPoint().x + getPoint().y;
  }

  @Override
  public String toString() {
    return String.format("(%d,%d)", getPoint().x, getPoint().y);
  }
}
