package org.example.scene;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetManager;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Quad;


public class LoadingScreen extends BaseAppState {
    private Node guiNode;
    private BitmapText loadingText;
    private BitmapText progressText;
    private Geometry progressBar;
    private Geometry progressBarBackground;
    private float progress = 0f;


    @Override
    protected void initialize(Application app) {
        guiNode = new Node("Loading Gui");
        AssetManager assetManager = app.getAssetManager();

        BitmapFont font = assetManager.loadFont("Interface/Fonts/Default.fnt");
        loadingText = new BitmapText(font);
        loadingText.setText("Loading...");
        loadingText.setSize(font.getCharSet().getRenderedSize() * 2);
        loadingText.setColor(ColorRGBA.White);

        float textWidth = loadingText.getLineWidth();
        loadingText.setLocalTranslation(
                (app.getContext().getSettings().getWidth() - textWidth) / 2,
                (float)(app.getContext().getSettings().getHeight()) / 2 + 50,
                0
        );

        progressText = new BitmapText(font);
        progressText.setText("0%");
        progressText.setSize(font.getCharSet().getRenderedSize() * 1.5f);
        progressText.setColor(ColorRGBA.White);
        progressText.setLocalTranslation(
                (float)(app.getContext().getSettings().getWidth()) / 2 - 20,
                (float)(app.getContext().getSettings().getHeight()) / 2 - 30,
                0
        );

        Quad bgQuad = new Quad(400, 20);
        progressBarBackground = new Geometry("ProgressBarBG", bgQuad);
        Material bgMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        bgMat.setColor("Color", ColorRGBA.DarkGray);
        progressBarBackground.setMaterial(bgMat);
        progressBarBackground.setLocalTranslation(
                (float)(app.getContext().getSettings().getWidth() - 400) / 2,
                (float)(app.getContext().getSettings().getHeight()) / 2 - 70,
                0
        );

        Quad barQuad = new Quad(1, 20);
        progressBar = new Geometry("ProgressBar", barQuad);
        Material barMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        barMat.setColor("Color", ColorRGBA.Green);
        progressBar.setMaterial(barMat);
        progressBar.setLocalTranslation(
                (float)(app.getContext().getSettings().getWidth() - 400) / 2,
                (float)(app.getContext().getSettings().getHeight()) / 2 - 70,
                1
        );


        guiNode.attachChild(loadingText);
        guiNode.attachChild(progressText);
        guiNode.attachChild(progressBarBackground);
        guiNode.attachChild(progressBar);

    }

    @Override
    protected void cleanup(Application app) {
        guiNode.detachAllChildren();
    }

    @Override
    protected void onEnable() {
        if (getApplication() instanceof com.jme3.app.SimpleApplication) {
            ((com.jme3.app.SimpleApplication) getApplication()).getGuiNode().attachChild(guiNode);
        }
    }

    @Override
    protected void onDisable() {
        guiNode.removeFromParent();
    }

    @Override
    public void update(float tpf){
        progressBar.setLocalScale(progress * 400, 1, 1);

        progressText.setText(String.format("%.0f%%", progress * 100));

        float textWidth = progressText.getLineWidth();
        progressText.setLocalTranslation(
                (getApplication().getContext().getSettings().getWidth() - textWidth) / 2,
                (float)(getApplication().getContext().getSettings().getHeight()) / 2 - 20,
                0
        );
    }

    public void setProgress(float progress){
        this.progress = Math.max(0f, Math.min(1f, progress));
    }

    public void setLoadingText(String text){
        if(loadingText != null) {
            loadingText.setText(text);
            float textWidth = loadingText.getLineWidth();
            loadingText.setLocalTranslation(
                    (getApplication().getContext().getSettings().getWidth() - textWidth) / 2,
                    (float) (getApplication().getContext().getSettings().getHeight()) / 2 + 50,
                    0
            );
        }
    }
}