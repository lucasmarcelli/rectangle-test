package ca.marcelli;

import ca.marcelli.entities.DrawRectangle;
import ca.marcelli.entities.DrawSegment;
import ca.marcelli.entities.Drawable;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class RectangleFrame extends JFrame {

  // Simple panel for rendering buttons for interaction
  private JPanel infoPanel = new JPanel();

  // Hashmap tracking sub-panels for interaction
  private HashMap<String, JPanel> interactiveComponents = new HashMap<>();

  // Panel for drawing rectangles on
  private RectanglePanel rectanglePanel;

  // Panel for listing intersections
  private JPanel intersectPanel = new JPanel();

  public RectangleFrame(HashMap<String, DrawRectangle> rectangles) {
    // Set basic layouts and properties for the main panels
    setLayout(new BorderLayout());
    rectanglePanel = new RectanglePanel(rectangles);
    rectanglePanel.setBackground(Color.WHITE);
    infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.PAGE_AXIS));
    infoPanel.setPreferredSize(new Dimension(400, 700));
    rectanglePanel.setPreferredSize(new Dimension(800, 1200));
    intersectPanel.setMaximumSize(new Dimension(350, 1200));

    // Add listeners for mouse click and motion to generate new rectangles
    rectanglePanel.addMouseListener(indicatorRectangleAdapter());
    rectanglePanel.addMouseMotionListener(indicatorRectangleAdapter());

    // Add the labels and buttons to the info panel, and finish generating the frame
    generateInteractiveComponents();
    add(rectanglePanel, BorderLayout.CENTER);
    add(infoPanel, BorderLayout.LINE_START);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setSize(1400, 1200);
    setVisible(true);
  }

  // MouseAdapter for drawing new rectangles
  private MouseAdapter indicatorRectangleAdapter() {
    return new MouseAdapter() {
      @Override
      public void mousePressed(MouseEvent e) {
        super.mousePressed(e);
        rectanglePanel.setNewPointA(e);
      }

      @Override
      public void mouseDragged(MouseEvent e) {
        super.mouseDragged(e);
        rectanglePanel.setNewPointB(e);
        revalidate();
        repaint();
      }

      @Override
      public void mouseReleased(MouseEvent e) {
        super.mouseReleased(e);
        rectanglePanel.addIndicatorRectToHashmap();
        generateInteractiveComponents();
      }
    };
  }

  // Generate the labels and buttons for each rectangle
  private void generateInteractiveComponents() {
    // Clear the current list of components, easy way to ensure no duplicates
    interactiveComponents.values().forEach(i -> infoPanel.remove(i));
    interactiveComponents.clear();

    // Build new ones based on the rectangles currently part of the panel
    rectanglePanel.getRectangles().values().forEach(rectangle -> {
      JButton deleteButton = new JButton("Delete");
      JButton evaluateButton = new JButton("Evaluate");
      JLabel rectLabel = new JLabel(rectangle.getName());

      // Put GUI elements in their own panel so it's easier to work with
      JPanel componentPanel = generateSubPanel(rectLabel, deleteButton, evaluateButton);

      // Define listeners for delete and evaluate
      deleteButton.addActionListener(e -> {
        rectanglePanel.setIntersectPointsAndSegments(new ArrayList<>());
        rectanglePanel.setIntersections(new ArrayList<>());
        intersectPanel.removeAll();
        rectanglePanel.deleteRectangle(rectangle.getName());
        interactiveComponents.remove(rectangle.getName());
        infoPanel.remove(componentPanel);
        revalidate();
        repaint();
      });

      // Pressing evaluate will check that rectangle against all other rectangles
      evaluateButton.addActionListener(e -> {
        rectanglePanel.setIntersections(new ArrayList<>());
        rectanglePanel.setIntersectPointsAndSegments(new ArrayList<>());
        intersectPanel.removeAll();
        rectanglePanel.getRectangles().values().stream().filter(r -> r != rectangle).forEach(r -> {
          // Check for intersection
          DrawRectangle intersection = r.isIntersectingWith(rectangle);
          // Check adjacent
          List<DrawSegment> adjacentSegments = r.isAdjacentTo(rectangle);
          rectanglePanel.addIntersectPointsAndSegments(adjacentSegments);
          adjacentSegments.forEach(segment -> intersectPanel.add(new JLabel(segment.getMessage())));
          if (null != intersection) {
            // We use the name of the rectangle to write label to info panel easily.
            // Since containment is just intersection but with equality to one of the
            // original rectangles,
            // i only need to check rectangles that actually had an intersection
            intersection.setColor(Color.BLUE);
            // You really need to use HTML for multiline jlabels yeesh...
            if (rectangle.contains(r, intersection)) {
              intersection.setMessage(String.format("<html>%s contains %s<br/></html>",
                  rectangle.getName(), r.getName()));
            } else if (r.contains(rectangle, intersection)) {
              intersection.setMessage(String.format("<html> %s is contained within %s <br/><html>",
                  rectangle.getName(), r.getName()));
            } else {
              String message = "<html>";
              message +=
                  String.format("%s intersects with %s<br/>", r.getName(), rectangle.getName());
              List<Drawable> pointsAndSegments = rectanglePanel
                  .createIntersectionPointsAndSegments(intersection, r, rectangle);
              message += pointsAndSegments.stream().map(drawable -> {
                rectanglePanel.addIntersectPointsAndSegments(drawable);
                if (drawable instanceof DrawSegment) {
                  DrawSegment segment = (DrawSegment) drawable;
                  return String.format("<br/> along all points from %s", segment);
                }
                return String.format("<br/>%s ", drawable.toString());
              }).collect(Collectors.joining());
              message += "</html>";
              intersection.setMessage(message);
              intersection.setColor(Color.PINK);
            }
            rectanglePanel.addIntersection(intersection);
            JLabel intersectLabel = new JLabel(intersection.getMessage());
            intersectLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            intersectPanel.add(intersectLabel);
          }
        });
        revalidate();
        repaint();
      });

      // Add the generated sub panels to the main info panel
      interactiveComponents.put(rectangle.getName(), componentPanel);
      infoPanel.add(componentPanel);
      infoPanel.add(intersectPanel);
    });
    revalidate();
    repaint();
  }

  private JPanel generateSubPanel(JComponent rectLabel, JComponent deleteButton,
                                  JComponent evaluateButton) {
    // Add the components to the sub panel and set the sub panel properties
    List<JComponent> components = List.of(rectLabel, deleteButton, evaluateButton);
    JPanel componentPanel = new JPanel();
    components.forEach(componentPanel::add);
    componentPanel.setLayout(new FlowLayout());
    componentPanel.setMaximumSize(new Dimension(300, 50));
    return componentPanel;
  }
}
