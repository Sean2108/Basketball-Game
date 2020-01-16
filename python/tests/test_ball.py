import pytest

from unittest.mock import patch


def test_init(ball):
    assert ball.state == ball.prev_state == [0, 0, 0, 0]
    assert ball.mass == 3
    assert ball.radius == 2


def test_f(ball):
    result = ball.f(0.1, [1, 2, 3, 4], 5, 6)
    assert result == [-12, -22, -15, -26]


@patch("Ball.ode.integrate", return_value=[0, 1, 2, 3])
def test_update(ode_integrate_mock, ball):
    ball.state = [1, 2, 3, 4]
    ball.update(0.1)
    assert ball.prev_state == [1, 2, 3, 4]
    ode_integrate_mock.assert_called_once_with(0.1)
    assert ball.state == [0, 1, 2, 3]


@patch("Ball.ode.integrate", return_value=[0, 1, 2, 3])
@patch("Ball.ode.set_initial_value")
def test_update_set_pos_set_vel(ode_initial_value_mock, ode_integrate_mock, ball):
    ball.update(0.1)
    ball.set_pos([5, 6])
    ode_initial_value_mock.assert_called_with([5, 6, 2, 3], 0.1)
    ball.set_vel([7, 8])
    ode_initial_value_mock.assert_called_with([5, 6, 7, 8], 0.1)
    assert ball.state == [5, 6, 7, 8]


def test_move_by(ball):
    ball.state = [1, 2, 3, 4]
    ball.move_by([1, 2])
    assert ball.prev_state == [1, 2, 3, 4]
    assert ball.state == [2, 4, 3, 4]
