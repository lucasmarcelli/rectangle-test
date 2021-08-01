package ca.marcelli.entities;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class DrawSegment extends Drawable {
  private DrawPoint p1;
  private DrawPoint p2;

  public DrawSegment(List<Point> points, String message) {
    points.stream().map(DrawPoint::new).collect(Collectors.toList());
    this.p1 = new DrawPoint(points.get(0));
    this.p2 = new DrawPoint(points.get(1));
    setMessage(message);
  }

  public DrawSegment(DrawPoint p1, DrawPoint p2, String message) {
    this.p1 = p1;
    this.p2 = p2;
    setMessage(message);
  }

  @Override
  public void fill(Graphics2D g2d) {
    g2d.setColor(Color.CYAN);
    p1.fill(g2d);
    p2.fill(g2d);
    g2d.setStroke(new BasicStroke(5));
    g2d.drawLine(p1.getX() + 2, p1.getY() + 2, p2.getX() + 2, p2.getY() + 2);
    g2d.setStroke(new BasicStroke(1));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof DrawSegment)) {
      return false;
    }
    DrawSegment segment = (DrawSegment) o;
    return p1.equals(segment.p1) && p2.equals(segment.p2) ||
        p2.equals(segment.p1) && p1.equals(segment.p2);
  }

  @Override
  public int hashCode() {
    return p1.hashCode() + p2.hashCode();
  }

  @Override
  public String toString() {
    return String.format("%s to %s", p1.toString(), p2.toString());
  }

  public boolean containsPoint(DrawPoint p) {
    return p.equals(getP1()) || p.equals(getP2());
  }

  public boolean containsPoint(Point p) {
    DrawPoint drawPoint = new DrawPoint(p);
    return containsPoint(drawPoint);
  }
}
