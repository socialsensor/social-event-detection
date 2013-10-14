package models;

public class Point {
	
	private double longitude;
	private double latitude;

	public Point(double longitude, double latitude) {
		super();
		this.longitude = longitude;
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public double getLatitude() {
		return latitude;
	}

	@Override
	public String toString() {
		return "(" + longitude + ", " + latitude + ")";
	}
	
	
}
