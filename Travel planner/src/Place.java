import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class Place extends Circle {
	private String name;
	private double x;
	private double y;

	public Place(String name, double x, double y) {
		super(x, y, 10.0);
		setFill(Color.BLUE);
		this.name = name;
		this.x = x;
		this.y = y;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public void setX(double x) {
		this.x = x;
	}

	public void setY(double y) {
		this.y = y;
	}

	public String getName() {
		return name;
	}

	public void makeRed() {
		this.setFill(Color.RED);
	}

}
