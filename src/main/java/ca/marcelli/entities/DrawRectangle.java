package ca.marcelli.entities;

import com.github.javafaker.Faker;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DrawRectangle extends Drawable {

  // I want a name to identify rectangles by, faker comes in handy for this
  private String name = Faker.instance().name().firstName();

  public DrawRectangle(int x, int y, int width, int length) {
    super(x, y, width, length);
  }

  /**
   * Turns a set of points into a hashmap that has all points that share at least 3 identical x or y values.
   * Works conceptually by taking a vertical line for all possible x values, and writing down each point
   * that is on that line beside the current value. Repeat with a horizontal line for y. Cross out all
   * entries with less than 3 points to a value.
   *
   * @param vertices set of distinct vertices between 2 rectangles and the rectangle representing their intersection
   * @return
   */
  public static List<Map<Integer, List<Point>>> getPointHashes(Set<Point> vertices) {
    Map<Integer, List<Point>> xPointsMap = new HashMap<>();
    Map<Integer, List<Point>> yPointsMap = new HashMap<>();
    vertices.forEach(point -> {
      List<Point> xPointList = new ArrayList<>();
      List<Point> yPointList = new ArrayList<>();
      xPointsMap.putIfAbsent(point.x, xPointList);
      yPointsMap.putIfAbsent(point.y, yPointList);
      xPointList.addAll(xPointsMap.get(point.x));
      yPointList.addAll(yPointsMap.get(point.y));
      yPointList.add(point);
      xPointList.add(point);
      xPointsMap.put(point.x, xPointList);
      yPointsMap.put(point.y, yPointList);
    });
    xPointsMap.values().removeIf(v -> v.size() < 3);
    yPointsMap.values().removeIf(v -> v.size() < 3);
    return List.of(xPointsMap, yPointsMap);
  }

  /**
   * Deep copy of a rectangle, mostly useful for drawing the indicator on the GUI.
   *
   * @param drawRectangle The DrawRectangle to copy.
   * @return a new instance of the DrawRectangle
   */
  public static DrawRectangle copyRectangle(DrawRectangle drawRectangle) {
    DrawRectangle dr =
        new DrawRectangle(drawRectangle.getX(), drawRectangle.getY(), drawRectangle.getWidth(),
            drawRectangle.getLength());
    dr.setName(drawRectangle.getName());
    return dr;
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
  public static Stream<Drawable> extractIntersectPoints(Map.Entry<Integer, List<Point>> entry,
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

  /**
   * Checks that an intersection exists between two rectangles, and returns the rectangle representing
   * the intersection between them if it does. Simply checks if one of the rectangles have part of
   * the other within it's bounds, like a videogame hitbox.
   *
   * @param other The DrawRectangle to check against this
   * @return The DrawRectangle representing the intersection
   */
  public DrawRectangle isIntersectingWith(DrawRectangle other) {
    int leftXPoint = Math.max(getX(), other.getX());
    int rightXPoint = Math.min(getX() + getWidth(), other.getX() + other.getWidth());
    int topYPoint = Math.max(getY(), other.getY());
    int bottomYPoint = Math.min(getY() + getLength(), other.getY() + other.getLength());
    if (leftXPoint >= rightXPoint || topYPoint >= bottomYPoint) {
      return null;
    }
    return new DrawRectangle(leftXPoint, topYPoint, rightXPoint - leftXPoint,
        bottomYPoint - topYPoint);
  }

  /**
   * Check for adjacent sides between rectangles, on both axes, with the straight line test mentioned above,
   * but with additional checks for not intersecting, and that the segment exists on a shared side.
   *
   * There are two special cases.
   * 1. If only 6 distinct points exist, the adjacency is proper and it's the segment between the shared points
   * 2. If only 7 distinct points exist, it's always a sub line adjacency and it's the segment that
   * includes the shared point, to the nearest point on the same axis
   *
   * @param other The DrawRectangle you are checking against
   * @return A list of segments to draw
   */
  public List<DrawSegment> isAdjacentTo(DrawRectangle other) {
    Set<Point> allPoints = getVertices();
    allPoints.addAll(other.getVertices());

    if (allPoints.size() == 6) {
      List<DrawPoint> points = other.getVertices().stream().filter(p -> getVertices().contains(p))
          .map(DrawPoint::new).collect(Collectors.toList());
      return List.of(new DrawSegment(points.get(0), points.get(1),
          String.format("%s is Proper Adjacent to %s", getName(), other.getName())));
    }

    if (allPoints.size() == 7) {
      String message = String.format("%s is Sub-Line Adjacent to %s", getName(), other.getName());
      // Find the shared vertex
      var shared = other.getVertices().stream().filter(p -> getVertices().contains(p))
          .collect(Collectors.toList()).get(0);
      // The adjacent side will be the smaller of the two if it exists
      int length = Math.min(other.getLength(), getLength());
      int width = Math.min(other.getWidth(), getWidth());
      // Make sure it's on a shared side too.
      return allPoints.stream().filter(point -> (point.x == shared.x
          && (getX() == other.getX() + other.getWidth() || other.getX() == getX() + getWidth())
          && (shared.y + length == point.y || shared.y - length == point.y))
          || (point.y == shared.y
          && (getY() == other.getY() + other.getLength() || other.getY() == getY() + getLength())
          && (shared.x + width == point.x || shared.x - width == point.x)))
          .map(p -> new DrawSegment(List.of(shared, p), message)).collect(Collectors.toList());
    }

    // Straight line method
    List<Map<Integer, List<Point>>> hashes = getPointHashes(allPoints);
    return Stream
        .concat(getAdjacentPointsFromHash(hashes.get(0), false).entrySet().stream()
                .flatMap(entry -> getAdjacentType(entry.getValue(), other, false)),
            getAdjacentPointsFromHash(hashes.get(1), true).entrySet().stream()
                .flatMap(entry -> getAdjacentType(entry.getValue(), other, true)))
        .filter(Objects::nonNull).distinct().collect(Collectors.toList());
  }

  /**
   * Get all four vertices for this rectangle.
   * @return Set of vertices.
   */
  public Set<Point> getVertices() {
    Set<Point> vertices = new HashSet<>();
    vertices.add(new Point(getX(), getY()));
    vertices.add(new Point(getX() + getWidth(), getY()));
    vertices.add(new Point(getX(), getY() + getLength()));
    vertices.add(new Point(getX() + getWidth(), getY() + getLength()));
    return vertices;
  }

  /**
   *
   * Containment is just intersection, but one of the rectangles directly matches
   * the resulting intersection rectangle's dimensions.
   *
   * @param other rectangle to check against
   * @return true if this contains the other
   */

  public boolean hasContainmentWith(DrawRectangle other) {
    DrawRectangle intersection = isIntersectingWith(other);
    return null != intersection && hasContainmentWith(other, intersection);
  }

  /**
   * Overload that assumes you already found the intersection because it's handy
   *
   * @param other rectangle to check against
   * @param intersection rectangle representing the intersection
   * @return true if this contains the other
   */
  public boolean hasContainmentWith(DrawRectangle other, DrawRectangle intersection) {
    return other.equals(intersection);
  }

  /**
   * Override draw, to add text to the middle of the rectangle.
   * @param g2d graphics 2d
   */
  @Override
  public void draw(Graphics2D g2d) {
    super.draw(g2d);
    // Draw the name in the centre of the rectangle
    int textWidth = g2d.getFontMetrics().stringWidth(getName());
    int textHeight = g2d.getFontMetrics().getHeight();
    g2d.drawString(getName(), getX() + getWidth() / 2 - textWidth / 2,
        getY() + getLength() / 2 + textHeight / 2);
  }


  /**
   * Utility function to get the adjacent points to construct a segment with.
   *
   * @param hash The hash generated by the straight line method
   * @param isY true for horizontal line
   * @return A simplified map to turn into segments
   */
  private Map<Integer, List<Point>> getAdjacentPointsFromHash(Map<Integer, List<Point>> hash,
                                                              boolean isY) {
    hash.replaceAll((k, points) -> {
      int count = points.size();
      return points.stream().sorted(Comparator.comparingInt(p -> isY ? p.x : p.y)).skip(1)
          .limit(count - 2)
          .collect(Collectors.toList());
    });
    return hash;
  }

  /**
   * Determines the adjacency type in the general case and removes segments that are outside the
   * confines of any line. The type is determined by:
   *
   * 1. If the segment length is equal to the length of one of the sides, it's sub line
   * 2. Otherwise it's partial
   *
   * Assuming you have already checked and skipped the two special cases.
   *
   * @param points Map of all the point
   * @param other Rectangle to compare to
   * @param isY true for horizontal line test
   * @return Stream of segments to draw
   */
  private Stream<DrawSegment> getAdjacentType(List<Point> points, DrawRectangle other,
                                              boolean isY) {
    String message;
    var maxPoint = points.stream().max(Comparator.comparingInt(p -> isY ? p.x : p.y)).get();
    var minPoint = points.stream().min(Comparator.comparingInt(p -> isY ? p.x : p.y)).get();
    var max = isY ? maxPoint.x : maxPoint.y;
    var min = isY ? minPoint.x : minPoint.y;
    if (isY && (max <= getX() && max <= getX() + getWidth()
        || max <= other.getX() && max <= other.getX() + other.getWidth())) {
      return null;
    } else if (!isY && (max <= getY() && max <= getY() + getLength()
        || max <= other.getY() && max <= other.getY() + other.getLength())) {
      return null;
    }
    int minSize =
        isY ? Math.min(other.getWidth(), getWidth()) : Math.min(other.getLength(), getLength());
    if (max - min != minSize) {
      message = "Partial Adjacent";
    } else {
      message = "Sub-Line Adjacent";
    }
    return Stream.of(new DrawSegment(points,
        String.format("%s is %s to %s", getName(), message, other.getName())));
  }
}
