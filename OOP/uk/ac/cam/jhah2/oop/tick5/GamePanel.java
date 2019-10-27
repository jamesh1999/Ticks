package uk.ac.cam.jhah2.oop.tick5;

import java.lang.StringBuilder;

import java.awt.*;
import javax.swing.*;

public class GamePanel extends JPanel
{
	private World world = null;

	@Override
	protected void paintComponent(java.awt.Graphics g)
	{
		// Clear background
		g.setColor(java.awt.Color.WHITE);
		g.fillRect(0, 0, this.getWidth(), this.getHeight());

		// Display world
		if (world != null)
		{
			// Cells
			float cellSize = Math.min((float)this.getWidth() / world.getWidth(),
									(float)this.getHeight() / world.getHeight());
			g.setColor(Color.BLACK);
			for (int y = 0; y < world.getHeight(); ++y)
				for (int x = 0; x < world.getWidth(); ++x)
					if (world.getCell(y, x))
						g.fillRect((int)(x*cellSize), (int)(y*cellSize), (int)cellSize, (int)cellSize);

			// Grid
			g.setColor(Color.LIGHT_GRAY);
			for (int x = 0; x < world.getWidth() + 1; ++x)
				g.drawLine((int)(x * cellSize), 0, (int)(x * cellSize), (int)(world.getHeight() * cellSize));
			for (int y = 0; y < world.getHeight() + 1; ++y)
				g.drawLine(0, (int)(y * cellSize), (int)(world.getWidth() * cellSize), (int)(y * cellSize));
		}

		// Generation counter
		StringBuilder sb = new StringBuilder();
		sb.append("Generation: ");
		if (world != null) sb.append(world.getGenerationCount());
		g.setColor(Color.BLACK);
		g.drawString(sb.toString(), 8, this.getHeight() - 10);
	}

	public void display(World w)
	{
		world = w;
		repaint();
	}
}