package ca.marcelli;

import ca.marcelli.entities.DrawRectangle;
import ca.marcelli.entities.Drawable;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.JPanel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
public class RectanglePanel extends JPanel {

  @Getter
  private HashMap<String, DrawRectangle> rectangles;

  // Simple way to draw new rectangles
  private Point newPointA, newPointB;
  private DrawRectangle indicatorRectangle;

  // Draw intersection
  @Getter
  @Setter
  private List<DrawRectangle> intersections = new ArrayList<>();

  @Getter
  @Setter
  private List<Drawable> intersectPointsAndSegments = new ArrayList<>();

  public RectanglePanel(HashMap<String, DrawRectangle> rectangles) {
    this.rectangles = rectangles;
  }

  public void addIndicatorRectToHashmap() {
    rectangles.put(indicatorRectangle.getName(), DrawRectangle.copyRectangle(indicatorRectangle));
    newPointA = null;
    newPointB = null;
    indicatorRectangle = null;
  }

  public void addIntersection(DrawRectangle intersection) {
    intersections.add(intersection);
  }

  /**
   * Creates the drawables that will show the intersection points and segments. A point intersection
   * occurs when two orthogonal sides cross. An "infinite" intersection segment occurs when the
   * rectangles share one or more sides with each other, but still intersect.
   *
   * The extraction works conceptually by taking a vertical line, moving it over each possible x value,
   * and counting the points. If the number of points is at least 3, there must be an intersection there,
   * and it will correspond to the "middle" of the values.
   * Repeat with a horizontal line for y values.
   *
   *
   * @param intersectionRectangle DrawRectangle representing the intersection
   * @param a DrawRectangle for intersection
   * @param b Second DrawRectangle for intersection
   * @return a list of drawables to render
   */
  public List<Drawable> getAllIntersectionsFor(DrawRectangle intersectionRectangle,
                                               DrawRectangle a,
                                               DrawRectangle b) {

    // Fast fail in case intersection is null.
    if(null == intersectionRectangle) return null;
    // Get the vertices of the intersection rectangle and the two intersecting
    // rectangles, removing duplicates
    Set<Point> vertices = intersectionRectangle.getVertices();
    vertices.addAll(a.getVertices());
    vertices.addAll(b.getVertices());
    int totalCount = vertices.size();
    List<Map<Integer, List<Point>>> hashes = DrawRectangle.getPointHashes(vertices);
    // 3 or more points along a vertical line
    Stream<Drawable> xIntersects =
        hashes.get(0).entrySet().stream().filter(entry -> entry.getValue().size() > 2)
            .flatMap(entry -> DrawRectangle.extractIntersects(entry, totalCount,
                intersectionRectangle.getVertices(),
                a.getVertices(), b.getVertices(), false));

    // 3 or more points along a horizontal line
    Stream<Drawable> yIntersects =
        hashes.get(1).entrySet().stream().filter(entry -> entry.getValue().size() > 2)
            .flatMap(entry -> DrawRectangle.extractIntersects(entry, totalCount,
                intersectionRectangle.getVertices(),
                a.getVertices(), b.getVertices(), true));

    return Stream.concat(xIntersects, yIntersects).distinct().collect(Collectors.toList());
  }

  // Removing from the hash + a repaint on the listener ensures the rectangle is
  // removed
  public void deleteRectangle(String key) {
    rectangles.remove(key);
  }

  // When clicking, create an indicator rectangle and set Point A to be the first
  // click
  public void setNewPointA(MouseEvent e) {
    indicatorRectangle = new DrawRectangle();
    newPointA = e.getPoint();
  }

  // Point B is set on drag on every update
  public void setNewPointB(MouseEvent e) {
    newPointB = e.getPoint();
  }

  @Override
  public void paintComponent(Graphics g) {
    super.paintComponent(g);
    Graphics2D g2d = (Graphics2D) g;
    // Render an indicator when drawing new rectangles
    if (null != newPointA && null != newPointB) {
      int width = newPointA.x - newPointB.x;
      int length = newPointA.y - newPointB.y;

      // If the width/length is negative, point b is used to draw the initial vertex
      // instead
      int x = width <= 0 ? newPointA.x : newPointB.x;
      int y = length <= 0 ? newPointA.y : newPointB.y;
      indicatorRectangle.setX(x);
      indicatorRectangle.setY(y);
      indicatorRectangle.setWidth(Math.abs(width));
      indicatorRectangle.setLength(Math.abs(length));
      indicatorRectangle.draw(g2d);
    }

    // Fill intersections + add points
    intersections.forEach(intersect -> intersect.fill(g2d));

    // Draw all rectangles in hash
    rectangles.values().forEach(rectangle -> rectangle.draw(g2d));

    // Fill in intersection points and segments
    intersectPointsAndSegments.forEach(drawable -> {
      drawable.fill(g2d);
    });
  }

  public void addIntersectPointsAndSegments(Drawable drawable) {
    intersectPointsAndSegments.add(drawable);
  }

  public void addIntersectPointsAndSegments(List<? extends Drawable> drawables) {
    intersectPointsAndSegments.addAll(drawables);
  }
}
