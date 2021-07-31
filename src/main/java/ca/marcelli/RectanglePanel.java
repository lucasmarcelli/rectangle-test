package ca.marcelli;

import ca.marcelli.entities.DrawPoint;
import ca.marcelli.entities.DrawRectangle;
import ca.marcelli.entities.DrawSegment;
import ca.marcelli.entities.Drawable;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.JPanel;
import lombok.Getter;
import lombok.Setter;

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

  // The intersection points are a subset of the vertices on the intersection and
  // the vertices of the rectangles
  // with a edge case for if a side is shared but an intersection still occurs
  public List<Drawable> createIntersectionPointsAndSegments(DrawRectangle intersectionRectangle,
                                                            DrawRectangle a,
                                                            DrawRectangle b) {
    // Get the vertices of the intersection rectangle and the two intersecting
    // rectangles, removing duplicates
    Set<Point> vertices = intersectionRectangle.getVertices();
    vertices.addAll(a.getVertices());
    vertices.addAll(b.getVertices());
    int totalCount = vertices.size();
    List<Map<Integer, List<Point>>> hashes = DrawRectangle.getPointHashes(vertices);

    // 3 or more points along a vertical line
    Stream<Drawable> xPoints =
        hashes.get(0).entrySet().stream().filter(entry -> entry.getValue().size() > 2)
            .flatMap(entry -> extractIntersectPoints(entry, totalCount,
                intersectionRectangle.getVertices(),
                a.getVertices(), b.getVertices(), false));

    // 3 or more points along a horizontal line
    Stream<Drawable> yPoints =
        hashes.get(1).entrySet().stream().filter(entry -> entry.getValue().size() > 2)
            .flatMap(entry -> extractIntersectPoints(entry, totalCount,
                intersectionRectangle.getVertices(),
                a.getVertices(), b.getVertices(), true));

    // Make it into a set to remove any duplicates
    return Stream.concat(xPoints, yPoints).distinct().collect(Collectors.toList());
  }

  /**
   * Extract the intersection points from the map entry.
   * <p>
   * For a list of points with the same x values, sorted by y value, the points
   * between the first and last point in the list are the intersection points,
   * with an edge case for intersections along a shared side, where the
   * intersection is all the points between the two that also exist on the
   * intersection rectangle and at least one of the original rectangles. This
   * works if you find all equivalent y's and sort by x as well.
   * <p>
   * If the total count is less than 10, it's an edge case. There will always be
   * two resulting points, with all the points between them being "intersections".
   * The resulting set for each line will be two points, defining the segment of
   * intersection. There is a second, related edge case where there are exactly 10
   * points, but the size of the list is 4 with points that exist only along the
   * intersecting rectangle removed.
   *
   * @param entry                       The entry of points along the line
   * @param totalCount                  total count of all points
   * @param intersectionRectanglePoints the points on the intersection rectangle
   * @param a                           the points on rectangle a
   * @param b                           the points on rectangle b
   * @param isY                         true if we're looking at horizontal lines
   * @return a stream of drawables representing the various intersections
   */
  private Stream<Drawable> extractIntersectPoints(Map.Entry<Integer, List<Point>> entry,
                                                  int totalCount,
                                                  Set<Point> intersectionRectanglePoints,
                                                  Set<Point> a, Set<Point> b, boolean isY) {

    int count = entry.getValue().size();
    DrawSegment segment = null;
    if (totalCount < 10 || (totalCount == 10
        && entry.getValue().stream().filter(p -> a.contains(p) || b.contains(p)).count() == 4)) {
      List<DrawPoint> points = entry.getValue().stream()
          .filter(p -> intersectionRectanglePoints.contains(p) && (a.contains(p) || b.contains(p)))
          .map(DrawPoint::new).collect(Collectors.toList());
      if (points.size() == 2) {
        segment = new DrawSegment(points.get(0), points.get(1));
      }
    }

    DrawSegment finalSegment = segment;
    return Stream.concat(Stream.of(segment),
        entry.getValue().stream()
            .filter(p -> finalSegment == null || !finalSegment.containsPoint(p))
            .sorted(Comparator.comparingInt(p -> isY ? p.x : p.y)).skip(1).limit(count - 2)
            .map(DrawPoint::new))
        .filter(Objects::nonNull);
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
