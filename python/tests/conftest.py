import pytest

from Ball import Ball2D
from unittest.mock import patch, ANY


@pytest.fixture
@patch("Ball.pygame")
def ball(pygame_mock):
    ball = Ball2D("test_img", 2, 3)
    pygame_mock.image.load.assert_called_once_with("test_img")
    pygame_mock.transform.scale.assert_called_once_with(ANY, (4, 4))
    return ball
