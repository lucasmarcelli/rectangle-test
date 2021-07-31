package ca.marcelli.entities;

import java.awt.Color;
import java.awt.Graphics2D;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Drawable {
  private int x;
  private int y;
  private int width;
  private int length;
  private Color color = Color.BLACK;
  private String message;

  public Drawable(int x, int y, int width, int length) {
    this.x = x;
    this.y = y;
    this.width = width;
    this.length = length;
  }

  public Drawable(int x, int y, int width, int length, Color color) {
    this.x = x;
    this.y = y;
    this.width = width;
    this.length = length;
    this.color = color;
  }

  public void draw(Graphics2D g2d) {
    g2d.setColor(getColor());
    g2d.drawRect(getX(), getY(), getWidth(), getLength());
  }

  public void fill(Graphics2D g2d) {
    g2d.setColor(getColor());
    g2d.fillRect(getX(), getY(), getWidth(), getLength());
  }

  @Override
  public boolean equals(Object o) {
	  if (this == o) {
		  return true;
	  }
	  if (!(o instanceof Drawable)) {
		  return false;
	  }
    Drawable drawable = (Drawable) o;
    return (drawable.getX() == this.getX() && drawable.getY() == this.getY()
        && drawable.getLength() == this.getLength() && drawable.getWidth() == this.getWidth());
  }
}
