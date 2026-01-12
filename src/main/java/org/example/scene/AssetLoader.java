package org.example.scene;


import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.scene.Spatial;
import com.jme3.texture.Texture;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class AssetLoader extends BaseAppState {

    private LoadingScreen loadingScreen;
    private List<AssetToLoad> assetsToLoad;
    private Map<String, Object> loadedAssets;
    private int loadedCount = 0;
    private boolean isLoading = false;
    private boolean loadingComplete = false;
    private AssetManager assetManager;
    private Runnable onCompleteCallback;

    private static class AssetToLoad{
        public String path;
        public String key;
        public AssetType type;

        public AssetToLoad(String path, String key, AssetType type){
            this.path = path;
            this.key = key;
            this.type = type;
        }
    }

    public enum AssetType{
        MODEL,
        TEXTURE,
        MATERIAL
    }

    public AssetLoader(){
        this.assetsToLoad = new ArrayList<>();
        this.loadedAssets = new HashMap<>();
    }

    @Override
    protected void initialize(Application app) {
        this.assetManager = app.getAssetManager();
        loadingScreen =  new LoadingScreen();
        app.getStateManager().attach(loadingScreen);
    }

    @Override
    protected void cleanup(Application app) {
        if(loadingScreen != null && loadingScreen.isInitialized()){
            getStateManager().detach(loadingScreen);
        }
        assetsToLoad.clear();
        loadedAssets.clear();
    }

    @Override
    protected void onEnable() {
        if (!isLoading && !loadingComplete) {
            startLoading();
        }
    }

    @Override
    protected void onDisable() {

    }

    public AssetLoader addModel(String path, String key){
        assetsToLoad.add(new AssetToLoad(path, key, AssetType.MODEL));
        return this;
    }

    public AssetLoader addTexture(String path, String key){
        assetsToLoad.add(new AssetToLoad(path, key, AssetType.TEXTURE));
        return this;
    }

    public AssetLoader addMaterial(String MaterialDefPath, String key){
        assetsToLoad.add(new AssetToLoad(MaterialDefPath, key, AssetType.MATERIAL));
        return this;
    }

    public AssetLoader onComplete(Runnable callback){
        this.onCompleteCallback = callback;
        return this;
    }

    public void startLoading(){
        if(isLoading){
            System.out.println("Already loading");
            return;
        }

        if(assetsToLoad.isEmpty()){
            System.out.println("No assets to load");
            onLoadingComplete();
            return;
        }

        if(!isInitialized() || getApplication() == null){
            System.err.println("ERROR: AssetLoader not initialized! Cannot start loading.");
            return;
        }

        isLoading = true;
        loadedCount = 0;

        new Thread(()-> {
            for(AssetToLoad asset : assetsToLoad){
                try{
                    Object loadedAsset = loadAsset(asset);

                    synchronized (loadedAssets){
                        loadedAssets.put(asset.key, loadedAsset);
                    }
                    getApplication().enqueue(() -> {
                        loadedCount++;
                        float progress = (float) loadedCount/assetsToLoad.size();
                        loadingScreen.setProgress(progress);
                        loadingScreen.setLoadingText("Loading...(" + loadedCount + "/" + assetsToLoad.size() + ")");

                        System.out.println("Loaded " + asset.type + " : " + asset.path + " as '" + asset.key + "'");
                        if (loadedCount == assetsToLoad.size()) {
                            onLoadingComplete();
                        }
                        return null;
                    });

                } catch (Exception e) {
                    System.out.println("Faild to load " + asset.type + " : " + asset.path);
                    e.printStackTrace();

                    getApplication().enqueue(() -> {
                        loadedCount++;
                        float progress = (float) loadedCount/assetsToLoad.size();
                        loadingScreen.setProgress(progress);

                        if (loadedCount == assetsToLoad.size()) {
                            onLoadingComplete();
                        }
                        return null;
                    });
                }
            }
        }, "AssetLoaderThread").start();
    }

    private Object loadAsset(AssetToLoad asset){
        switch(asset.type){
            case MODEL:
                return assetManager.loadModel(asset.path);
            case TEXTURE:
                return assetManager.loadTexture(asset.path);
            case MATERIAL:
                return asset.path;
            default:
                throw new IllegalArgumentException("Unkown asset type : " + asset.type);
        }
    }

    private void onLoadingComplete() {
        System.out.println("All assets loaded successfully ! ("+ loadedAssets.size() + " assets)");
        loadingComplete = true;
        isLoading = false;

        getApplication().enqueue(() -> {
            if(loadingScreen.isInitialized()){
                getStateManager().detach(loadingScreen);
            }

            if(onCompleteCallback != null){
                onCompleteCallback.run();
            }

            return null;
        });
    }

    public Spatial getModel(String key){
        synchronized (loadedAssets){
            Object asset = loadedAssets.get(key);
            if(asset instanceof Spatial){
                return (Spatial) asset;
            }
        }
        System.err.println("Model not Found or not a Model : " + key);
        return null;
    }

    public Texture getTexture(String key){
        synchronized (loadedAssets){
            Object asset = loadedAssets.get(key);
            if(asset instanceof Texture){
                return (Texture) asset;
            }
        }
        System.err.println("Texture not Found or not a Texture : " + key);
        return null;
    }

    public Material getMaterial(String key){
        synchronized (loadedAssets){
            Object asset = loadedAssets.get(key);

            if(asset instanceof Material){
                return (Material) asset;
            } else if (asset instanceof String) {
                String materialPath = (String) asset;
                try {
                    Material mat = new Material(assetManager, materialPath);
                    loadedAssets.put(key, mat);
                    System.out.println("Created material on main thread: " + materialPath);
                    return mat;
                } catch (Exception e) {
                    System.err.println("Failed to create material from path: " + materialPath);
                    e.printStackTrace();
                    return null;
                }
            }
        }
        System.err.println("Material not Found or not a Material : " + key);
        return null;
    }


    public boolean isLoadingComplete(){
        return this.loadingComplete;
    }

    public boolean isLoading(){
        return this.isLoading;
    }

    public int getLoadedCount(){
        return this.loadedCount;
    }

    public int getTotalAssetCount(){
        return this.assetsToLoad.size();
    }


}