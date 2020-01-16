import pytest
from Text import Text
from World import World
from unittest.mock import patch, MagicMock


@patch("Text.Text.add_to_screen")
def test_score_display_p1_turn(add_to_screen_mock):
    world = World()
    world.p1turn = True
    world.p1score = 5
    world.p2score = 6
    text = Text()
    screen = MagicMock()
    text.score_display(world, screen)
    add_to_screen_mock.assert_any_call(
        screen, 30, "Player 1: 5 points", 150, 50, (255, 0, 0)
    )
    add_to_screen_mock.assert_any_call(
        screen, 30, "Player 2: 6 points", 150, 90, (0, 0, 0)
    )


@patch("Text.Text.add_to_screen")
def test_score_display_p2_turn(add_to_screen_mock):
    world = World()
    world.p1turn = False
    world.p1score = 5
    world.p2score = 6
    text = Text()
    screen = MagicMock()
    text.score_display(world, screen)
    add_to_screen_mock.assert_any_call(
        screen, 30, "Player 1: 5 points", 150, 50, (0, 0, 0)
    )
    add_to_screen_mock.assert_any_call(
        screen, 30, "Player 2: 6 points", 150, 90, (255, 0, 0)
    )


@patch("Text.Text.add_to_screen")
def test_victory_message_p1_wins(add_to_screen_mock):
    world = World()
    world.p1score = 5
    world.p2score = 4
    text = Text()
    screen = MagicMock()
    text.victory_message(world, screen)
    add_to_screen_mock.assert_any_call(screen, 100, "The winner is Player 1!", 640, 320)


@patch("Text.Text.add_to_screen")
def test_victory_message_p2_wins(add_to_screen_mock):
    world = World()
    world.p1score = 4
    world.p2score = 5
    text = Text()
    screen = MagicMock()
    text.victory_message(world, screen)
    add_to_screen_mock.assert_any_call(screen, 100, "The winner is Player 2!", 640, 320)
