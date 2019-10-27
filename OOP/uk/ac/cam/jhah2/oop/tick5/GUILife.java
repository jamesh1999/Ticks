package uk.ac.cam.jhah2.oop.tick5;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.io.IOException;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

public class GUILife extends JFrame
{
	private PatternStore patternStore;
	private World world;
	private ArrayList<World> cachedWorlds;

	private boolean playing;
	private Timer timer;

	private GamePanel gamePanel;
	private JButton buttonPlay;

	private World copyWorld(boolean useCloning)
	{
		if (!useCloning)
		{
			if(world instanceof ArrayWorld)
				return new ArrayWorld((ArrayWorld)world);
			return new PackedWorld((PackedWorld)world);
		}

		return world.clone();
	}

	private void moveBack()
	{
		int gen = world.getGenerationCount() - 1;
		if (gen < 0) gen = 0;
		world = cachedWorlds.get(gen);

		gamePanel.display(world);
	}

	private void moveForward()
	{
		if (world == null)
		{
			System.out.println("Please select a pattern to play (l to list):");
			return;
		}

		// Cache lookup
		if(cachedWorlds.size() > world.getGenerationCount() + 1)
			world = cachedWorlds.get(world.getGenerationCount() + 1);
		else
		{
			// Generate new
			world = copyWorld(false);
			world.nextGeneration();
			cachedWorlds.add(world);
		}

		gamePanel.display(world);
	}

	private void selectPattern(Pattern p)
	{
		// Stop play animation
		if (playing) runOrPause();

		try 
		{
			// Select the appropriate world format
			if (p.getWidth() * p.getHeight() > 64)
				world = new ArrayWorld(p);
			else
				world = new PackedWorld(p);

			// Clear generation cache
			cachedWorlds = new ArrayList<>();
			cachedWorlds.add(world);

			gamePanel.display(world);
		}
		catch (PatternFormatException e)
		{ 
			System.out.println(e.getMessage());
		}
		catch (Exception e)
		{
			System.out.println("Pattern is too large for PackedWorld.");
		}
	}

	private void runOrPause()
	{
		if (playing)
		{
			timer.cancel();
			playing=false;
			buttonPlay.setText("Play");
			return;
		}

		playing=true;
		buttonPlay.setText("Stop");
		timer = new Timer(true);
		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				moveForward();
			}
		}, 0, 25);
	}

	public GUILife(PatternStore ps)
	{
		super("Game of Life");
		patternStore = ps;
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setSize(1024, 768);

		add(createPatternsPanel(),BorderLayout.LINE_START);
		add(createControlPanel(),BorderLayout.PAGE_END);
		add(createGamePanel(),BorderLayout.CENTER);
	}

	// GUI construction ---

	private void addBorder(JComponent component, String title)
	{
		Border etch = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
		Border tb = BorderFactory.createTitledBorder(etch,title);
		component.setBorder(tb);
	}

	private JPanel createGamePanel()
	{
		gamePanel = new GamePanel();
		addBorder(gamePanel,"Game Panel");
		return gamePanel;
	}

	private JPanel createPatternsPanel()
	{
		JPanel patt = new JPanel();
		addBorder(patt,"Patterns");

		BorderLayout layout = new BorderLayout();
		patt.setLayout(layout);

		Pattern[] pattArray = {};
		pattArray = patternStore.getPatternsNameSorted().toArray(pattArray);
		JList<Pattern> list = new JList<Pattern>(pattArray);
		list.addListSelectionListener(
			e ->
			{
				Pattern p = list.getSelectedValue();
				selectPattern(p);
			});
		
		JScrollPane scrollPane = new JScrollPane(list);
		patt.add(scrollPane, BorderLayout.CENTER);

		return patt; 
	}

	private JPanel createControlPanel()
	{
		JPanel ctrl = new JPanel();
		addBorder(ctrl,"Controls");

		GridLayout layout = new GridLayout(1,3);
		ctrl.setLayout(layout);

		JButton buttonBack = new JButton("< Back");
		buttonBack.addActionListener(
			e ->
			{
				// Stop animation
				if (playing) runOrPause();
				moveBack();
			});
		ctrl.add(buttonBack);

		buttonPlay = new JButton("Play");
		buttonPlay.addActionListener(e -> runOrPause());
		ctrl.add(buttonPlay);

		JButton buttonForward = new JButton("Forward >");
		buttonForward.addActionListener(
			e ->
			{
				// Stop animation
				if (playing) runOrPause();
				moveForward();
			});
		ctrl.add(buttonForward);
		return ctrl;
	}

	// ---

	public static void main(String[] args)
	{
		// Default to cam pattern store
		if (args.length == 0)
			args = new String[]{"https://www.cl.cam.ac.uk/teaching/1819/OOProg/ticks/life.txt"};
		else if (args.length > 1)
		{
			System.out.println("Usage: java GameOfLife <path/url to store>");
			return;
		}
		
		try
		{
			// Load store and start GUI
			PatternStore ps = new PatternStore(args[0]);
			GUILife gui = new GUILife(ps);
			gui.setVisible(true);
		}
		catch (IOException ioe)
		{
			System.out.println("Failed to load pattern store");
		}
	}
}