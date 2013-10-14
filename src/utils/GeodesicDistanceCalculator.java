package utils;

import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Map;

public class GeodesicDistanceCalculator {

	public static Double vincentyDistance(double lat1, double lon1, double lat2, double lon2) {
		
		double a = 6378137, b = 6356752.3142,  f = 1/298.257223563;
		double L = toRad((lon2-lon1));
		double U1 = Math.atan((1-f) * Math.tan(toRad(lat1)));
		double U2 = Math.atan((1-f) * Math.tan(toRad(lat2)));
		
		double sinU1 = Math.sin(U1), cosU1 = Math.cos(U1);
		double sinU2 = Math.sin(U2), cosU2 = Math.cos(U2);
		
		double lambda = L, lambdaP, iterLimit = 100, cosSqAlpha, cosSigma, sigma, sinAlpha, cos2SigmaM, sinSigma, sinLambda, cosLambda;
		
		do {
			
			sinLambda = Math.sin(lambda);
			cosLambda = Math.cos(lambda);
			
		    sinSigma = Math.sqrt((cosU2*sinLambda) * (cosU2*sinLambda) + 
		      (cosU1*sinU2-sinU1*cosU2*cosLambda) * (cosU1*sinU2-sinU1*cosU2*cosLambda));
		    
		 // co-incident points
		    if (sinSigma == 0) 
		    	return 0.0;  
		    
		    cosSigma = sinU1*sinU2 + cosU1*cosU2*cosLambda;
		    sigma = Math.atan2(sinSigma, cosSigma);
		    sinAlpha = cosU1 * cosU2 * sinLambda / sinSigma;
		    cosSqAlpha = 1 - sinAlpha*sinAlpha;
		    
		    if(cosSqAlpha == 0.0){
		    	cos2SigmaM = 0;
		    }else{
		    	cos2SigmaM = cosSigma - 2*sinU1*sinU2/cosSqAlpha;
		    }
		    
			double C = f/16*cosSqAlpha*(4+f*(4-3*cosSqAlpha));
			lambdaP = lambda;
			lambda = L + (1-C) * f * sinAlpha *
			  (sigma + C*sinSigma*(cos2SigmaM+C*cosSigma*(-1+2*cos2SigmaM*cos2SigmaM)));
			
		} while(Math.abs(lambda-lambdaP) > 1e-12 && --iterLimit>0);
		
		// formula failed to converge
		if (iterLimit == 0) 
			return null;  

		double uSq = cosSqAlpha * (a*a - b*b) / (b*b);
		double A = 1 + uSq/16384*(4096+uSq*(-768+uSq*(320-175*uSq)));
		double B = uSq/1024 * (256+uSq*(-128+uSq*(74-47*uSq)));
		double deltaSigma = B*sinSigma*(cos2SigmaM+B/4*(cosSigma*(-1+2*cos2SigmaM*cos2SigmaM)-
		    B/6*cos2SigmaM*(-3+4*sinSigma*sinSigma)*(-3+4*cos2SigmaM*cos2SigmaM)));
		
		Double s = b*A*(sigma-deltaSigma);
		
		return s;
	}
	
	public static double toRad(double val) {
		return val * Math.PI / 180;
	}
	
	/**
	 * This method receives two points in 2D space. One representing an initial geodesic point in decimal
	 * format and a second one representing a second point in common Cartesian form. The second point 
	 * denotes the distance from the first point, if the latter is considered to be point (0,0). 
	 * The cartesian points exrpesses distance in kilometers (i.e. <code>newCartesianPointX</code>=1
	 * and <code>newCartesianPointY</code>=-1 denotes a point that resides 1 km towards the east and 1
	 * km towards the south from the initial geodesic point).
	 * 
	 * This method returns a Point2D point (in double format) which represents the new geodesic point.
	 * 
	 * 
	 * @param initialLongtitude The initial longtitude value in decimal format
	 * @param initialLatitude The initial latitude value in decimal format
	 * @param newCartesianPointX The change in the X axis 
	 * @param newCartesianPointY The change in the Y axis
	 * @return The new geodesic point in decimal format.
	 */
	public static Point2D.Double get(double initialLongtitude, double initialLatitude, double newCartesianPointX, double newCartesianPointY) {
		
		if (degreeToKilometersMap==null) {
			initializeDegreeToKilometersMap();
		}
		
		double kilometersPerDegreeForSpecificLatitude = getKilometersPerDegreeForLatitude(initialLatitude);		
		double geopointsLongtitude = newCartesianPointX/kilometersPerDegreeForSpecificLatitude+initialLongtitude;
		double geopointsLatitude = newCartesianPointY/kilometersPerDegreeForSpecificLatitude+initialLatitude;
		
		return new Point2D.Double(geopointsLongtitude, geopointsLatitude);
		
	}
	
private static Map<Integer, Double> degreeToKilometersMap;
	
	private static void initializeDegreeToKilometersMap() {
		
		degreeToKilometersMap = new HashMap<Integer, Double>();
		degreeToKilometersMap.put(0, 110.57);
		degreeToKilometersMap.put(10, 110.61);
		degreeToKilometersMap.put(20, 110.70);
		degreeToKilometersMap.put(30, 110.85);
		degreeToKilometersMap.put(40, 111.04);
		degreeToKilometersMap.put(50, 111.23);
		degreeToKilometersMap.put(60, 111.41);
		degreeToKilometersMap.put(70, 111.56);
		degreeToKilometersMap.put(80, 111.66);
		degreeToKilometersMap.put(90, 111.69);		
	}
	
	private static double getKilometersPerDegreeForLatitude(double latitude) {
		return degreeToKilometersMap.get((((int)Math.ceil(latitude))/10)*10);
	}
}