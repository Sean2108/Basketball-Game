import pygame

BLACK = (0, 0, 0)

class Text:
    def text_objects(self, text, font):
        textSurface = font.render(text, True, BLACK)
        return textSurface, textSurface.get_rect()

    def score_display(self, world, screen):
        self.add_to_screen(screen, 30, "Player 1: " + str(world.p1score) + " points", 150, 50)
        self.add_to_screen(screen, 30, "Player 2: " + str(world.p2score) + " points", 150, 90)

    def victory_message(self, world, screen):
        winner = 1 if world.p1score > world.p2score else 2
        self.add_to_screen(screen, 100, "The winner is Player " + str(winner) + "!", 640, 320)

    def add_to_screen(self, screen, font_size, text, center_x, center_y):
        largeText = pygame.font.Font('freesansbold.ttf', font_size)
        TextSurf, TextRect = self.text_objects(text, largeText)
        TextRect.center = (center_x, center_y)
        screen.blit(TextSurf, TextRect)