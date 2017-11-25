import pygame
from Ball import Ball2D
from World import World
from PowerBar import PowerBar
from Text import Text

BLACK = (0, 0, 0)
WHITE = (255, 255, 255)
RED = (255, 0, 0)

def main():

   # initializing pygame
    pygame.init()

    clock = pygame.time.Clock()

    # top left corner is (0,0)
    win_width = 1280
    win_height = 640
    screen = pygame.display.set_mode((win_width, win_height))
    pygame.display.set_caption('Basketball')

    world = World()
    power = PowerBar()
    scoreboard = Text()
    
    world.add_rim('disk-red.png', 5).set_pos([1000, 300])
    world.add_rim('disk-red.png', 5).set_pos([1075, 300])
    
    dt = 0.1

    while True:
        # 100 fps
        clock.tick(60)

        # Clear the background, and draw the sprites
        screen.fill(WHITE)
        power.draw(screen)
        world.draw(screen)
        pygame.draw.arc(screen, RED, (50,50,50,50), 1, 1, 10)
        # draw rim line
        pygame.draw.line(screen, RED, [1000, 340], [1075, 340], 10)
        # draw backboard
        pygame.draw.line(screen, RED, [1075, 250], [1075, 640], 10)
        scoreboard.score_display(world, screen)
        if world.won:
            scoreboard.victory_message(world, screen)
            pygame.display.update()
            clock.tick(1)
            # countdown timer to close the game when won
            for i in range(100):
                pass
            break
        elif not world.shot:
            power.start(world)
        else:
            won = world.update(dt, power)

        pygame.display.update()

if __name__ == '__main__':
    main()
