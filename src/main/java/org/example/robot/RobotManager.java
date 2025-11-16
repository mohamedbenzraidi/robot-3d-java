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
import org.example.scene.AssetLoader;

public class RobotManager {
    private final AssetManager assetManager;
    private Spatial robot;

    public RobotManager(AssetManager assetManager){
        this.assetManager = assetManager;
    }

    public void setRobot(Node rootNode, AssetLoader assetLoader){
        try{
            robot = assetLoader.getModel("robot");

            Material robotMat = assetLoader.getMaterial("defMat");
            Texture robotTex = assetLoader.getTexture("robotTexture");
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
