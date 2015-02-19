package psyknz.libgdx.orbgame;

import java.util.Arrays;

import com.badlogic.gdx.math.Rectangle;

public class UITable {
	
	public float tablePadding;						//
	public boolean useProportionalPadding = true;	//
	
	private float[][] cells;			//
	private Rectangle[][] cellBounds;	//
	
	/**
	 * @param padding */
	public UITable(float padding) {
		tablePadding = padding;			//
		cells = new float[0][0];		//
	}
	
	/**
	 * @param height
	 * @param widths */
	public void addRow(float height, float... widths) {
		cells = Arrays.copyOf(cells, cells.length + 1);					//
		float[] vals = new float[widths.length + 1];					//
		vals[0] = height;												//
		for(int i = 1; i < vals.length; i++) vals[i] = widths[i - 1];	//
		cells[cells.length - 1] = vals;									//
	}
	
	/**
	 * @param x
	 * @param y
	 * @param width
	 * @param height */
	public void buildTable(float x, float y, float width, float height) {		
		float padding;													//
		if(!useProportionalPadding) padding = tablePadding;				//
		else if(width <= height) padding = width * tablePadding / 2;	//
		else padding = height * tablePadding / 2;						//
		
		float totalCellWidth, totalCellHeight = height - (cells.length + 1) * padding;	//
		float tempX, tempY = y + height - padding;										//
		cellBounds = new Rectangle[cells.length][];										//
		
		for(int i = 0; i < cells.length; i++) {											//
			cellBounds[i] = new Rectangle[cells[i].length - 1];							//
			totalCellWidth = width - (cells[i].length + 1) * padding;					//
			tempX = x + padding;														//
			tempY -= totalCellHeight * cells[i][0];										//
			for(int j = 1; j < cells[i].length; j++) {									//
				cellBounds[i][j - 1] = new Rectangle(tempX, tempY, 						//
						totalCellWidth * cells[i][j], totalCellHeight * cells[i][0]);	//
				tempX += cellBounds[i][j - 1].width + padding;							//
			}
		}		
	}
	
	/**
	 * @param row
	 * @param cellIndex
	 * @return */
	public Rectangle getCell(int row, int cellIndex) {
		if(cellBounds != null) return cellBounds[row][cellIndex];	//
		return null;												//
	}
	
	/**
	 * @param row
	 * @return */
	public Rectangle[] getRow(int row) {
		if(cellBounds != null) return cellBounds[row];	//
		return null;									//
	}
	
	/**
	 * @param x
	 * @param y */
	public void translateTable(float x, float y) {
		if(cellBounds == null) return;	//
		
		for(int i = 0; i < cellBounds.length; i++) {		//
			for(int j = 0; j < cellBounds[i].length; j++) {	//
				cellBounds[i][j].x += x;					//
				cellBounds[i][j].y += y;					//
			}
		}
	}
}
