import pytest
from World import World
from unittest.mock import patch, MagicMock


@patch("World.World.update_score")
def test_reset(update_score_mock):
    world = World()
    world.shot = True
    world.scored = True
    world.p1turn = False
    power = MagicMock()
    world.reset(power)
    assert not world.shot
    assert not world.scored
    assert world.p1turn
    assert world.ball.state[:2] == [30, 30]
    assert world.shot_from == 30
    update_score_mock.assert_called_once()
    power.reset.assert_called_once()


def test_update_score_not_won():
    world = World()
    world.p1score = 10
    world.p2score = 20
    world.p1turn = True
    world.shot_from = 50
    world.update_score()
    assert world.p1score == 19
    assert world.p2score == 20
    assert not world.won


def test_update_score_won():
    world = World()
    world.p1score = 10
    world.p2score = 20
    world.p1turn = False
    world.shot_from = 30
    world.update_score()
    assert world.p1score == 10
    assert world.p2score == 30
    assert world.won


@pytest.mark.parametrize(
    "ball_state,called",
    [
        ([1296, 0, 0, 0], True),
        ([0, -16, 0, 0], True),
        ([1295, 0, 0, 0], False),
        ([0, -15, 0, 0], False),
    ],
)
@patch("World.World.check_for_collision")
@patch("World.World.reset")
def test_update_ball_out_of_bounds(
    reset_mock, check_for_collision_mock, ball_state, called
):
    world = World()
    world.ball.update = MagicMock()
    world.ball.state = ball_state
    world.update(0.1, 50)
    check_for_collision_mock.assert_called_once()
    world.ball.update.assert_called_once_with(0.1)
    if called:
        reset_mock.assert_called_once_with(50)
    else:
        reset_mock.assert_not_called()
    assert not world.scored


@pytest.mark.parametrize(
    "ball_state,scored",
    [
        ([1001, 281, 0, 0], True),
        ([1000, 281, 0, 0], False),
        ([1074, 281, 0, 0], True),
        ([1075, 281, 0, 0], False),
        ([1001, 289, 0, 0], True),
        ([1001, 280, 0, 0], False),
        ([1001, 290, 0, 0], False),
    ],
)
@patch("World.World.check_for_collision")
@patch("World.World.reset")
def test_update_ball_in_rim(reset_mock, check_for_collision_mock, ball_state, scored):
    world = World()
    world.ball.update = MagicMock()
    world.ball.state = ball_state
    world.update(0.1, 50)
    check_for_collision_mock.assert_called_once()
    world.ball.update.assert_called_once_with(0.1)
    reset_mock.assert_not_called()
    assert world.scored == scored


@pytest.mark.parametrize(
    "ball_state,hit_top,hit_side",
    [
        ([1060, 406, 100, 200], True, False),
        ([1059, 406, 100, 200], False, False),
        ([1075, 406, 100, 200], True, False),
        ([1076, 406, 100, 200], False, False),
        ([1060, 410, 100, 200], True, False),
        ([1075, 411, 100, 200], False, False),
        ([1060, 16, 100, 200], False, True),
        ([1060, 15, 100, 200], False, False),
        ([1060, 405, 100, 200], False, True),
    ],
)
def test_update_ball_in_rim(ball_state, hit_top, hit_side):
    world = World()
    world.ball.state = ball_state
    world.ball.prev_state = [1000, 400, 50, 150]
    result = world.check_backboard_collision()
    if hit_top:
        assert world.ball.state == [1000, 400, 50, -150]
    elif hit_side:
        assert world.ball.state == [1000, 400, -50, 150]
    else:
        assert world.ball.state == ball_state
    assert result == (hit_top or hit_side)
