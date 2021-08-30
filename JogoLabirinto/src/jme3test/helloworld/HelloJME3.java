

package jme3test.helloworld;

import com.jme3.anim.AnimClip;
import com.jme3.anim.AnimComposer;
import com.jme3.anim.AnimTrack;
import com.jme3.anim.TransformTrack;
import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.Animation;
import com.jme3.animation.LoopMode;
import com.jme3.animation.SpatialTrack;
import com.jme3.app.SimpleApplication;
import com.jme3.asset.plugins.ZipLocator;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import static com.jme3.bullet.PhysicsSpace.getPhysicsSpace;
import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.PhysicsCollisionListener;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.bullet.control.GhostControl;
import com.jme3.bullet.control.PhysicsControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.material.RenderState.FaceCullMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Matrix4f;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.niftygui.NiftyJmeDisplay;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Quad;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.terrain.heightmap.AbstractHeightMap;
import com.jme3.terrain.heightmap.ImageBasedHeightMap;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import java.io.BufferedWriter;
import java.util.Random;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.lang.reflect.*;


public class HelloJME3 extends SimpleApplication
        implements ActionListener, ScreenController, PhysicsCollisionListener {

    private Spatial terrain;
    private BulletAppState bulletAppState;
    private RigidBodyControl landscape;
    private CharacterControl player;
    float airTime = 0;
    private Vector3f walkDirection = new Vector3f();
    private boolean left = false, right = false, up = false, down = false, exit = false;

    //Temporary vectors used on each frame.
    //They here to avoid instanciating new vectors on each frame
    private Vector3f camDir = new Vector3f();
    private Vector3f camLeft = new Vector3f();
    private Nifty nifty;
    private Node ghostControlToHub;
    private Node ghostControlNextLevel;
    private Node ghostControlMainKey;
    private Node collisionNode;
    private int fase = 0;
    private int noMapas;
    private Scanner scan;
    private Scanner config;
    private boolean hubWorld;
    private boolean mainKey = true;
    private Mapa[] mapas;
    private Node[] entrances;
    private BufferedWriter partidaBW;
    private GazeTrackerClient client;
    private GazeData data;
    private FakeEyeTracker fake;
    private int toggleWalkCounter = 0;
    private int toggleBackWalkCounter = 0;

    public static void main(String[] args){
        HelloJME3 app = new HelloJME3();
        app.setPauseOnLostFocus(false);
        app.start();
    }

    public void simpleInitApp() {
        flyCam.setEnabled(false); //set True to control the camera with the mouse
        String[] a = null;
        fake = new FakeEyeTracker();
        fake.main(a);
        client = new GazeTrackerClient("localhost", 3000, true);
        
        Date date = new Date(); 
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        writeOnPartidaTxt(formatter.format(date));
        setUpKeys();
        setUpLight();
        
        
        
        try {
            File mapFile = new File(System.getProperty("user.dir") + "\\src\\jme3test\\helloworld\\mapa.txt");
            this.scan = new Scanner(mapFile);
            this.noMapas = scan.nextInt();
            this.mapas = new Mapa[noMapas];
            for(int i = 0; i<this.noMapas;i++){
                int colunas = scan.nextInt();
                int linhas = scan.nextInt();
                scan.nextLine();
                this.mapas[i] = new Mapa();
                this.mapas[i].matriz  = new char [linhas][colunas];
                for(int y = 0;y<linhas;y++){
                    String s = scan.nextLine();
                    for(int x = 0; x<colunas;x++){
                        this.mapas[i].matriz[y][x] = s.charAt(x);
                    }
                }
                this.mapas[i].xchave = scan.nextInt();
                this.mapas[i].zchave = scan.nextInt();
                this.mapas[i].xobjetivo = scan.nextInt();
                this.mapas[i].zobjetivo = scan.nextInt();
                int numOfGates = scan.nextInt();
                this.mapas[i].numOfGates = numOfGates;
                this.mapas[i].xkey = new int[numOfGates];
                this.mapas[i].zkey = new int[numOfGates];
                this.mapas[i].xgate = new int[numOfGates];
                this.mapas[i].zgate = new int[numOfGates];
                this.mapas[i].gate = new Node[numOfGates];
                this.mapas[i].gateKey = new Node[numOfGates];
                this.mapas[i].hasGateKey = new boolean[numOfGates];
                this.mapas[i].gateOrientation = new String[numOfGates];
                this.mapas[i].gateColor = new String[numOfGates];
                for(int g = 0; g < numOfGates; g++){
                    this.mapas[i].gateColor[g] = scan.next();
                    this.mapas[i].gateOrientation[g] = scan.next();
                    this.mapas[i].xkey[g] = scan.nextInt();
                    this.mapas[i].zkey[g] = scan.nextInt();
                    this.mapas[i].xgate[g] = scan.nextInt();
                    this.mapas[i].zgate[g] = scan.nextInt();
                    this.mapas[i].hasGateKey[g] = false;
                }
                
            }
            
            this.config = new Scanner(new File(System.getProperty("user.dir") + "\\src\\jme3test\\helloworld\\config.txt"));
            config.findInLine("hubWorld=");
            this.hubWorld = config.nextBoolean();
            scan.close();
            config.close();
            
        } catch (FileNotFoundException ex) {
            Logger.getLogger(HelloJME3.class.getName()).log(Level.SEVERE, null, ex);
        }
        begin();
        if(this.hubWorld) createHubworldSelection();
        else ghostControlNextLevel = addCubeCollision(20*5,2,20*5,"Black");
        
        
        char[][] mazeTest = {{'9','2','1','B'},  //                                     _       _   
                             {'2','8','A','3'},  //0: __ v  2: | <  4:I_^>  6: _I^<  8:I v>  A:  I v< 
                             {'2','4','6','3'},  //                                     _       _
                             {'5','0','0','2'}}; //1: __ ^  3: | >  5:I_v<  7: _Iv>  9:I ^<  B:  I ^>
        char[][] maze2 = {{'0','4','5','0'},
                          {'0','A','B','0'},
                          {'0','0','0','0'},
                          {'1','0','1','0'}};
    }
    
    
    void createHubworldSelection(){
        entrances = new Node [this.noMapas];
        for(int i=0; i<noMapas;i++){
            this.entrances[i] = addCubeCollision(-20*(i-1),2,30*2,"Black");
        }
    }

    void begin(){
        /** Set up Physics */
        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);
        //bulletAppState.setDebugEnabled(true); //
        
        // We re-use the flyby camera for rotation, while positioning is handled by physics
        viewPort.setBackgroundColor(new ColorRGBA(0.7f, 0.8f, 1f, 1f));


        // We load the scene from the zip file and adjust its size.

        Material terrainMat = new Material(assetManager,
                "Common/MatDefs/Terrain/Terrain.j3md");
        terrainMat.setTexture("Alpha", assetManager.loadTexture(
                "Textures/Terrain/splat/alphamap.png"));
        AbstractHeightMap heightmap = null;
        Texture heightMapImage = assetManager.loadTexture(
                "Textures/Terrain/splat/mountains512.png");
        Texture grass = assetManager.loadTexture(
                "Textures/Terrain/splat/grass.jpg");
        grass.setWrap(WrapMode.Repeat);
        terrainMat.setTexture("Tex1", grass);
        terrainMat.setFloat("Tex1Scale", 64f);
        Texture dirt = assetManager.loadTexture(
                "Textures/Terrain/splat/dirt.jpg");
        dirt.setWrap(WrapMode.Repeat);
        terrainMat.setTexture("Tex2", dirt);
        terrainMat.setFloat("Tex2Scale", 32f);
        Texture rock = assetManager.loadTexture(
                "Textures/Terrain/splat/road.jpg");
        rock.setWrap(WrapMode.Repeat);
        terrainMat.setTexture("Tex3", rock);
        terrainMat.setFloat("Tex3Scale", 128f);
        heightmap = new ImageBasedHeightMap(heightMapImage.getImage());
        terrain = new TerrainQuad("my terrain", 65, 513, heightmap.getHeightMap());
        terrain.setMaterial(terrainMat);

        terrain.setLocalScale(2f);

        // We set up collision detection for the scene by creating a
        // compound collision shape and a static RigidBodyControl with mass zero.
        CollisionShape sceneShape =
                CollisionShapeFactory.createMeshShape(terrain);
        landscape = new RigidBodyControl(sceneShape, 0);
        terrain.addControl(landscape);
        
        
        
        /**
         * We set up collision detection for the player by creating
         * a capsule collision shape and a CharacterControl.
         * The CharacterControl offers extra settings for
         * size, stepheight, jumping, falling, and gravity.
         * We also put the player in its starting position.
         */
        CapsuleCollisionShape capsuleShape = new CapsuleCollisionShape(1.5f, 6f, 1);
        player = new CharacterControl(capsuleShape, 0.05f);
        player.setJumpSpeed(20);
        player.setFallSpeed(30);
        player.setPhysicsLocation(new Vector3f(20, 10, 20));
        player.setUp(new Vector3f(0, 1, 0)); 
        
        
        // We attach the scene and the player to the rootnode and the physics space,
        // to make them appear in the game world.
        rootNode.attachChild(terrain);
        bulletAppState.getPhysicsSpace().add(landscape);
        bulletAppState.getPhysicsSpace().add(player);

        // You can change the gravity of individual physics objects after they are
        // added to the PhysicsSpace.
        player.setGravity(new Vector3f(0,-30f,0));
        
        cam.setLocation(player.getPhysicsLocation());
        cam.lookAtDirection(new Vector3f(0, 0, 1), new Vector3f(0,1,0));
        
        up = false;
        down = false;
    }
    
    void fileMaze(int height, int width){
        if(fase <= noMapas){
            Node world = new Node("world");
            createMaze(mapas[this.fase-1].matriz,20,20,world); 
        }else{
        }
    }

    void resetAll(){
        rootNode.detachAllChildren();
    }

    void randomMaze(int height, int width){
        Random rand = new Random();
        char[] caracteres = {'0','1','2','3','4','5','6','7','8','9','A','B'};
        char[][] maze = new char [6][6];
        for(int i = 0; i<maze.length; i++){
            for(int j = 0; j<maze[i].length; j++){
                maze[i][j] = caracteres[rand.nextInt(caracteres.length)];
            }
        }
        createMaze(maze, height, width, new Node("world"));
    }
  
    
    
    void createMaze(char[][] matriz, int height, int width, Node world){
        for(int i = 0; i<matriz.length; i++){
            for(int j = 0; j<matriz[i].length; j++){
                switch(matriz[i][j]){
                    case '0':
                        addWall(height, width,j,0,i,world,"dh");
                        break;
                    case '1':
                        addWall(height, width,j,0,i,world,"uh");
                        break;
                    case '2':
                        addWall(height, width,j,0,i,world,"lv");
                        break;
                    case '3':
                        addWall(height, width,j,0,i,world,"rv");
                        break;

                    case '4':
                        addHalfWall(height, width,j,0,i,world,"ruh");
                        addHalfWall(height, width,j,0,i,world,"trv");
                        break;

                    case '5':
                        addHalfWall(height, width,j,0,i,world,"rdh");
                        addHalfWall(height, width,j,0,i,world,"tlv");
                        break;

                    case '6':
                        addHalfWall(height, width,j,0,i,world,"luh");
                        addHalfWall(height, width,j,0,i,world,"tlv");
                        break;

                    case '7':
                        addHalfWall(height, width,j,0,i,world,"ldh");
                        addHalfWall(height, width,j,0,i,world,"trv");
                        break;

                    case '8':
                        addHalfWall(height, width,j,0,i,world,"rdh");
                        addHalfWall(height, width,j,0,i,world,"brv");
                        break;
                    case '9':
                        addHalfWall(height, width,j,0,i,world,"ruh");
                        addHalfWall(height, width,j,0,i,world,"blv");
                        break;
                    case 'A':
                        addHalfWall(height, width,j,0,i,world,"ldh");
                        addHalfWall(height, width,j,0,i,world,"blv");
                        break;
                    case 'B':
                        addHalfWall(height, width,j,0,i,world,"luh");
                        addHalfWall(height, width,j,0,i,world,"brv");
                        break;
                }
                
            }
        }

        addCeiling(matriz.length*width,matriz[0].length*width,height,world);
        rootNode.attachChild(world);
    }
    
    void writeOnPartidaTxt(String message){
        
        try{
            File partida = new File(System.getProperty("user.dir") + "\\src\\jme3test\\helloworld\\partida.txt");
            FileWriter fw = new FileWriter(partida, true);
            this.partidaBW = new BufferedWriter(fw);
            partidaBW.write(message);
            partidaBW.newLine();
            partidaBW.close();
        }
        catch(Exception e){
            System.out.println("Oh no");
            System.out.println(e);
        }
    }
    
    void addWall(float height, float width, float moveX, float moveY, float moveZ, Node world, String version){
        Quad quad = new Quad(width,height);
        Geometry walle = new Geometry("Wall-e",quad);
        Material walleMat = new Material(assetManager,"Common/MatDefs/Light/Lighting.j3md");

        Texture image = assetManager.loadTexture("Textures/DuplicatedBrickWall.jpg");

        walleMat.setTexture("DiffuseMap", image);
        walleMat.setBoolean("UseMaterialColors",true);
        walleMat.setColor("Diffuse",ColorRGBA.White);  // minimum material color
        walleMat.setColor("Specular",ColorRGBA.White); // for shininess
        walleMat.setFloat("Shininess", 8f); // [1,128] for shininess
        walleMat.setColor("Ambient",ColorRGBA.White.mult(0.3f));
        walleMat.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Off);
        walle.setMaterial(walleMat);
        Node walleNod = new Node("walleNod");
        world.attachChild(walleNod);
        walleNod.attachChild(walle);
        
        
        switch(version){
            case "dh":
                walleNod.move(moveX*width , moveY*height, moveZ*width + width/2);
                walleNod.rotate(0,0,0);
                break;
            case "uh":
                walleNod.move((moveX+1)*width, moveY*height, moveZ*width + width/2);
                walleNod.rotate(0,(float)Math.PI,0);
                break;
            case "lv":
                walleNod.move(moveX*width + width/2, moveY*height, moveZ*width);
                walleNod.rotate(0,3*(float)Math.PI/2,0);
                break;
            case "rv":
                walleNod.move(moveX*width + width/2, moveY*height, (1+moveZ)*width);
                walleNod.rotate(0,(float)Math.PI/2,0);
                break;
            
        }
        


        CollisionShape walleCol = CollisionShapeFactory.createMeshShape(walle);
        RigidBodyControl walleBod = new RigidBodyControl(walleCol, 0);
        walle.addControl(walleBod);
        bulletAppState.getPhysicsSpace().add(walleBod);
    }
    
    void addHalfWall(float height, float width, float moveX, float moveY, float moveZ, Node world, String version){
        Quad quad = new Quad(width/2,height);
        Geometry walle = new Geometry("Wall-e",quad);
        Material walleMat = new Material(assetManager,"Common/MatDefs/Light/Lighting.j3md");

        Texture image = assetManager.loadTexture("Textures/BrickWall.jpg");

        walleMat.setTexture("DiffuseMap", image);
        walleMat.setBoolean("UseMaterialColors",true);
        walleMat.setColor("Diffuse",ColorRGBA.White);  // minimum material color
        walleMat.setColor("Specular",ColorRGBA.White); // for shininess
        walleMat.setFloat("Shininess", 8f); // [1,128] for shininess
        walleMat.setColor("Ambient",ColorRGBA.White.mult(0.3f));
        walleMat.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Off);
        walle.setMaterial(walleMat);
        Node walleNod = new Node("walleNod");
        world.attachChild(walleNod);
        walleNod.attachChild(walle);
        
        
        switch(version){
            case "ldh":
                walleNod.move(moveX*width , moveY*height, moveZ*width + width/2);
                walleNod.rotate(0,0,0);
                break;
            case "luh":
                walleNod.move((moveX+1)*width - width/2, moveY*height, moveZ*width + width/2);
                walleNod.rotate(0,(float)Math.PI,0);
                break;
            case "rdh":
                walleNod.move(moveX*width + width/2, moveY*height, moveZ*width + width/2);
                walleNod.rotate(0,0,0);
                break;
            case "ruh":
                walleNod.move((moveX+1)*width, moveY*height, moveZ*width + width/2);
                walleNod.rotate(0,(float)Math.PI,0);
                break;
            case "tlv":
                walleNod.move(moveX*width + width/2, moveY*height, moveZ*width);
                walleNod.rotate(0,3*(float)Math.PI/2,0);
                break;
            case "trv":
                walleNod.move(moveX*width + width/2, moveY*height, (1+moveZ)*width - width/2);
                walleNod.rotate(0,(float)Math.PI/2,0);
                break;    
            case "blv":
                walleNod.move(moveX*width + width/2, moveY*height, moveZ*width + width/2);
                walleNod.rotate(0,3*(float)Math.PI/2,0);
                break;
            case "brv":
                walleNod.move(moveX*width + width/2, moveY*height, (1+moveZ)*width);
                walleNod.rotate(0,(float)Math.PI/2,0);
                break;
        }
        
        CollisionShape walleCol = CollisionShapeFactory.createMeshShape(walle);
        RigidBodyControl walleBod = new RigidBodyControl(walleCol, 0);
        walle.addControl(walleBod);
        bulletAppState.getPhysicsSpace().add(walleBod);
    }
    
    Node addCubeCollision(float x, float y, float z,String colorStr){
        GhostControl ghostControl = new GhostControl(new BoxCollisionShape(new Vector3f(1,1,1)));  // a box-shaped ghost
        
        Node cNode = new Node("cNode");
        // Optional: Add a Geometry, or other controls, to the node if you need to
        
        // attach everything to activate it
        Box b = new Box(1, 1, 1);
        Geometry geom = new Geometry("Box", b);
        
        cNode.attachChild(geom);
        cNode.move(x, y, z);
        cNode.addControl(ghostControl);                         // the ghost follows this node
        
        
        
        CollisionShape wallShape = CollisionShapeFactory.createBoxShape(geom);
        RigidBodyControl thing = new RigidBodyControl(wallShape, 0);
        
        rootNode.attachChild(cNode);
        
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        
        ColorRGBA color = colorStrToRGBA(colorStr);
        mat.setColor("Color", color);
        geom.setMaterial(mat);
        bulletAppState.getPhysicsSpace().add(thing);
        
        getPhysicsSpace().add(ghostControl);
        return cNode;
        
        
    }
    
    void addColoredCube(float x, float y, float z, ColorRGBA color){
        Box b = new Box(1, 2, 1);
        Geometry geom = new Geometry("Box", b);
        Node cNode = new Node("cNode"); 
        cNode.attachChild(geom);
        cNode.move(x, y, z);



        CollisionShape wallShape = CollisionShapeFactory.createBoxShape(geom);
        RigidBodyControl thing = new RigidBodyControl(wallShape, 0);

        rootNode.attachChild(cNode);

        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", color);

        geom.setMaterial(mat);
        bulletAppState.getPhysicsSpace().add(thing);
    }

    void addCubeRed(float x, float y, float z){
        Box b = new Box(1, 20, 1);
        Geometry geom = new Geometry("Box", b);
        Node cNode = new Node("cNode"); 
        cNode.attachChild(geom);
        cNode.move(x, y, z);



        CollisionShape wallShape = CollisionShapeFactory.createBoxShape(geom);
        RigidBodyControl thing = new RigidBodyControl(wallShape, 0);

        rootNode.attachChild(cNode);

        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Red);

        geom.setMaterial(mat);
        bulletAppState.getPhysicsSpace().add(thing);
    }

    void addCubeBlue(float x, float y, float z){
        Box b = new Box(1, 20, 1);
        Geometry geom = new Geometry("Box", b);
        Node cNode = new Node("cNode"); 
        cNode.attachChild(geom);
        cNode.move(x, y, z);



        CollisionShape wallShape = CollisionShapeFactory.createBoxShape(geom);
        RigidBodyControl thing = new RigidBodyControl(wallShape, 0);

        rootNode.attachChild(cNode);

        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Blue);
        geom.setMaterial(mat);
        bulletAppState.getPhysicsSpace().add(thing);
    }

    void addCeiling(float width, float length,float height, Node world){
        Quad quad = new Quad(width,length);
        Geometry walle = new Geometry("Ceiling",quad);
        Material walleMat = new Material(assetManager,"Common/MatDefs/Light/Lighting.j3md");

        Texture image = assetManager.loadTexture("Textures/BrownCeiling.jpg");

        walleMat.setTexture("DiffuseMap", image);
        walleMat.setBoolean("UseMaterialColors",true);
        walleMat.setColor("Diffuse",ColorRGBA.White);  // minimum material color
        walleMat.setColor("Specular",ColorRGBA.White); // for shininess
        walleMat.setFloat("Shininess", 8f); // [1,128] for shininess
        walleMat.setColor("Ambient",ColorRGBA.White.mult(0.3f));
        walleMat.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Off);
        walle.setMaterial(walleMat);
        Node walleNod = new Node("walleNod");
        world.attachChild(walleNod);
        walleNod.attachChild(walle);

        walleNod.move(0,height+0.1f,0);
        walleNod.rotate(3*(float)Math.PI/2,3*(float)Math.PI/2,0);
    }

    private void setUpLight() {
        // We add light so we see the scene
        AmbientLight al = new AmbientLight();
        al.setColor(ColorRGBA.White.mult(1.3f));
        rootNode.addLight(al);

        DirectionalLight dl = new DirectionalLight();
        dl.setColor(ColorRGBA.White);
        dl.setDirection(new Vector3f(2.8f, -2.8f, -2.8f).normalizeLocal());
        rootNode.addLight(dl);
    }

  /** We over-write some navigational key mappings here, so we can
   * add physics-controlled walking and jumping: */
    private void setUpKeys() {
        inputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_D));
        inputManager.addMapping("Up", new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping("Down", new KeyTrigger(KeyInput.KEY_S));
        inputManager.addMapping("Jump", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addMapping("Exit", new KeyTrigger(KeyInput.KEY_ESCAPE));
        
        inputManager.addListener(this, "Left");
        inputManager.addListener(this, "Right");
        inputManager.addListener(this, "Up");
        inputManager.addListener(this, "Down");
        inputManager.addListener(this, "Jump");
        inputManager.addListener(this, "Exit");
    }
  
    Node addGate(float height, float width, float moveX, float moveY, float moveZ, Node world, String version, String colorStr){
        Box quad = new Box(width, height, 1);
        //Could be CSGBox
        Geometry walle = new Geometry("Box", quad);
      
      
        Material walleMat = new Material(assetManager,"Common/MatDefs/Light/Lighting.j3md");

        Texture image = assetManager.loadTexture("Textures/MetalGate.jpg");

        walleMat.setTexture("DiffuseMap", image);
        walleMat.setBoolean("UseMaterialColors",true);
        
        
        
        ColorRGBA color = colorStrToRGBA(colorStr);
        walleMat.setColor("Diffuse", color);  // minimum material color
        walleMat.setColor("Ambient",color.mult(0.3f));
        
        walleMat.setColor("Specular",ColorRGBA.White); // for shininess
        walleMat.setFloat("Shininess", 8f); // [1,128] for shininess
        walle.setMaterial(walleMat);
        Node walleNod = new Node("walleNod");
        world.attachChild(walleNod);
        walleNod.attachChild(walle);
        
        
        switch(version){
            case "Horizontal":
                walleNod.move(moveX*2*width, moveY*height + height, moveZ*2*width - width);
                walleNod.rotate(0,0,0);
                break;
            case "Vertical":
                walleNod.move(moveX*2*width - width, moveY*height + height + 0.1f, moveZ*2*width);
                walleNod.rotate(0,3*(float)Math.PI/2,0);
                break;
            
        }
        
        GhostControl ghostControl = new GhostControl(new BoxCollisionShape(new Vector3f(width*1.0f,height,1.0f)));
        walleNod.addControl(ghostControl);   
        getPhysicsSpace().add(ghostControl);


        CollisionShape walleCol = CollisionShapeFactory.createMeshShape(walle);
        RigidBodyControl walleBod = new RigidBodyControl(walleCol, 0);
        walle.addControl(walleBod);
        bulletAppState.getPhysicsSpace().add(walleBod);
        return walleNod;
    }
  
    public void createGateAnimation(int faseAtual){ //this way of making animation is deprecated after jdk 3.2, maybe one I'll update to one similar to the commented one below (which doesn't work)
        if(faseAtual > 0 && mapas[faseAtual-1].gate != null){
                for(int j = 0;j<this.mapas[faseAtual-1].numOfGates;j++){
                    float animTime = 5;
                    int fps = 25;
                    float totalXLength = 19;

                    //calculating frames
                    int totalFrames = (int) (fps * animTime);
                    float dT = animTime / totalFrames, t = 0;
                    float dX = totalXLength / totalFrames, x = 0;
                    float[] times = new float[totalFrames];
                    Vector3f[] translations = new Vector3f[totalFrames];
                    Quaternion[] rotations = new Quaternion[totalFrames];
                    Vector3f[] scales = new Vector3f[totalFrames];
                    for (int i = 0; i < totalFrames; ++i) {
                        times[i] = t;
                        t += dT;
                        rotations[i] = mapas[faseAtual-1].gate[j].getLocalRotation();
                        scales[i] = Vector3f.UNIT_XYZ;
                        translations[i] = new Vector3f(mapas[faseAtual-1].gate[j].getLocalTranslation().getX(), x+10, mapas[faseAtual-1].gate[j].getLocalTranslation().getZ());
                        x += dX;
                        
                    }
                    /*
                    Node gate = mapas[faseAtual-1].gate[j];
                    TransformTrack transformTrack = new TransformTrack(gate.getChild(0), times, translations, rotations, scales);
                    
                    // creating the animation
                    AnimClip animClip = new AnimClip("anim");
                    animClip.setTracks(new AnimTrack[] { transformTrack});

                    // create spatial animation control
                    AnimComposer animComposer = new AnimComposer();
                    animComposer.addAnimClip(animClip);

                    gate.addControl(animComposer);
                    
                    */
                    SpatialTrack spatialTrack = new SpatialTrack(times, translations, rotations, scales);

                    //creating the animation
                    Animation spatialAnimation = new Animation("anim", animTime);
                    spatialAnimation.setTracks(new SpatialTrack[] { spatialTrack });

                    //create spatial animation control
                    AnimControl control = new AnimControl();
                    HashMap<String, Animation> animations = new HashMap<String, Animation>();
                    animations.put("anim", spatialAnimation);
                    control.setAnimations(animations);
                    mapas[faseAtual-1].gate[j].addControl(control);
                    //rootNode.attachChild(model);
                    
                }
        }
    }
  

  /** These are our custom actions triggered by key presses.
   * We do not walk yet, we just keep track of the direction the user pressed. */
    public void onAction(String binding, boolean isPressed, float tpf) {
        if (binding.equals("Left")) {
          left = isPressed;
        } else if (binding.equals("Right")) {
          right= isPressed;
        } else if (binding.equals("Up")) {
          up = isPressed;
        } else if (binding.equals("Down")) {
          down = isPressed;
        } else if (binding.equals("Exit")) {
          exit = isPressed;
        }else if (binding.equals("Jump")) {
            if (isPressed) {
                if(player.onGround()){
                    player.jump(new Vector3f(0,20f,0));}
                }

        }
    }
  
    ColorRGBA colorStrToRGBA(String colorStr){
        ColorRGBA rgba;
        Field colorField;
        try {
            colorField = ColorRGBA.class.getField(colorStr);
            rgba = (ColorRGBA) colorField.get(ColorRGBA.class);
        }catch(Exception ex){
            Logger.getLogger(HelloJME3.class.getName()).log(Level.SEVERE, null, ex);
            System.out.print("Color Defaulted to Red");
            rgba = ColorRGBA.Red;
        }
        return rgba;
    }  

  /**
   * This is the main event loop--walking happens here.
   * We check in which direction the player is walking by interpreting
   * the camera direction forward (camDir) and to the side (camLeft).
   * The setWalkDirection() command is what lets a physics-controlled player walk.
   * We also make sure here that the camera moves with player.
   */
    
  @Override
    public void simpleUpdate(float tpf){
        try{
            data = client.readGazeData();
            //System.out.println(data.getX() + " " + data.getY() + " " + data.getTimestamp()+ " " + data.isValid() + " " + cam.getDirection() + " " + player.getPhysicsLocation()); 
            
        }
        
        catch(Exception e){
            System.out.println(e);
        }
        
        if(data.getY() <= 150 && data.getX() >= 400 && data.getX() <= 1100){
            up = true;
        }
        
        if(data.getY() >= 700 && data.getX() >= 400 && data.getX() <= 1100){
            up = false;
        }
        
        //an attempt at an timed toggle for walking forward and backward
//        if(data.getY() <= 250 && data.getX() >= 400 && data.getX() <= 1100){
//            if(toggleWalkCounter >= 1200){
//                up = !up;
//                down = false;
//                toggleWalkCounter = 0;
//            }else{
//                toggleWalkCounter++;
//            }
//        }else{
//            toggleWalkCounter = 0;
//        }
//        
//        if(data.getY() >= 600 && data.getX() >= 400 && data.getX() <= 1100){
//            if(toggleBackWalkCounter >= 1200){
//                down = !down;
//                up = false;
//                toggleBackWalkCounter = 0;
//            }else{
//                toggleBackWalkCounter++;
//            }
//        }else{
//            toggleBackWalkCounter = 0;
//        }
        
        if(data.getX() <= 250 || data.getX() >= 1250){
            Vector3f vup = new Vector3f(0,1,0);
            Vector3f vAtual = cam.getDirection();
            Vector3f vecFinal;
            if(data.getX() <= 250){
                vecFinal = cam.getLeft();
            }else{
                vecFinal = cam.getLeft().negate();
            }
            for(int v = 0; v < 300; v++){
                vecFinal = vecFinal.add(vAtual);
            }

            cam.lookAtDirection(vecFinal, vup);
        }
        
        
        camDir.set(cam.getDirection()).multLocal(0.6f);
        
        camLeft.set(cam.getLeft()).multLocal(0.4f);
        
        walkDirection.set(0, 0, 0);
        if (left) {
            walkDirection.addLocal(camLeft);
        }
        if (right) {
            walkDirection.addLocal(camLeft.negate());
        }
        if (up) {
            //divide each by the norm (which is achieved by using pithagorean theorem) so that forward speed becomes independent of camera y axis
            //It doesn't matter in this game though because with the eyetracking controls it isn't possible to look up or down anyway
            walkDirection.addLocal(camDir.x*2/3,0,camDir.z*2/3);
        }
        if (down) {
            walkDirection.addLocal(-camDir.x*2/3,0,-camDir.z*2/3);
        }
        if (exit) {
            fake.exit();
        }
        if (!player.onGround()) {
            airTime = airTime + tpf;
        } else {
            airTime = 0; 
        }
        player.setWalkDirection(walkDirection);
        cam.setLocation(player.getPhysicsLocation());
        writeOnPartidaTxt("Mouse:" + this.inputManager.getCursorPosition().toString() + " L: " + left+ " R: " + right + " U: " + up + " D: " + down + " Player Location: " + player.getPhysicsLocation() + " Camera Direction: " + cam.getDirection());
        if(hubWorld){
            for(int i = 0; i<this.noMapas;i++){
                if(entrances[i] != null){
                    if(entrances[i].getControl(GhostControl.class).getOverlappingObjects().contains(player)){
                        resetAll();
                        begin();
                        Node world = new Node("world");
                        Mapa mapaAtual = this.mapas[i];
                        for(int j = 0; j<mapaAtual.hasGateKey.length;j++){
                            mapaAtual.hasGateKey[j] = false;
                        }
                        
                        createMaze(mapaAtual.matriz,20,20,world);
                        ghostControlToHub = addCubeCollision(20*mapaAtual.xobjetivo,2,20*mapaAtual.zobjetivo,"Black");
                        ghostControlMainKey = addCubeCollision(20*mapaAtual.xchave,2,20*mapaAtual.zchave,"Brown");
                        this.mainKey = false;
                        this.fase = i + 1;
                        for(int j = 0; j<mapaAtual.numOfGates;j++){
                            mapas[this.fase-1].gate[j] = addGate(10, 10, mapaAtual.xgate[j], 0, mapaAtual.zgate[j], world, mapaAtual.gateOrientation[j],mapaAtual.gateColor[j]);
                            mapas[this.fase-1].gateKey[j] = addCubeCollision(20*mapaAtual.xkey[j], 2 , 20*mapaAtual.zkey[j], mapaAtual.gateColor[j]);
                        }
                        createGateAnimation(this.fase);
                    }
                }
            }
        }
        if(this.fase > 0 && mapas[this.fase-1].gate != null){
            for(int j = 0;j<this.mapas[this.fase-1].numOfGates;j++){
                if(mapas[this.fase-1].gateKey[j].getControl(GhostControl.class).getOverlappingObjects().contains(player)){
                    mapas[this.fase-1].hasGateKey[j] = true;
                    mapas[this.fase-1].gateKey[j].removeFromParent();
                }
                
                if((!mapas[this.fase-1].gate[j].getControl(GhostControl.class).getOverlappingObjects().contains(rootNode.getChild(0).getControl(RigidBodyControl.class)) ||mapas[this.fase-1].gate[j].getControl(GhostControl.class).getOverlappingObjects().contains(player)) && mapas[this.fase-1].hasGateKey[j]){
                    //mapas[this.fase-1].gate[j].getControl(AnimComposer.class).setCurrentAction("anim");
                    //the above code would be the way to use animation without using deprecated libraries.
                    if(mapas[this.fase-1].gate[j].getControl(AnimControl.class).getNumChannels() < 1){
                        //run animation
                        
                        AnimChannel anime = mapas[this.fase-1].gate[j].getControl(AnimControl.class).createChannel();
                        anime.setAnim("anim");
                        anime.setLoopMode(LoopMode.DontLoop);
                        
                    }
                    
                    else{
                        
                        AnimChannel aniChannel = mapas[this.fase-1].gate[j].getControl(AnimControl.class).getChannel(0);
                        if(aniChannel.getTime() >= aniChannel.getAnimMaxTime()/2){
                            bulletAppState.getPhysicsSpace().remove(mapas[this.fase-1].gate[j].getChild(0).getControl(RigidBodyControl.class));
                        }
                        
                        
                    }
                

                }
        }
        if(ghostControlMainKey != null)
            if(ghostControlMainKey.getControl(GhostControl.class).getOverlappingObjects().contains(player)){
                this.mainKey = true;
                ghostControlMainKey.removeFromParent();
            }
        if(ghostControlToHub != null)
            if(ghostControlToHub.getControl(GhostControl.class).getOverlappingObjects().contains(player) && this.mainKey == true){
                resetAll();
                this.fase = 0;
                begin();
                createHubworldSelection();
            }
        if(ghostControlNextLevel != null)
            if(ghostControlNextLevel.getControl(GhostControl.class).getOverlappingObjects().contains(player) && this.mainKey){
                resetAll();
                begin();
                this.fase++;
                fileMaze(20,20);
                if(this.fase < this.noMapas)
                ghostControlMainKey = addCubeCollision(20*mapas[this.fase -1].xchave,2,20*mapas[this.fase -1].zchave,"Brown");
                ghostControlNextLevel = addCubeCollision(20*mapas[this.fase -1].xobjetivo,2,20*mapas[this.fase-1].zobjetivo,"Black");
                this.mainKey = false;
            }
        }
    }
    
    private PhysicsSpace getPhysicsSpace(){
        return bulletAppState.getPhysicsSpace();
    }
    
    @Override
    public void bind(Nifty nifty, Screen screen) {
        System.out.println("bind( " + screen.getScreenId() + ")");
    }

    @Override
    public void onStartScreen() {
        System.out.println("onStartScreen");
    }

    @Override
    public void onEndScreen() {
        System.out.println("onEndScreen");
    }

    public void quit(){
        nifty.gotoScreen("end");
    }

    @Override
    public void collision(PhysicsCollisionEvent event) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    
}

class Mapa{
    char[][] matriz;
    int xchave,zchave,xobjetivo,zobjetivo;
    int numOfGates,xkey[], zkey[], xgate[], zgate[];
    boolean hasGateKey[];
    String gateOrientation [];
    String gateColor[];
    Node gate[], gateKey[];
    
    public Mapa(){
    }
}

/*
Fontes:
    Imagens: https://i.pinimg.com/originals/76/99/ba/7699ba4de8e66f222c848105c6ccfa1f.jpg
             https://www.wildtextures.com/wp-content/uploads/wildtextures-leather-Campo-darkbrown.jpg
*/
/*
13/03
Estudar Nifty por fora
Montar troca de mundos na mão
Se não conseguir fazer if detectar colisão, fazer detectar posição no mundo
24/03
Fazer o código receber labirintos por arquivos txt
Fazer txt de configuração (definir se, após uma fase, volta ao 'menu')
Resolver problema dos cubos vermelhos e da luz
Implementar movimentação mais realista
04/06
Fazer mapas mais complexos com o esquema de chave para liberar saída
Ver como capturar todos os inputs de teclas e mouse
30/07
Fazer com que coordenadas da câmera e teclas pressionadas sejam gravadas num txt "partida"
27/08
Entender as informações da câmera e colocar posição do personagem e informações do mouse no partida.txt
01/10
Fazer animação para porta e ter múltiplas portas
19/11
Fazer teto, semáforo e colisão seguir porta
10/12
Colocar cores nas chaves e portas
Completar o teto
Colisão porta
Impedir personagem de girar para trás
01/04
Resolver memory leak
*/