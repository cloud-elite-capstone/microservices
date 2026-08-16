package com.retasify.shopservice.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.locationtech.jts.io.WKBWriter;

@Converter(autoApply = false)
public class PointAttributeConverter implements AttributeConverter<Point, byte[]> {

    private final WKBWriter writer = new WKBWriter();
    private final WKBReader reader = new WKBReader();

    @Override
    public byte[] convertToDatabaseColumn(Point attribute) {
        if (attribute == null) {
            return null;
        }
        return writer.write(attribute);
    }

    @Override
    public Point convertToEntityAttribute(byte[] dbData) {
        if (dbData == null || dbData.length == 0) {
            return null;
        }
        try {
            return (Point) reader.read(dbData);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Invalid WKB geometry for Point.", e);
        }
    }
}
