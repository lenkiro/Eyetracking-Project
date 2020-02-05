/*
package jme3test.helloworld;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;

/**
 * This is the Main Class of your Game. You should only do initialization here.
 * Move your Logic into AppStates or Controls
 * @author normenhansen
 *//*
public class HelloJME3 extends SimpleApplication {

    public static void main(String[] args) {
        HelloJME3 app = new HelloJME3();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        Box b = new Box(1, 1, 1);
        Geometry geom = new Geometry("Box", b);

        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Blue);
        geom.setMaterial(mat);

        rootNode.attachChild(geom);
    }

    @Override
    public void simpleUpdate(float tpf) {
        //TODO: add update code
    }

    @Override
    public void simpleRender(RenderManager rm) {
        //TODO: add render code
    }
}
*/

package jme3test.helloworld;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.plugins.ZipLocator;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.material.RenderState.FaceCullMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
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

/**
 * Example 9 - How to make walls and floors solid.
 * This collision code uses Physics and a custom Action Listener.
 * @author normen, with edits by Zathras
 */
public class HelloJME3 extends SimpleApplication
        implements ActionListener {

    private Spatial terrain;
    private BulletAppState bulletAppState;
    private RigidBodyControl landscape;
    private CharacterControl player;
    private Vector3f walkDirection = new Vector3f();
    private boolean left = false, right = false, up = false, down = false;

    //Temporary vectors used on each frame.
    //They here to avoid instanciating new vectors on each frame
    private Vector3f camDir = new Vector3f();
    private Vector3f camLeft = new Vector3f();

    public static void main(String[] args) {
      HelloJME3 app = new HelloJME3();
      app.start();
    }

    public void simpleInitApp() {
        /** Set up Physics */
        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);
        //bulletAppState.setDebugEnabled(true);

        // We re-use the flyby camera for rotation, while positioning is handled by physics
        viewPort.setBackgroundColor(new ColorRGBA(0.7f, 0.8f, 1f, 1f));
        flyCam.setMoveSpeed(100);
        setUpKeys();
        setUpLight();

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
        player.setPhysicsLocation(new Vector3f(0, 10, 0));

        // We attach the scene and the player to the rootnode and the physics space,
        // to make them appear in the game world.
        rootNode.attachChild(terrain);
        bulletAppState.getPhysicsSpace().add(landscape);
        bulletAppState.getPhysicsSpace().add(player);

        // You can change the gravity of individual physics objects after they are
        // added to the PhysicsSpace.
        player.setGravity(new Vector3f(0,-30f,0));

        char[][] mazeTest = {{'9','1','1','B'},  //                                     _       _   
                             {'2','8','A','3'},  //0: __ v  2: | <  4:I_^>  6: _I^<  8:I v>  A:  I v< 
                             {'2','4','6','3'},  //                                     _       _
                             {'5','0','0','7'}}; //1: __ ^  3: | >  5:I_v<  7: _Iv>  9:I ^<  B:  I ^>
        char[][] maze2 = {{'0','4','5','0'},
                          {'0','A','B','0'},
                          {'0','0','0','0'},
                          {'1','0','1','0'}};




        Node world = new Node("world");
        createMaze(mazeTest,30,30,world); 

        addCubeBlue(0,0,0);
        addCubeBlue(2,0,0);
        addCubeBlue(4,0,0);
        addCubeRed(0,0,2);
        addCubeRed(0,0,4);                                         

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


        rootNode.attachChild(world);
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
        System.out.println(walleMat.getParams());
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
        System.out.println(walleMat.getParams());
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
    inputManager.addListener(this, "Left");
    inputManager.addListener(this, "Right");
    inputManager.addListener(this, "Up");
    inputManager.addListener(this, "Down");
    inputManager.addListener(this, "Jump");
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
    } else if (binding.equals("Jump")) {
      if (isPressed) { player.jump(new Vector3f(0,20f,0));}
    }
  }

  /**
   * This is the main event loop--walking happens here.
   * We check in which direction the player is walking by interpreting
   * the camera direction forward (camDir) and to the side (camLeft).
   * The setWalkDirection() command is what lets a physics-controlled player walk.
   * We also make sure here that the camera moves with player.
   */
  @Override
    public void simpleUpdate(float tpf) {
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
            walkDirection.addLocal(camDir);
        }
        if (down) {
            walkDirection.addLocal(camDir.negate());
        }
        System.out.println(player.getPhysicsLocation());
        player.setWalkDirection(walkDirection);
        cam.setLocation(player.getPhysicsLocation());
        
    }
}