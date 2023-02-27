public class Edge<T> {
	private T destination;
	private String name;
	private int weight;

	public Edge(T destination, String name, int weight) {
		this.destination = destination;
		this.name = name;
		this.weight = weight;
	}

	public T getDestination() {
		return destination;
	}

	public String getName() {
		return name;
	}

	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}

	@Override
	public String toString() {
		return "till " + destination + " med " + name + " tar " + weight;
	}
}
