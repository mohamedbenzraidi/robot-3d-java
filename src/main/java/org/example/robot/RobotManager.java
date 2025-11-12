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

    public void setRobot(Node rootNode, Vector3f position){
        try{
            robot = this.assetManager.loadModel("Models/ai_robot.j3o");
            robot.scale(5f);
            robot.setLocalTranslation(position.add(new Vector3f(2f,-3f,-2f)));
            robot.rotate(0, (float) Math.toRadians(-90), 0);
            rootNode.attachChild(robot);


        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error : loading the robot model");
        }

    }

    public Spatial getRobot(){
        return this.robot;
    }


}
