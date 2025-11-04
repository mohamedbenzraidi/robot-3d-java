package org.example.scene;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.simsilica.lemur.*;

public class HudManager extends BaseAppState {

    private SimpleApplication app;

    @Override
    protected void initialize(Application app) {
        this.app = (SimpleApplication) app;

        Container hud = new Container();
        hud.addChild(new Label("Virtual 3D Visit"));
        Button startBtn = hud.addChild(new Button("Start Tour"));
        Button stopBtn = hud.addChild(new Button("Stop"));

        startBtn.addClickCommands(source -> System.out.println("Starting robot..."));
        stopBtn.addClickCommands(source -> System.out.println("Stopping robot..."));

        ((SimpleApplication) app).getGuiNode().attachChild(hud);
        hud.setLocalTranslation(20, app.getCamera().getHeight() - 20, 0);
    }

    @Override protected void cleanup(Application app) {}
    @Override protected void onEnable() {}
    @Override protected void onDisable() {}
}