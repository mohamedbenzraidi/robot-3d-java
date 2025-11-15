package org.example.robot;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.AssetKey;
import com.jme3.asset.AssetManager;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.texture.Texture;

public class RobotManager {
    private final SimpleApplication app;
    private final AssetManager assetManager;
    private Spatial robot;

    public RobotManager(SimpleApplication app){
        this.app = app;
        this.assetManager = app.getAssetManager();
    }

    public void setRobot(Node rootNode){
        try{
            robot = this.assetManager.loadModel("Models/robot.glb");

            Material robotMat = new Material(app.getAssetManager(), "Common/MatDefs/Light/Lighting.j3md");
            Texture robotTex = app.getAssetManager().loadTexture("Textures/texture.png");
            robotMat.setTexture("DiffuseMap", robotTex);
            robot.setMaterial(robotMat);


            rootNode.attachChild(robot);


        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("ℹ Aucune texture externe trouvée pour le robot — texture intégrée utilisée.");

        }

    }

    public Spatial getRobot(){
        return this.robot;
    }


}
