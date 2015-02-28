package psyknz.libgdx.orbgame.misc;

import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;

public class CameraController extends InputAdapter{
	
	public static final int FIT_TO_SCREEN = 0;	// When resized the entire area defined by the camera will be inside of the window.
	public static final int FILL_SCREEN = 1;	// When resized the camera will fill entire window while maintaining its aspect ratio.
	public static final int STRETCH_TO_FIT = 2;	// When resized the area defined by the camera will be stretched to the edges of the window.
	
	private Camera camera;	// Reference to the camera this is the controller for.
	
	public int baseWidth, baseHeight;		// Width and height the camera is supposed to be.
	public int fitType;						// How the camera should be fitted to screens with different dimensions than the camera.
	
	private int screenWidth, screenHeight;	// The physical dimensions of the screen.
	private Rectangle viewArea;				// Rectangle representing the generated viewport and its location.
	private float zoom = 1.0f;				// Zoom factor of the camera relative to its sizing after being fitted to the screen.
	
	public boolean reversableAspectRatio = false;	// Whether the cameras aspect ratio can be reversed to better fit the screen ratio.
	
	private Vector3 touchNew, touchOld;	// Temporary three dimensional vector used to process touch input.
	
	/**
	 * Creates a new camera controller to manage the given camera. Designated the target width and height of the camera as well as the
	 * the method which should be used to fit to a screen of different dimensions
	 * @param camera The camera which should be controlled by this object.
	 * @param width Intended width of the viewport, is adjusted to provide best fit to the screen.
	 * @param height Intended height of the viewport, is adjusted to provide best fit to the screen.
	 * @param fillType Method that should be used to fit the camera to the screens dimensions.
	 */
	public CameraController(Camera camera, int width, int height, int fitType) {
		this.camera = camera;		// Configures the controller based on the constructor arguments.
		this.baseWidth = width;
		this.baseHeight = height;
		this.fitType = fitType;
		
		viewArea = new Rectangle();	// Instantiates a new blank rectangle to store information about the viewport.
		
		touchNew = new Vector3();	// Instantiates a blank Vector3 for processing input.
		touchOld = new Vector3();	// Instantiates a blank Vector3 for storing last recorded input.
	}
	
	/**
	 * Resizes the camera to fit a screen of the given dimensions using the aspect ratio and fitting rules selected.
	 * @param width
	 * @param height
	 */
	public void resize(int width, int height) {
		screenWidth = width;	// Records the physical width of the screen.
		screenHeight = height;	// Records the physical height of the screen.
		
		float screenAspectRatio = (float) width / height;			// Determines the aspect ratio of the resized window.
		float cameraAspectRatio = (float) baseWidth / baseHeight;	// Determines the target aspect ratio for the camera.
		
		if(reversableAspectRatio && 										// If the camera aspect ratio can be reversed
				((screenAspectRatio > 1 && cameraAspectRatio < 1) || 		// and aspect ratio is substantially wider
				(screenAspectRatio < 1 && cameraAspectRatio > 1))) 			// or aspect ratio is substantially thinner than the screen
					viewArea.setSize(baseHeight / zoom, baseWidth / zoom);	// the aspect ratio is reversed to better fit the screen.
		else viewArea.setSize(baseWidth / zoom, baseHeight / zoom);			// Otherwise the original aspect ratio is enforced.
		
		cameraAspectRatio = viewArea.width / viewArea.height;	// Determines the real aspect ratio for the viewport.
		
		switch(fitType) {													// Viewport width and height set based on method of fitting.
			case FIT_TO_SCREEN:												// If the camera needs to fit entirely inside of the screen
				if(cameraAspectRatio > screenAspectRatio) 					// and the camera is wider than the screen
					viewArea.height = viewArea.width / screenAspectRatio;	// height is adjusted to maintain the aspect ratio.
				else viewArea.width = viewArea.height * screenAspectRatio;	// Otherwise width is adjusted for the aspect ratio.
				break;
				
			case FILL_SCREEN:												// If the camera needs to fill the entire screen
				if(cameraAspectRatio > screenAspectRatio) 					// and the camera is wider than the screen
					viewArea.width = viewArea.height / screenAspectRatio;	// width is adjusted to maintain the aspect ratio.
				else viewArea.height = viewArea.width * screenAspectRatio;	// Otherwise height is adjusted to maintain the aspect ratio.
				break;
				
			default: break;	// By default stretch to fit is the method of fitting used, which does not maintain the aspect ratio.
		}
		
		camera.viewportWidth = viewArea.width;		// Sets the new viewport width for the camera.
		camera.viewportHeight = viewArea.height;	// Sets the new viewport height for the camera.
		update();									// The changes are applied.
	}
	
	/**
	 * Updates the camera and the controllers representation of the viewport.
	 */
	public void update() {
		camera.update();												// Updates te camera.
		viewArea.setPosition(camera.position.x - viewArea.width / 2,	// Centres the Rectangle representing the viewport over top
				camera.position.y - viewArea.height / 2);				// of the viewport area.
	}
	
	/**
	 * Sets the zoom factor for the camera. @see resize(float, float) must be called at least once before setting the zoom.
	 * @param zoom How zoomed in or out you want the camera. 
	 */
	public void setZoom(float zoom) {
		assert zoom > 0;					// The zoom factor may never be 0 or less to avoid division by 0.
		this.zoom = zoom;					// Updates the zoom factor
		resize(screenWidth, screenHeight);	// and then resizes the camera to apply the adjustment to the zoom.
	}
	
	/**
	 * @return Current zoom factor of the camera.
	 */
	public float getZoom() {
		return zoom;
	}
	
	/**
	 * @return Rectangle representing the view area in in-game units.
	 */
	public Rectangle getViewArea() {
		return viewArea;
	}
	
	@Override
	/**
	 * Records the point where the user has touched to allow motion processing.
	 */
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		touchNew.set(screenX, screenY, 0);	// Records the position of the touch
		camera.unproject(touchNew);			// and converts it into in-game units.
		return true;						// Prevents any other input from being processed.
	}
	
	@Override
	/**
	 * When the user drags their finger across the screen the camera is moved by an equivalent amount.
	 */
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		touchOld.set(touchNew);							// The last touch is stored before
		touchNew.set(screenX, screenY, 0);				// the new touch is recorded
		camera.unproject(touchNew);						// and converted into in-game units.
		camera.position.sub(touchNew.x - touchOld.x, 	// The difference between the new and old touches is subtracted from
				touchNew.y - touchOld.y, 0);			// the cameras position.
		update();										// The camera and the viewport Rectangle are updated.
		return true;									// Al other input is prevented.
	}
	
	@Override
	/**
	 * 
	 */
	public boolean scrolled(int amount) {
		System.out.println("User scrolled by " + amount + " units");
		return false;
	}
}
