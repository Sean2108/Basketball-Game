import pygame
import numpy as np
from Ball import Ball2D
from Rim import Rim2D


class World:
    def __init__(self):
        self.ball = Ball2D("disk-blue.png", 15, 0.1).set_pos([30, 30])
        self.rim = []
        self.e = 1.0  # Coefficient of restitution
        self.shot = False
        self.p1score = 0
        self.p2score = 0
        self.scored = False
        self.p1turn = True
        self.won = False
        self.shot_from = 30

    def reset(self, power):
        self.shot = False
        if self.scored:
            self.update_score()
        self.scored = False
        self.p1turn = not self.p1turn
        # default position is 30, 30
        self.ball = self.ball.set_pos([30, 30])
        self.shot_from = 30
        power.reset()

    def update_score(self):
        # score is proportional to distance to the rim, greater distance -> greater score awarded
        score = int((1030 - self.shot_from) / 100)
        if self.p1turn:
            self.p1score += score
        else:
            self.p2score += score
        if self.p1score >= 30 or self.p2score >= 30:
            self.won = True

    def add_rim(self, imgfile, radius):
        rim = Rim2D(imgfile, radius)
        self.rim.append(rim)
        return rim

    def draw(self, screen):
        self.ball.draw(screen)
        for rim in self.rim:
            rim.draw(screen)

    def update(self, dt, power):
        self.check_for_collision()
        self.ball.update(dt)
        # ball is out of bounds -> reset
        if (
            self.ball.state[0] > 1280 + self.ball.radius
            or self.ball.state[1] < 0 - self.ball.radius
        ):
            self.reset(power)
        # ball is in the rim -> scored
        top_of_ball = self.ball.state[1] + self.ball.radius
        if (
            self.ball.state[0] > 1000
            and self.ball.state[0] < 1075
            and top_of_ball > 295
            and top_of_ball < 305
        ):
            self.scored = True

    def normalize(self, v):
        return v / np.linalg.norm(v)

    def check_for_collision(self):
        if self.check_backboard_collision():
            return

        self.check_rim_collision()

    def check_backboard_collision(self):
        right_of_ball = self.ball.state[0] + self.ball.radius
        if right_of_ball >= 1075 and right_of_ball <= 1090:
            bottom_of_ball = self.ball.state[1] - self.ball.radius
            # hit top of backboard
            if bottom_of_ball > 390 and bottom_of_ball <= 395:
                self.ball.state = self.ball.prev_state
                self.ball.set_vel([self.ball.state[2], -self.ball.state[3]])
                return True
            # hit side of backboard
            if bottom_of_ball > 0 and bottom_of_ball <= 390:
                self.ball.state = self.ball.prev_state
                self.ball.set_vel([-self.ball.state[2], self.ball.state[3]])
                return True
        return False

    def check_rim_collision(self):
        pos_i = self.ball.state[0:2]
        for j in range(0, len(self.rim)):

            pos_j = np.array(self.rim[j].state[0:2])
            dist_ij = np.sqrt(np.sum((pos_i - pos_j) ** 2))

            radius_j = self.rim[j].radius
            if dist_ij > self.ball.radius + radius_j:
                continue

            self.ball.state = self.ball.prev_state

            vel_i = np.array(self.ball.state[2:])
            n_ij = self.normalize(pos_i - pos_j)

            mass_i = self.ball.mass

            J = -(1 + self.e) * np.dot(vel_i, n_ij) / ((1.0 / mass_i + 1.0))

            vel_i_aftercollision = vel_i + n_ij * J / mass_i

            self.ball.set_vel(vel_i_aftercollision)
            return
