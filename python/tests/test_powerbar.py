import pytest
from unittest.mock import patch, MagicMock, ANY
from PowerBar import PowerBar
from World import World
from math import radians, cos, sin


@patch("PowerBar.pygame")
def test_draw(pygame_mock):
    screen = MagicMock()
    powerbar = PowerBar()
    powerbar.draw(screen)
    pygame_mock.draw.rect.assert_any_call(screen, ANY, ANY, 1)
    pygame_mock.draw.rect.assert_any_call(screen, ANY, ANY, 0)


@patch("PowerBar.pygame.mouse.get_pos", return_value=(100, 200))
def test_get_angle_dx_0(get_pos_mock):
    powerbar = PowerBar()
    world = World()
    world.ball.state = [100, 300, 400, 450]
    result = powerbar.get_angle(world)
    assert result == radians(90)


@pytest.mark.parametrize(
    "atan_value,expected", [(0, 0), (-1, 0), (90, 90), (91, radians(90))]
)
@patch("PowerBar.pygame.mouse.get_pos", return_value=(150, 200))
def test_get_angle_dx_not_0(get_pos_mock, atan_value, expected):
    powerbar = PowerBar()
    world = World()
    world.ball.state = [100, 300, 400, 450]
    with patch("PowerBar.atan", return_value=atan_value) as atan:
        result = powerbar.get_angle(world)
    assert result == expected


@patch("PowerBar.PowerBar.get_angle", return_value=50)
def test_shoot_ball(get_angle_mock):
    powerbar = PowerBar()
    powerbar.power = 60
    world = World()
    world.ball.state = [100, 300, 400, 450]
    world.ball.set_vel = MagicMock()
    powerbar.shoot_ball(world)
    assert world.shot
    assert world.shot_from == 100
    vel = 150 * 60 / 100
    world.ball.set_vel.assert_called_once_with([vel * cos(50), vel * sin(50)])


@pytest.mark.parametrize(
    "power,direction,expected_power, expected_direction",
    [(0, 1, 1, 1), (99, 1, 100, -1), (1, -1, 0, 1), (98, 1, 99, 1), (2, -1, 1, -1)],
)
def test_move_bar(power, direction, expected_power, expected_direction):
    powerbar = PowerBar()
    powerbar.power, powerbar.direction = power, direction
    powerbar.move_bar()
    assert powerbar.power == expected_power
    assert powerbar.direction == expected_direction


def test_reset():
    powerbar = PowerBar()
    powerbar.power = 50
    powerbar.direction = -1
    assert powerbar.power == 50
    assert powerbar.direction == -1
    powerbar.reset()
    assert powerbar.power == 0
    assert powerbar.direction == 1
