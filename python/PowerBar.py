import pygame, sys
from math import atan, radians, cos, sin

BLACK = (0, 0, 0)
BLUE = (0, 0, 255)

class PowerBar:
    def __init__(self):
        self.power = 0
        self.direction = 1
        self.running = True

    def draw(self, screen):
        pygame.draw.rect(screen, BLACK, (320, 100, 640, 100), 1)
        pygame.draw.rect(screen, BLUE, (320, 100, self.power * 640 / 100, 100), 0)

    def get_angle(self, world):
        x, y = pygame.mouse.get_pos()
        dx = x - world.ball.state[0]
        dy = 640 - y - world.ball.state[1]
        if dx == 0:
            angle = radians(90)
        else:
            angle = atan(dy / float(dx))
            if angle < 0:
                angle = 0
            elif angle > 90:
                angle = radians(90)
        return angle

    def start(self, world):
        event = pygame.event.poll()
        if event.type == pygame.QUIT:
            pygame.quit()
            sys.exit(0)
        elif event.type == pygame.KEYDOWN:
            if event.key == pygame.K_a and world.ball.state[0] > 30:
                world.ball.set_pos([world.ball.state[0] - 20, world.ball.state[1]])
            elif event.key == pygame.K_d and world.ball.state[0] < 450:
                world.ball.set_pos([world.ball.state[0] + 20, world.ball.state[1]])
        elif event.type == pygame.MOUSEBUTTONDOWN and event.button == 1:
            self.shoot_ball(world)
        self.move_bar()

    def shoot_ball(self, world):
        angle = self.get_angle(world)
        world.shot = True
        world.shot_from = world.ball.state[0]
        vel = 150 * self.power / 100
        vel_x = vel * cos(angle)
        vel_y = vel * sin(angle)
        world.ball.set_vel([vel_x, vel_y])
        # testing value, 100% will score at default position
        # world.ball.set_vel([75, 96])

    def move_bar(self):
        self.power += self.direction
        if self.power <= 0 or self.power >= 100:
            self.direction *= -1

    def reset(self):
        self.power = 0
        self.direction = 1