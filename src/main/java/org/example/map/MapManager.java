package org.example.map;


import org.geotools.data.*;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.map.FeatureLayer;
import org.geotools.map.MapContent;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.renderer.lite.StreamingRenderer;
import org.geotools.styling.SLD;
import org.geotools.styling.Style;
import org.hsqldb.types.Charset;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class MapManager {
    public BufferedImage renderMapImage(File shapeFile) throws Exception{
        FileDataStore dataStore = FileDataStoreFinder.getDataStore(shapeFile);
        SimpleFeatureSource featureSource = dataStore.getFeatureSource();

        MapContent map = new MapContent();

        Style style = SLD.createSimpleStyle(featureSource.getSchema());

        map.addLayer(new FeatureLayer(featureSource, style));

        BufferedImage bufferedImage = new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics2D = bufferedImage.createGraphics();
        graphics2D.setPaint(Color.WHITE);
        graphics2D.fillRect(0, 0, 800, 600);


        StreamingRenderer renderer = new StreamingRenderer();
        renderer.setMapContent(map);
        renderer.paint(graphics2D, new Rectangle(0, 0, 800, 600), map.getViewport().getBounds());
        graphics2D.dispose();
        map.dispose();

        return bufferedImage;
    }

    public void room() throws Exception{
        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        builder.setName("Room");
        builder.setCRS(DefaultGeographicCRS.WGS84);
        builder.add("the_geom", Polygon.class);
        builder.add("name", String.class);
        final SimpleFeatureType TYPE = builder.buildFeatureType();

        GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();

        Coordinate[] coords = new Coordinate[] {
                new Coordinate(0,0),
                new Coordinate(10,0),
                new Coordinate(10,8),
                new Coordinate(0,8),
                new Coordinate(0,0)
        };

        Polygon roomPolygon = geometryFactory.createPolygon(coords);
        SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(TYPE);
        featureBuilder.add(roomPolygon);
        featureBuilder.add("Room A");
        SimpleFeature feature = featureBuilder.buildFeature(null);



        File shapeFile = new File("src/main/resources/maps/shapeFile.shp");
        Map<String, Object> params = new HashMap<>();
        params.put("url", shapeFile.toURI().toURL());
        params.put("Create a spatial index", Boolean.TRUE);

        ShapefileDataStoreFactory shapefileDataStoreFactory = new ShapefileDataStoreFactory();

        ShapefileDataStore shapefileDataStore = (ShapefileDataStore) shapefileDataStoreFactory.createDataStore(params);

        shapefileDataStore.createSchema(TYPE);
        shapefileDataStore.setCharset(StandardCharsets.UTF_8);

        Transaction transaction = new DefaultTransaction("create");
        String typeName = shapefileDataStore.getTypeNames()[0];

        SimpleFeatureSource featureSource = shapefileDataStore.getFeatureSource(typeName);

        if(featureSource instanceof SimpleFeatureStore featureStore){
            featureStore.setTransaction(transaction);
            try{
               featureStore.addFeatures(DataUtilities.collection(feature));
               transaction.commit();
                System.out.println("File created successfully : " + shapeFile.getAbsolutePath());
            }catch (Exception e){
                System.out.println("Issue here");
                e.printStackTrace();
                transaction.rollback();
            } finally {
                transaction.close();
            }
        }else{
            System.out.println("Unable to read file.");
        }
    }

    public SimpleFeatureIterator getIterator(File shapeFile)throws Exception{
        FileDataStore dataStore = FileDataStoreFinder.getDataStore(shapeFile);
        SimpleFeatureSource featureSource = dataStore.getFeatureSource();
        SimpleFeatureIterator iterator = featureSource.getFeatures().features();

        return iterator;
    }
}
