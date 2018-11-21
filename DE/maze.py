import math, time

# Import/Initialise pygame
import pygame
from pygame.locals import *
pygame.init()

# Display parameters
# Colour constants
RED = (255,0,0)
GREEN = (0,255,0)
BLUE = (0,0,255)

TARGET_FPS = 60
TARGET_UPS = 600
LONG_EDGE = 1200

# Environment parameters
MAZE = pygame.image.load("maze1.png")



# Simple 2D vector with some operations defined
class Vector2D(object):

	def __init__(self, t):
		self.__x = t[0]
		self.__y = t[1]

	def asTuple(self):
		return (self.__x, self.__y)

	def floor(self):
		return Vector2D((int(self.__x), int(self.__y)))

	def rotate(self, angle):
		return Vector2D((
			self.__x * math.cos(angle) - self.__y * math.sin(angle),
			self.__y * math.cos(angle) + self.__x * math.sin(angle)
			))

	# Operator overloading
	def __add__(self, other):
		return Vector2D((self.__x + other.__x, self.__y + other.__y))
	def __sub__(self, other):
		return Vector2D((self.__x - other.__x, self.__y - other.__y))
	def __mul__(self, other):
		return Vector2D((self.__x * other, self.__y * other))
	def __div__(self, other):
		return Vector2D((self.__x / other, self.__y / other))


# Implementation of a Moore FSM
class StateMachine(object):
	def __init__(self, inputs, outputs, machine):
		self.__machine = machine
		self.__state = 0

		self.__inputs = (0,) * inputs
		self.__outputs = (0,) * outputs

	# 'Clocks' the state machine once
	def update(self, inputs):
		self.__inputs = inputs

		current = self.__machine[self.__state]
		for transition in current[1]:
			if not transition[1](inputs): continue;

			self.__state = transition[0]
			break;

		self.__outputs = self.__machine[self.__state][0]
		return self.__outputs

	# Returns the idx of the current state
	def getState(self):
		return self.__state

	# Access last inputs/outputs
	def getInput(self, idx):
		return self.__inputs[idx]
	def getInputs(self):
		return self.__inputs
	def getOutput(self, idx):
		return self.__outputs[idx]
	def getOutputs(self):
		return self.__outputs

class Ant(object):

	BODY_COLOUR = BLUE
	BODY_RADIUS = 6

	ANTENNAE_LENGTH = 18
	ANTENNAE_THICKNESS = 3
	TOUCHING_COLOUR = GREEN
	DEFAULT_COLOUR = RED

	MOVEMENT_SPEED = 2

	def __init__(self, pos):
		self.pos = pos
		self.angle = 0

		self.brain = StateMachine(2, 3,
			(((1,0,0), ((1, lambda inputs: inputs[0] or inputs[1]), (0, lambda inputs: True))), # Making use of the fact that the FSM class checks state transitions in turn to simplify the expressions
			((0,1,0), ((1, lambda inputs: inputs[0] or inputs[1]), (2, lambda inputs: True))),
			((1,0,1), ((1, lambda inputs: inputs[1]), (2, lambda inputs: True))),	
			))

	def draw(self, surface):
		pygame.draw.circle(surface, self.BODY_COLOUR, self.pos.floor().asTuple(), int(self.BODY_RADIUS / sf), 0)

		# Antennae
		pygame.draw.line(surface, 
			self.TOUCHING_COLOUR if self.brain.getInput(0) else self.DEFAULT_COLOUR, 
			self.pos.floor().asTuple(), 
			(self.pos + Vector2D((0, self.ANTENNAE_LENGTH)).rotate(self.angle - 0.78)).floor().asTuple(),
			int(self.ANTENNAE_THICKNESS / sf))
		pygame.draw.line(surface, 
			self.TOUCHING_COLOUR if self.brain.getInput(1) else self.DEFAULT_COLOUR, 
			self.pos.floor().asTuple(), 
			(self.pos + Vector2D((0, self.ANTENNAE_LENGTH)).rotate(self.angle + 0.78)).floor().asTuple(),
			int(self.ANTENNAE_THICKNESS / sf))

	def update(self):
		# Check antennae
		leftAntenna = (self.pos + Vector2D((0, self.ANTENNAE_LENGTH)).rotate(self.angle - 0.78)).floor().asTuple()
		rightAntenna = (self.pos + Vector2D((0, self.ANTENNAE_LENGTH)).rotate(self.angle + 0.78)).floor().asTuple()

		# In contact w/ wall?
		leftInput = MAZE.get_at(leftAntenna).r < 128
		rightInput = MAZE.get_at(rightAntenna).r < 128

		# In contact w/ maze end?
		leftDone = leftInput and MAZE.get_at(leftAntenna).g > 128
		rightDone = rightInput and MAZE.get_at(rightAntenna).g > 128
		if leftDone or rightDone: return;

		# Update FSM & move
		action = self.brain.update((leftInput, rightInput))

		if action[0]: self.pos += Vector2D((0, self.MOVEMENT_SPEED)).rotate(self.angle)
		if action[1]: self.angle -= 0.1
		if action[2]: self.angle += 0.1



maze_size = MAZE.get_size()
sf = LONG_EDGE / max(maze_size)
screen_size = (Vector2D(maze_size) * sf).floor().asTuple()
screen = pygame.display.set_mode(screen_size)
screen_buffer = MAZE.copy().convert()

ant = Ant(Vector2D((220, 1545)))

draw_time = time.time()
update_time = time.time()

done = False
while not done:
	if time.time() - update_time > 1 / TARGET_UPS:
		update_time = time.time()

		# Update
		ant.update()

	if time.time() - draw_time > 1 / TARGET_FPS:
		draw_time = time.time()

		# Draw
		screen_buffer.blit(MAZE, (0,0))
		ant.draw(screen_buffer)

		pygame.transform.scale(screen_buffer, screen_size, screen)
		pygame.display.flip()

	# Event loop
	for ev in pygame.event.get():
		if ev.type == QUIT:
			done = True
