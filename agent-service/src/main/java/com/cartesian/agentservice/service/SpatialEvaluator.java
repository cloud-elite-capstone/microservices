package com.cartesian.agentservice.service;

import java.util.List;
import java.util.Map;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.springframework.stereotype.Component;

@Component
public class SpatialEvaluator {

    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();

    public Polygon parsePolygon(Object locationObj) {
        if (locationObj == null) {
            return null;
        }
        if (locationObj instanceof Polygon polygon) {
            return polygon;
        }
        if (locationObj instanceof Map<?, ?> map) {
            if (!"Polygon".equalsIgnoreCase(String.valueOf(map.get("type")))) {
                return null;
            }
            Object coordinates = map.get("coordinates");
            if (!(coordinates instanceof List<?> rings) || rings.isEmpty()) {
                return null;
            }
            Object outerRing = rings.get(0);
            if (!(outerRing instanceof List<?> ringCoordinates) || ringCoordinates.size() < 4) {
                return null;
            }

            Coordinate[] coordinatesArray = new Coordinate[ringCoordinates.size()];
            for (int i = 0; i < ringCoordinates.size(); i++) {
                Object coordinate = ringCoordinates.get(i);
                if (!(coordinate instanceof List<?> point) || point.size() < 2) {
                    return null;
                }
                double x = Double.parseDouble(point.get(0).toString());
                double y = Double.parseDouble(point.get(1).toString());
                coordinatesArray[i] = new Coordinate(x, y);
            }
            LinearRing linearRing = GEOMETRY_FACTORY.createLinearRing(coordinatesArray);
            return GEOMETRY_FACTORY.createPolygon(linearRing);
        }
        return null;
    }

    public boolean isWithinPolygon(Point point, Polygon polygon) {
        if (polygon == null) {
            return true;
        }
        if (point == null) {
            return false;
        }
        return polygon.contains(point) || polygon.touches(point) || polygon.isWithinDistance(point, 0.05);
    }
}
